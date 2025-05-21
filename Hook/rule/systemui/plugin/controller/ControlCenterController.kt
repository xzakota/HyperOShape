package com.xzakota.oshape.startup.rule.systemui.plugin.controller

import android.content.Context
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.Plugin
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_CONTROL_CENTER
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.plugin.BaseSliderShowPercentage
import com.xzakota.oshape.startup.rule.systemui.plugin.BrightnessSliderShowPercentage
import com.xzakota.oshape.startup.rule.systemui.plugin.HideRawCCEditButton
import com.xzakota.oshape.startup.rule.systemui.plugin.NewCCButtonEvent
import com.xzakota.oshape.startup.rule.systemui.plugin.VolumeSliderShowPercentage
import com.xzakota.oshape.startup.rule.systemui.shared.SystemUIPlugin

object ControlCenterController : BaseHook(false) {
    override fun onHookLoad() {
        PluginInstance.addListener(object : PluginListener(PluginInstance.miuiSystemUI(PLUGIN_CONTROL_CENTER)) {
            override fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance) {
                val classLoader = pluginInstance.pluginFactory.classLoader
                SystemUIPlugin.init(classLoader)

                depends(pluginInstance, this)
            }

            override fun onPluginUnloaded(plugin: Plugin, pluginInstance: PluginInstance) {
                super.onPluginUnloaded(plugin, pluginInstance)

                BaseSliderShowPercentage.isUpdatedBlendBlur.set(false)
            }
        })
    }

    private fun depends(pluginInstance: PluginInstance, listener: PluginListener) {
        dependHookIfEnabled(
            BrightnessSliderShowPercentage(pluginInstance, listener),
            "systemui_control_center_brightness_slider_show_percentage_enabled"
        )
        dependHookIfEnabled(
            VolumeSliderShowPercentage(pluginInstance, listener),
            "systemui_control_center_volume_slider_show_percentage_enabled"
        )
        dependHookIfEnabled(
            HideRawCCEditButton(pluginInstance, listener),
            "systemui_control_center_hide_raw_tile_edit_button_enabled"
        )
        dependHookIfEnabled(
            NewCCButtonEvent(pluginInstance, listener),
            "systemui_control_center_add_header_shortcut_buttons_enabled"
        )
    }
}
