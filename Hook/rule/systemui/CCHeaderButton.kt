package com.xzakota.oshape.startup.rule.systemui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManagerGlobal
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.xzakota.android.extension.content.getDimensionPixelSize
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.hook.startup.component.androidx.constraintlayout.ReConstraintLayout
import com.xzakota.android.hook.startup.component.androidx.constraintlayout.ReConstraintSet
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.android.util.SystemUtils
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.reflect.isStatic
import com.xzakota.oshape.R
import com.xzakota.oshape.extension.reBooleanInt
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.systemui.api.MiuiDependency
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.controlCenterHeaderController
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.isHideHeaderCarrier
import com.xzakota.oshape.ui.fragment.host.systemui.ControlCenterHeaderFragment
import com.xzakota.oshape.util.DexKit
import miui.sdk.isVerticalMode
import miuix.animation.Folme
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Method
import kotlin.math.abs

class CCHeaderButton : BaseHook() {
    private lateinit var shortcutLayout: ViewGroup
    private lateinit var shortcutLayoutFolme: Any

    private lateinit var actionLayout: ViewGroup
    private lateinit var actionLayoutFolme: Any

    override fun onHookLoad() {
        controlCenterHeaderController.afterHookedFirstConstructor { param ->
            addButtons(param.nonNullThisObject.getObjectFieldAs<ViewGroup>("mView"))

            shortcutLayoutFolme = Folme.useAt(shortcutLayout)
            actionLayoutFolme = Folme.useAt(actionLayout)
        }

        controlCenterHeaderController.afterHookedMethod("updateConstraint") { param ->
            val headerView = ReConstraintLayout(param.nonNullThisObject.getObjectFieldAs<ViewGroup>("mView"))
            val carrierView = headerView.instance.findViewByIdNameAs<TextView>("normal_control_center_carrier_view")
            val statusBar = headerView.instance.findViewByIdNameAs<View>("normal_control_center_status_bar")

            ReConstraintSet().apply {
                clone(headerView)

                connect(shortcutLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                if (isVerticalMode(headerView.instance.context)) {
                    clear(shortcutLayout.id, ConstraintSet.TOP)
                    connect(
                        shortcutLayout.id,
                        ConstraintSet.BOTTOM,
                        statusBar.id,
                        ConstraintSet.TOP,
                        leftButtonGroupBottomMargin
                    )

                    connect(actionLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    connect(
                        actionLayout.id,
                        ConstraintSet.BOTTOM,
                        if (isHideHeaderCarrier) {
                            statusBar.id
                        } else {
                            carrierView.id
                        },
                        ConstraintSet.TOP,
                        rightButtonGroupBottomMargin
                    )
                } else if (isLeftButtonGroupShowOnLandscape) {
                    connect(shortcutLayout.id, ConstraintSet.BOTTOM, statusBar.id, ConstraintSet.BOTTOM, 0)
                    connect(shortcutLayout.id, ConstraintSet.TOP, statusBar.id, ConstraintSet.TOP)
                    if (!isHideHeaderCarrier) {
                        connect(
                            carrierView.id,
                            ConstraintSet.START,
                            shortcutLayout.id,
                            ConstraintSet.END,
                            buttonSpacing
                        )
                    }
                }
            }.applyTo(headerView)
        }

        controlCenterHeaderController.afterHookedMethod("updateDateVisibility") { param ->
            val controller = param.nonNullThisObject
            val view = controller.getObjectFieldAs<ViewGroup>("mView")

            val isVerticalMode = isVerticalMode(view.context)
            shortcutLayout.isVisible = isVerticalMode || isLeftButtonGroupShowOnLandscape
            actionLayout.isVisible = isVerticalMode
        }

        controlCenterHeaderController.afterHookedMethodByName("updateCarrierAndPrivacyVisible") { param ->
            val viewGroup = param.nonNullThisObject.getObjectFieldAs<ViewGroup>("mView")
            val privacyContainer = viewGroup.findViewByIdNameAs<View>("normal_privacy_container")

            var isVisible = isVerticalMode(viewGroup.context)
            if (isButtonGroupAdaptive) {
                isVisible = isVisible && !privacyContainer.isVisible
            }

            actionLayout.isVisible = isVisible
        }

        loadClass("com.android.systemui.controlcenter.shade.CombinedHeaderController")
            .afterHookedMethod("onSwitchProgressChanged", Float::class.java) { param ->
                val controller = param.nonNullThisObject
                val dateView = controller.getObjectFieldAs<View>("controlCenterDateView")
                val carrierView = controller.getObjectFieldAs<View>(
                    if (isMoreHyperOS200) {
                        "controlCenterCarrierLayout"
                    } else {
                        "controlCenterCarrierView"
                    }
                )

                val positionView = if (isVerticalMode(dateView.context)) {
                    dateView
                } else {
                    carrierView
                }

                shortcutLayout.translationX = positionView.translationX
                shortcutLayout.translationY = positionView.translationY

                actionLayout.translationX = carrierView.translationX
                actionLayout.translationY = carrierView.translationY
            }

        startFolmeAnimationAlpha.javaClass
        startFolmeAnimationTranslationX.javaClass
        controlCenterHeaderExpandController.afterHookedFirstConstructor { param ->
            val controller = param.nonNullThisObject
            controller.setAdditionalInstanceField("normalControlShortcutLayoutTranslationX", 0)
            hookCallback(controller, controller.getObjectFieldAs<Any>("controlCenterCallback")::class.java)
        }

        controlCenterHeaderExpandController.afterHookedMethod("updateLocation") { param ->
            val controller = param.nonNullThisObject
            val context = controller.getObjectFieldAs<Context>("context")
            val headerControlOffset = controller.getObjectFieldAs<Int>("headerControlOffset")
            val statusIcons = controller.chainGetObjectAs<View>("headerController.get().controlCenterStatusIcons")

            val isRTL = SystemUtils.isRTL()
            val positionViewLocation = controller.getObjectFieldAs<IntArray>(
                if (isVerticalMode(context)) {
                    "dateViewLocation"
                } else {
                    "carrierLocation"
                }
            )

            val statusIconsLocation = controller.getObjectFieldAs<IntArray>("normalControlStatusIconsLocation")
            val statusIconsWidth = if (isRTL) {
                statusIconsLocation[0] + statusIcons.width
            } else {
                statusIconsLocation[0]
            }

            val normalControlShortcutLayoutWidth = if (isRTL) {
                positionViewLocation[0]
            } else {
                positionViewLocation[0] + shortcutLayout.width
            }
            val i = statusIconsWidth - normalControlShortcutLayoutWidth
            val abs = abs(i)
            var normalControlShortcutLayoutTranslationX = if (abs <= headerControlOffset) {
                abs
            } else {
                headerControlOffset
            }

            controller.setAdditionalInstanceField(
                "normalControlShortcutLayoutTranslationX",
                normalControlShortcutLayoutTranslationX
            )
        }
    }

    private fun addButtons(headerView: ViewGroup) {
        val context = headerView.context

        val eachOnLayout = { layout: LinearLayout, buttonGroup: String? ->
            buttonGroup?.split(",")?.forEachIndexed { i, slug ->
                val buttonData = buttonMap[slug] ?: return@forEachIndexed
                layout.addView(
                    ImageView(context).apply {
                        id = buttonData.id
                        layoutParams = ViewGroup.MarginLayoutParams(buttonSize, buttonSize).apply {
                            if (i > 0) {
                                marginStart = buttonSpacing
                            }
                        }
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageDrawable(buttonData.styleIcon)
                        setOnClickListener(buttonData.onClickListener)
                    }
                )
            }
        }

        shortcutLayout = LinearLayout(context).apply {
            id = R.id.control_center_header_button_shortcut_layout

            eachOnLayout(this, leftButtonGroup)
        }

        actionLayout = LinearLayout(context).apply {
            id = R.id.control_center_header_button_action_layout
            gravity = Gravity.CENTER_VERTICAL
            if (!isHideHeaderCarrier) {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    context.getDimensionPixelSize("status_bar_height")
                )
            }

            eachOnLayout(this, rightButtonGroup)
        }

        headerView.run {
            addView(shortcutLayout)
            addView(actionLayout)
        }
    }

    private fun hookCallback(expandController: Any, callbackClass: Class<*>) {
        callbackClass.afterHookedMethodByName("onAppearanceChanged") {
            val newAppearance = it.argAs<Boolean>()
            val animate = it.argAs<Boolean>(1)
            val translationX = expandController.getAdditionalInstanceFieldAs<Int>(
                "normalControlShortcutLayoutTranslationX"
            )

            val startFolmeAnimationAlpha = { view: View?, folme: Any? ->
                if (startFolmeAnimationAlpha.isStatic) {
                    val alpha = if (newAppearance) {
                        1F
                    } else {
                        0F
                    }

                    val args = if (startFolmeAnimationAlpha.parameterCount == 5) {
                        arrayOf(expandController, view, folme, alpha, animate)
                    } else {
                        arrayOf(view, folme, alpha, animate)
                    }

                    startFolmeAnimationAlpha.invoke(null, *args)
                }
            }

            if (startFolmeAnimationTranslationX.isStatic) {
                val tx = if (newAppearance) {
                    0
                } else {
                    translationX
                }

                val args = if (startFolmeAnimationTranslationX.parameterCount == 5) {
                    arrayOf(expandController, shortcutLayout, shortcutLayoutFolme, tx, animate)
                } else {
                    arrayOf(shortcutLayout, shortcutLayoutFolme, tx, animate)
                }

                startFolmeAnimationTranslationX.invoke(null, *args)
            }

            startFolmeAnimationAlpha(shortcutLayout, shortcutLayoutFolme)
            startFolmeAnimationAlpha(actionLayout, actionLayoutFolme)
        }

        callbackClass.afterHookedMethodByName("onExpansionChanged") {
            val headerController = expandController.chainGetObjectAs<Any>("headerController.get()")
            val dateView = headerController.getObjectFieldAs<View>("controlCenterDateView")
            val carrierView = headerController.getObjectFieldAs<View>(
                if (isMoreHyperOS200) {
                    "controlCenterCarrierLayout"
                } else {
                    "controlCenterCarrierView"
                }
            )
            val positionView = if (isVerticalMode(dateView.context)) {
                dateView
            } else {
                carrierView
            }

            shortcutLayout.translationX = positionView.translationX
            shortcutLayout.translationY = positionView.translationY

            actionLayout.translationX = carrierView.translationX
            actionLayout.translationY = carrierView.translationY
        }
    }

    // 左按钮组
    private val leftButtonGroup by lazy {
        prefs.getNonNullString(
            "systemui_control_center_header_left_button_group",
            ControlCenterHeaderFragment.STRING_INITIAL_LEFT
        )
    }

    // 左按钮组额外下边距
    private val leftButtonGroupBottomMargin by lazy {
        dp2px(prefs.getInt("systemui_control_center_header_left_button_group_bottom_margin"))
    }

    // 左按钮组在横屏下显示
    private val isLeftButtonGroupShowOnLandscape by lazy {
        prefs.getBoolean("systemui_control_center_header_left_button_group_show_on_landscape")
    }

    // 右按钮组
    private val rightButtonGroup by lazy {
        prefs.getNonNullString(
            "systemui_control_center_header_right_button_group",
            ControlCenterHeaderFragment.STRING_INITIAL_RIGHT
        )
    }

    // 右按钮组额外下边距
    private val rightButtonGroupBottomMargin by lazy {
        dp2px(prefs.getInt("systemui_control_center_header_right_button_group_bottom_margin"))
    }

    // 按钮组自适应
    private val isButtonGroupAdaptive by lazy {
        prefs.getBoolean("systemui_control_center_header_button_group_adaptive_enabled")
    }

    // 按钮大小
    private val buttonSize by lazy {
        dp2px(prefs.getInt("systemui_control_center_header_button_size", 24))
    }

    // 按钮间隔
    private val buttonSpacing by lazy {
        dp2px(prefs.getInt("systemui_control_center_header_button_spacing", 5))
    }

    private val buttonMap by lazy {
        mapOf(
            "settings" to ButtonData(getThemeIcon("settings")) {
                MiuiDependency.hapticFeedBack.clickFeedback()
                MiuiStub.sysUIProvider.activityStarter.startActivity(
                    Intent().apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setClassName("com.android.settings", "com.android.settings.MainSettings")
                    },
                    true
                )
            },
            "smart_home" to ButtonData(getThemeIcon("smart_home")).apply {
                id = ID_SMART_HOME
            },
            "xiaomi_smart_hub" to ButtonData(getThemeIcon("xiaomi_smart_hub")) {
                MiuiDependency.hapticFeedBack.clickFeedback()
                MiuiStub.sysUIProvider.activityStarter.startActivity(
                    Intent("com.milink.service.deviceworld").apply {
                        addCategory("android.intent.category.DEFAULT")
                        putExtra("ref", "control_center")
                    },
                    false
                )
            },

            "power_menu" to ButtonData(getThemeIcon("power_menu")) {
                MiuiDependency.hapticFeedBack.clickFeedback()

                WindowManagerGlobal.getWindowManagerService().showGlobalActions()
            },
            "tile_wordless_mode" to ButtonData(getThemeIcon("tile_wordless_mode")) {
                MiuiDependency.hapticFeedBack.clickFeedback()

                Settings.Secure.putInt(
                    it.context.contentResolver,
                    "wordless_mode",
                    Settings.Secure.getInt(it.context.contentResolver, "wordless_mode").reBooleanInt()
                )
            },
            "tile_edit_mode" to ButtonData(getThemeIcon("tile_edit_mode")).apply {
                id = ID_EDIT
            }
        )
    }

    private val controlCenterHeaderExpandController by lazy {
        loadClass("com.android.systemui.controlcenter.shade.ControlCenterHeaderExpandController")
    }

    private val startFolmeAnimationAlpha by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(startFolmeAnimationAlpha)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(controlCenterHeaderExpandController)
                    name("startFolmeAnimationAlpha", StringMatchType.Contains)
                }
            }.singleOrNull()
        }
    }

    private val startFolmeAnimationTranslationX by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(startFolmeAnimationTranslationX)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(controlCenterHeaderExpandController)
                    name("startFolmeAnimationTranslationX", StringMatchType.Contains)
                }
            }.singleOrNull()
        }
    }

    companion object {
        val ID_EDIT = R.id.control_center_header_button_edit
        val ID_SMART_HOME = R.id.control_center_header_button_smart_home
    }

    private class ButtonData(val styleIcon: Drawable?, val onClickListener: View.OnClickListener? = null) {
        var id = View.NO_ID
    }
}
