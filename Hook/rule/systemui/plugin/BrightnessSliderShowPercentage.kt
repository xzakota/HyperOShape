package com.xzakota.oshape.startup.rule.systemui.plugin

import android.annotation.SuppressLint
import android.widget.FrameLayout
import android.widget.TextView
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import kotlin.math.roundToInt

class BrightnessSliderShowPercentage(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BaseSliderShowPercentage(pluginInstance, listener) {
    private var fromLeft = 0
    private var fromTop = 0
    private var fromWidth = 0
    private var fromHeight = 0

    private var toLeft = 0
    private var toTop = 0
    private var toWidth = 0
    private var toHeight = 0

    @SuppressLint("SetTextI18n")
    override fun onHookLoad() {
        super.onHookLoad()

        addHook(panelController.beforeHookedMethod("onCreate") { param ->
            val controller = param.nonNullThisObject
            val brightnessPanel = controller.getObjectFieldAs<FrameLayout>("brightnessPanel")
            val topText = brightnessPanel.findViewByIdNameAs<TextView>("top_text")
            initTopText(topText)
        })

        addHook(panelController.afterHookedMethod("updateBackgroundBlurMode") { param ->
            val controller = param.nonNullThisObject
            val brightnessPanel = controller.getObjectFieldAs<FrameLayout>("brightnessPanel")
            val topText = brightnessPanel.findViewByIdNameAs<TextView>("top_text")
            updateTopTextBlendColor(topText)
        })

        addHook(sliderController.afterHookedMethodByName("updateIconProgress") { param ->
            val controller = param.nonNullThisObject
            val slider = controller.callMethod("getSlider") ?: return@afterHookedMethodByName
            controller.chainGetObjectAs<TextView?>("getSliderHolder().getTopText()")?.text = "${calPercentage(slider)}%"
        })

        addHook(panelSliderController.afterHookedMethod("updateIconProgress") { param ->
            val controller = param.nonNullThisObject
            val slider = controller.callMethod("getVSlider") ?: return@afterHookedMethod
            val brightnessPanel = controller.getObjectFieldAs<FrameLayout>("brightnessPanel")
            val topText = brightnessPanel.findViewByIdNameAs<TextView>("top_text")
            topText.text = "${calPercentage(slider)}%"
        })

        addHook(panelAnimator.afterHookedMethod("calculateViewValues") { param ->
            val animator = param.nonNullThisObject

            val fromView = animator.getObjectFieldAs<Any>("fromView")
            val fromTopText = fromView.callMethodAs<TextView>("getTopText")
            fromLeft = fromTopText.left
            fromTop = fromTopText.top
            fromWidth = fromTopText.width
            fromHeight = fromTopText.height

            val toView = animator.callMethodAs<Any>("getToView")
            val brightnessPanel = toView.getObjectFieldAs<FrameLayout>("brightnessPanel")
            val toTopText = brightnessPanel.findViewByIdNameAs<TextView>("top_text")
            toLeft = toTopText.left
            toTop = toTopText.top
            toWidth = toTopText.width
            toHeight = toTopText.height
        })

        addHook(panelAnimator.afterHookedMethod("frameCallback") { param ->
            val animator = param.nonNullThisObject

            val sizeSliderX = animator.getObjectFieldAs<Float>("sizeSliderX")
            val sizeSliderY = animator.getObjectFieldAs<Float>("sizeSliderY")
            val l = fromLeft + (toLeft - fromLeft) * sizeSliderX
            val t = fromTop + (toTop - fromTop) * sizeSliderY
            val w = fromWidth + (toWidth - fromWidth) * sizeSliderX
            val h = fromHeight + (toHeight - fromHeight) * sizeSliderY

            val sliderController = animator.getObjectFieldAs<Any>("sliderController")
            val brightnessPanel = sliderController.getObjectFieldAs<FrameLayout>("brightnessPanel")
            val topText = brightnessPanel.findViewByIdNameAs<TextView>("top_text")
            topText.setLeftTopRightBottom(
                l.roundToInt(), t.roundToInt(),
                (l + w).roundToInt(), (t + h).roundToInt()
            )
        })
    }

    override fun getPanelControllerClassName(): String = "$BRIGHTNESS_PACKAGE_PREFIX.BrightnessPanelController"

    override fun getSliderControllerClassName(): String = "$BRIGHTNESS_PACKAGE_PREFIX.BrightnessSliderController"

    override fun getPanelAnimatorClassName(): String = "$BRIGHTNESS_PACKAGE_PREFIX.BrightnessPanelAnimator"

    private val panelSliderController by lazy {
        loadClass(BRIGHTNESS_PANEL_SLIDER_CONTROLLER)
    }

    companion object {
        private const val BRIGHTNESS_PACKAGE_PREFIX = "miui.systemui.controlcenter.panel.main.brightness"

        const val BRIGHTNESS_PANEL_SLIDER_CONTROLLER = "$BRIGHTNESS_PACKAGE_PREFIX.BrightnessPanelSliderController"
    }
}
