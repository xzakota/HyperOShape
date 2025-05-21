package com.xzakota.oshape.startup.rule.systemui.plugin

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.xzakota.android.extension.content.getColorStateList
import com.xzakota.android.extension.content.getIntArray
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.util.MathUtils
import com.xzakota.oshape.startup.rule.systemui.api.PluginControlCenterUtils
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import miui.extension.view.clearMiAllBlurAndBlendColor
import miui.extension.view.getMiViewBlurMode
import miui.extension.view.setMiBackgroundBlendColors
import miui.extension.view.setMiViewBlurMode
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseSliderShowPercentage(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    @SuppressLint("SetTextI18n")
    override fun onHookLoad() {
        addHook(sliderController.afterHookedMethod("onBindViewHolder") { param ->
            val holder = param.nonNullThisObject.callMethod("getSliderHolder") ?: return@afterHookedMethod
            val topText = holder.callMethodAs<TextView>("getTopText")
            initTopText(topText)
        })

        updateBlendBlur()
    }

    protected fun updateTopTextBlendColor(topText: TextView) {
        val context = topText.context
        if (PluginControlCenterUtils.getBackgroundBlurOpenedInDefaultTheme(context)) {
            if (topText.getMiViewBlurMode() == 3) {
                return
            }

            topText.clearMiAllBlurAndBlendColor()
            topText.setTextColor(context.getColorStateList("toggle_slider_icon_color"))
            topText.setMiViewBlurMode(3)
            topText.setMiBackgroundBlendColors(context.getIntArray("toggle_slider_icon_blend_colors"), 1F)
        } else {
            // miui_super_volume_expanded toggle_slider_top_text_color
            topText.setTextColor(Color.argb(0xFF, 0x99, 0x99, 0x99))
            topText.clearMiAllBlurAndBlendColor()
        }
    }

    protected fun initTopText(topText: TextView) {
        topText.post {
            topText.visibility = View.VISIBLE
            topText.typeface = Typeface.DEFAULT_BOLD
            if (topText.text == "200%") {
                topText.text = "0%"
            }
        }
    }

    protected fun calPercentage(slider: Any): Int {
        slider as SeekBar

        val minValue = slider.min
        val maxValue = slider.max
        val currentValue = runCatching {
            slider.callMethodAs<Int>("getTargetValue")
        }.getOrElse {
            runCatching {
                slider.callMethodAs<Int>("getValue")
            }.getOrDefault(slider.progress)
        }

        return MathUtils.calIntPercentage(currentValue, maxValue, minValue)
    }

    @Synchronized
    private fun updateBlendBlur() {
        if (isUpdatedBlendBlur.getAndSet(true)) {
            return
        }

        addHook(toggleSliderViewHolder.afterHookedMethod("updateBlendBlur") { param ->
            updateTopTextBlendColor(param.nonNullThisObject.callMethodAs<TextView>("getTopText"))
        })
    }

    protected val panelController by lazy {
        loadClass(getPanelControllerClassName())
    }

    protected val sliderController by lazy {
        loadClass(getSliderControllerClassName())
    }

    protected val panelAnimator by lazy {
        loadClass(getPanelAnimatorClassName())
    }

    protected val toggleSliderViewHolder by lazy {
        loadClass("miui.systemui.controlcenter.panel.main.recyclerview.ToggleSliderViewHolder")
    }

    protected abstract fun getPanelControllerClassName(): String
    protected abstract fun getSliderControllerClassName(): String
    protected abstract fun getPanelAnimatorClassName(): String

    companion object {
        var isUpdatedBlendBlur = AtomicBoolean(false)
    }
}
