package com.xzakota.oshape.startup.rule.systemui

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.xzakota.android.extension.content.getString
import com.xzakota.android.hook.core.loader.base.IMemberAfterHookCallback
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.util.image.DrawableUtils
import com.xzakota.code.extension.getFloatField
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setFloatField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.FlashlightController
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.activeSensitivityLevel1
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.activeSensitivityLevel2
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonActiveBgColor
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonBgMargins
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonNormalBgColor
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.keyguardBottomAreaInjector
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.leftButtonType
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.rightButtonType
import com.xzakota.oshape.util.DexKit
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Method

@SuppressLint("ClickableViewAccessibility")
object CustomizeBottomShortcut : BaseHook() {
    private var normalBg: Drawable? = null
    private var activeBg: Drawable? = null

    // private var isBottomIconRectDeep = false

    private lateinit var mainHandler: Handler

    override fun onHookLoad() {
        val isNormalColorNotTransparent = Color.alpha(buttonNormalBgColor) != 0
        val isActiveColorNotTransparent = Color.alpha(buttonActiveBgColor) != 0

        keyguardBottomAreaInjector.afterHookedFirstConstructor {
            if (isNormalColorNotTransparent || isActiveColorNotTransparent) {
                normalBg = createLayerDrawable(buttonNormalBgColor)
                activeBg = createLayerDrawable(buttonActiveBgColor)

                if (isNormalColorNotTransparent) {
                    setNormalBg()
                }
            }

            if (leftButtonType == 2 || rightButtonType == 2) {
                mainHandler = MiuiStub.baseProvider.mainHandler
            }
        }

        /*
        keyguardBottomAreaInjector.beforeHookedMethod("updateIcons") { param ->
            val injector = param.nonNullThisObject

            isBottomIconRectDeep = injector.getBooleanField("mBottomIconRectIsDeep")
        }
         */

        if (leftButtonType == 1) {
            hideLeftButton()
        } else if (leftButtonType == 2) {
            replaceFlashlightButton()
        }

        if (rightButtonType == 1) {
            hideRightButton()
        } else if (rightButtonType == 2) {
            customRightButtonIcon()

            if (isNormalColorNotTransparent) {
                resetCameraAnim()
            }
        }
    }

    private fun hideLeftButton() {
        keyguardBottomAreaInjector.afterHookedMethod("updateIcons") {
            it.nonNullThisObject.getObjectFieldAs<ImageView?>("mLeftButton")?.setImageDrawable(null)
        }
    }

    private fun hideRightButton() {
        keyguardBottomAreaInjector.afterHookedMethod("updateRightAffordanceViewLayoutVisibility") {
            it.nonNullThisObject.getObjectFieldAs<View?>("mRightButton")?.visibility = View.GONE
        }
    }

    private fun customRightButtonIcon(name: String = "camera") {
        val drawable = getThemeIcon(name) ?: return

        val setDrawable = { imageViewName: String ->
            IMemberAfterHookCallback {
                it.nonNullThisObject.getObjectFieldAs<ImageView?>(imageViewName)?.setImageDrawable(drawable)
            }
        }

        keyguardBottomAreaInjector.afterHookedMethod("updateRightIcon", block = setDrawable("mRightButton"))

        val hookCallback = setDrawable("mIconView")
        updateSizeForScreenSizeChange.createAfterHook(block = hookCallback)
        miuiKeyguardCameraViewInternal.afterHookedMethod("setDarkStyle", Boolean::class.java, block = hookCallback)
    }

    private fun resetCameraAnim() {
        var initWidth = 0
        var initHeight = 0

        miuiKeyguardCameraViewInternal.superclass?.afterHookedFirstConstructor {
            val view = it.thisObjectAs<View>()

            initWidth = view.getIntField("mIconWidth") - buttonBgMargins * 2
            initHeight = view.getIntField("mIconHeight") - buttonBgMargins * 2

            val iconCirclePaint = view.getObjectFieldAs<Paint>("mIconCirclePaint")
            iconCirclePaint.setColor(buttonNormalBgColor)
            iconCirclePaint.strokeWidth = 0F
        }

        miuiKeyguardCameraViewInternal.beforeHookedMethod("onDraw", Canvas::class.java) {
            val view = it.thisObjectAs<View>()

            val circleWidth = view.getFloatField("mIconCircleWidth")
            val circleHeight = view.getFloatField("mIconCircleHeight")

            view.setFloatField("mIconCircleWidth", circleWidth + initWidth)
            view.setFloatField("mIconCircleHeight", circleHeight + initHeight)
            view.setFloatField("mIconCircleAlpha", Color.alpha(buttonNormalBgColor) / 255F)
            view.setFloatField("mPreViewRadius", 100F)
        }
    }

    private fun replaceFlashlightButton() {
        val flashlightController by lazy {
            MiuiStub.sysUIProvider.flashlightController
        }

        var downTime = 0L
        var flashlightIcon: ImageView? = null
        val offDrawable = getThemeIcon("flashlight_off")
        val onDrawable = getThemeIcon("flashlight_on")

        val resetImage = { isEnabled: Boolean, isPost: Boolean ->
            flashlightIcon?.run {
                val action = action@{
                    val isInnerEnabled = getAdditionalInstanceFieldAs<Boolean?>("isEnabled")
                    if (isInnerEnabled != null) {
                        if (isInnerEnabled == isEnabled) {
                            return@action
                        } else {
                            setAdditionalInstanceField("isEnabled", isEnabled)
                        }
                    }

                    if (isEnabled) {
                        setImageDrawable(onDrawable)
                        background = activeBg
                    } else {
                        setImageDrawable(offDrawable)
                        background = normalBg
                    }
                }

                if (isPost) {
                    post(action)
                } else {
                    action()
                }
            }
        }

        val flashlightListener = object : FlashlightController.FlashlightListener {
            override fun onFlashlightError() {
                logD("onFlashlightError")
                resetImage(false, true)
            }

            override fun onFlashlightChanged(isEnabled: Boolean) {
                logD("onFlashlightChanged($isEnabled)")
                resetImage(isEnabled, true)
            }

            override fun onFlashlightAvailabilityChanged(isEnabled: Boolean) {
                logD("onFlashlightAvailabilityChanged($isEnabled)")
                if (!isEnabled) {
                    resetImage(false, true)
                }
            }
        }

        loadClass("com.android.systemui.statusbar.policy.KeyguardStateControllerImpl").beforeHookedMethod(
            "notifyKeyguardState", Boolean::class.java, Boolean::class.java
        ) { param ->
            val showing = param.argAs<Boolean>()
            val occluded = param.argAs<Boolean>(1)

            logD("notifyKeyguardState($showing, $occluded)")

            if (showing && !occluded) {
                resetImage(flashlightController.isEnabled(), true)
                FlashlightController.addListener(flashlightListener)
            } else {
                FlashlightController.removeListener(flashlightListener)
            }
        }

        val longPressRunnable1 = Runnable {
            flashlightController.toggleFlashlight()
            flashlightIcon?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        val longPressRunnable2 = Runnable {
            val intent = flashlightController.getShowFlashlightIntent()
            if (intent != null) {
                flashlightController.showFlashlight(intent = intent)
            }
        }

        val touchListener = View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downTime = SystemClock.uptimeMillis()

                    view.animate().setDuration(100).scaleX(1.21F).scaleY(1.21F).withEndAction {
                        if (activeSensitivityLevel1 >= 300) {
                            flashlightIcon?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    }
                    mainHandler.postDelayed(longPressRunnable2, activeSensitivityLevel2)
                }

                MotionEvent.ACTION_UP -> {
                    mainHandler.removeCallbacks(longPressRunnable2)
                    view.animate().setDuration(100).scaleX(1F).scaleY(1F).withEndAction {
                        val duration = SystemClock.uptimeMillis() - downTime
                        if (duration >= activeSensitivityLevel1) {
                            mainHandler.post(longPressRunnable1)
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    mainHandler.removeCallbacks(longPressRunnable2)
                    view.animate().setDuration(150).scaleX(1F).scaleY(1F)
                }
            }

            true
        }

        keyguardBottomAreaInjector.afterHookedMethod("updateLeftIcon") { param ->
            flashlightIcon = param.nonNullThisObject.getObjectFieldAs<ImageView?>("mLeftButton")?.also {
                resetImage(false, false)
                it.contentDescription = it.context.getString("quick_settings_flashlight_label")
                it.setOnTouchListener(touchListener)
            }
        }
    }

    private fun setNormalBg() {
        val updateIcon = { method: String, field: String ->
            keyguardBottomAreaInjector.afterHookedMethod(method) { param ->
                param.nonNullThisObject.getObjectFieldAs<ImageView?>(field)?.run {
                    background = normalBg
                }
            }
        }

        if (leftButtonType != 1) {
            updateIcon("updateLeftIcon", "mLeftButton")
        }
        updateIcon("updateRightIcon", "mRightButton")
    }

    private fun createLayerDrawable(color: Int): Drawable = LayerDrawable(
        arrayOf(DrawableUtils.getShapeSolidDrawable(color))
    ).apply {
        setLayerInset(0, buttonBgMargins, buttonBgMargins, buttonBgMargins, buttonBgMargins)
    }

    private val miuiKeyguardCameraViewInternal by lazy {
        loadClass("com.android.keyguard.MiuiKeyguardCameraView\$MiuiKeyguardCameraViewInternal")
    }

    private val updateSizeForScreenSizeChange by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(updateSizeForScreenSizeChange)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(miuiKeyguardCameraViewInternal)
                    name("updateSizeForScreenSizeChange", StringMatchType.StartsWith)
                }
            }.singleOrNull()
        }
    }
}
