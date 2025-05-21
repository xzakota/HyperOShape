package com.xzakota.oshape.startup.rule.systemui.plugin.aod

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.util.image.DrawableUtils
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setFloatField
import com.xzakota.code.extension.setFloatFieldWith
import com.xzakota.oshape.startup.rule.systemui.api.FlashlightController
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.api.ShortcutEntity
import com.xzakota.oshape.startup.rule.systemui.plugin.BasePluginHook
import com.xzakota.oshape.startup.rule.systemui.shared.AodPlugin.shortcutPluginImpl
import com.xzakota.oshape.startup.rule.systemui.shared.AodPlugin.shortcutViewLayoutController
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.activeSensitivityLevel1
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonActiveBgColor
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonBgMargins
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.buttonNormalBgColor
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.leftButtonType
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.rightButtonType

class CustomizeBottomShortcut200(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    private val mainHandler by lazy {
        MiuiStub.baseProvider.mainHandler
    }

    private var normalBg: Drawable? = null
    private var activeBg: Drawable? = null

    private var leftButtonOnKSCReceiver: ((Boolean) -> Unit)? = null

    override fun onHookLoad() {
        addHook(shortcutPluginImpl.afterHookedMethodByName("onSystemUIAction") { param ->
            val action = param.argAs<String>()
            val bundle = param.argAs<Bundle?>(1)

            when (action) {
                "onKeyguardShowingChanged" -> onKeyguardShowingChanged(bundle?.getBoolean("keyguardShowing") == true)
                "onKeyguardOccludedChanged" -> onKeyguardOccludedChanged(bundle?.getBoolean("occluded") == true)
            }
        })

        val isNormalColorNotTransparent = Color.alpha(buttonNormalBgColor) != 0
        addHook(shortcutViewLayoutController.afterHookedFirstConstructor {
            if (isNormalColorNotTransparent) {
                normalBg = createLayerDrawable(buttonNormalBgColor)
            }

            if (Color.alpha(buttonActiveBgColor) != 0) {
                activeBg = createLayerDrawable(buttonActiveBgColor)
            }
        })

        if (isNormalColorNotTransparent) {
            resetButtonAnim()
        }

        addHook(shortcutViewLayoutController.afterHookedMethodByName("updateShortcutView") { param ->
            val controller = param.nonNullThisObject
            val shortcutViewLeft = controller.getObjectFieldAs<View>("shortcutViewLeft")
            val shortcutViewRight = controller.getObjectFieldAs<View>("shortcutViewRight")
            // val shortcutViewLeftLayout = controller.getObjectFieldAs<View>("shortcutViewLeftLayout")
            // val shortcutViewRightLayout = controller.getObjectFieldAs<View>("shortcutViewRightLayout")

            val shortcutEntity = ShortcutEntity(param.argAs())
            val shortcutImage = param.argAs<ImageView>(1)
            val shortcutLayout = param.argAs<View>(2)

            if (leftButtonType == 1 || rightButtonType == 1) {
                shortcutLayout.isVisible = false
                return@afterHookedMethodByName
            }

            shortcutImage.background = normalBg
            if (shortcutImage == shortcutViewLeft) {
                when (leftButtonType) {
                    2 -> replaceFlashlightButton(shortcutEntity, shortcutImage)
                }
            } else if (shortcutImage == shortcutViewRight) {
                when (rightButtonType) {
                    2 -> replaceCameraButton(shortcutEntity, shortcutImage)
                }
            }
        })
    }

    private fun onKeyguardShowingChanged(isKeyguardShowing: Boolean) {
        logD("onKeyguardShowingChanged($isKeyguardShowing)")

        mainHandler.post {
            leftButtonOnKSCReceiver?.invoke(isKeyguardShowing)
        }
    }

    private fun onKeyguardOccludedChanged(isOccluded: Boolean) {
        logD("onKeyguardOccludedChanged($isOccluded)")
    }

    private fun resetButtonAnim() {
        var initWidth = 0
        var initHeight = 0

        val baseShortcutAnimView = loadClass("com.miui.keyguard.shortcuts.view.BaseShortcutAnimView")
        addHook(baseShortcutAnimView.afterHookedFirstConstructor {
            val view = it.thisObjectAs<View>()

            val iconCirclePaint = view.getObjectFieldAs<Paint>("iconCirclePaint")
            iconCirclePaint.setColor(buttonNormalBgColor)
            iconCirclePaint.strokeWidth = 0F
        })

        addHook(baseShortcutAnimView.afterHookedMethodByName("updateShortcutIconSize") {
            val view = it.thisObjectAs<View>()

            initWidth = view.getIntField("iconWidth") - buttonBgMargins * 2
            initHeight = view.getIntField("iconHeight") - buttonBgMargins * 2
        })

        addHook(baseShortcutAnimView.beforeHookedMethod("onDraw", Canvas::class.java) {
            val view = it.thisObjectAs<View>()

            view.setFloatFieldWith("iconCircleWidth") { it + initWidth }
            view.setFloatFieldWith("iconCircleHeight") { it + initHeight }
            view.setFloatField("iconCircleAlpha", Color.alpha(buttonNormalBgColor) / 255F)
            view.setFloatField("iconCircleRadius", 100F)
        })
    }

    private fun replaceCameraButton(shortcutEntity: ShortcutEntity, shortcutImage: ImageView) {
        shortcutEntity.action = "android.media.action.VIDEO_CAMERA"
        shortcutEntity.targetPackage = "com.android.camera"
        shortcutEntity.targetClass = "com.android.camera.Camera"
        shortcutImage.setImageDrawable(getThemeIcon("camera"))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun replaceFlashlightButton(shortcutEntity: ShortcutEntity, shortcutImage: ImageView) {
        val flashlightController by lazy {
            MiuiStub.sysUIProvider.flashlightController
        }

        shortcutEntity.action = "android.intent.action.VIEW"
        shortcutEntity.targetPackage = "miui.systemui.plugin"
        shortcutEntity.targetClass = "miui.systemui.flashlight.MiFlashlightActivity"

        var downTime = 0L
        val offDrawable = getThemeIcon("flashlight_off")
        val onDrawable = getThemeIcon("flashlight_on")

        val resetImage = { isEnabled: Boolean, isPost: Boolean ->
            shortcutImage.run {
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

        leftButtonOnKSCReceiver = {
            if (it) {
                resetImage(flashlightController.isEnabled(), true)
                FlashlightController.addListener(flashlightListener)
            } else {
                FlashlightController.removeListener(flashlightListener)
            }
        }

        val longPressRunnable = Runnable {
            flashlightController.toggleFlashlight()
            shortcutImage.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        val onTouchListener = View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downTime = SystemClock.uptimeMillis()

                    view.animate().setDuration(100).scaleX(1.21F).scaleY(1.21F).withEndAction {
                        if (activeSensitivityLevel1 >= 300) {
                            shortcutImage.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    view.animate().setDuration(100).scaleX(1F).scaleY(1F).withEndAction {
                        val duration = SystemClock.uptimeMillis() - downTime
                        if (duration >= activeSensitivityLevel1) {
                            mainHandler.post(longPressRunnable)
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    view.animate().setDuration(150).scaleX(1F).scaleY(1F)
                }
            }

            true
        }

        resetImage(false, false)
        shortcutImage.doOnAttach {
            shortcutImage.setOnTouchListener(onTouchListener)
        }
        shortcutImage.doOnDetach {
            shortcutImage.setOnTouchListener(null)
        }

    }

    private fun createLayerDrawable(color: Int): Drawable = LayerDrawable(
        arrayOf(DrawableUtils.getShapeSolidDrawable(color))
    ).apply {
        setLayerInset(0, buttonBgMargins, buttonBgMargins, buttonBgMargins, buttonBgMargins)
    }
}
