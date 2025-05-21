package com.xzakota.oshape.startup.rule.systemui.plugin.controller

import android.content.Context
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.Plugin
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_SHORTCUT
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.plugin.aod.CustomizeBottomShortcut200
import com.xzakota.oshape.startup.rule.systemui.plugin.aod.RemoveKeyguardShortcutTips200
import com.xzakota.oshape.startup.rule.systemui.shared.AodPlugin
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.isCustomizeBottomButton
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.isRemoveButtonTips

object KeyguardShortcutController : BaseHook(false) {
    override fun onHookLoad() {
        PluginInstance.addListener(object : PluginListener(PluginInstance.miuiAod(PLUGIN_SHORTCUT)) {
            override fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance) {
                val classLoader = pluginInstance.pluginFactory.classLoader
                AodPlugin.init(classLoader)

                depends(pluginInstance, this)
            }
        })
    }

    private fun depends(pluginInstance: PluginInstance, listener: PluginListener) {
        dependHook(RemoveKeyguardShortcutTips200(pluginInstance, listener), isRemoveButtonTips)
        dependHook(CustomizeBottomShortcut200(pluginInstance, listener), isCustomizeBottomButton)
    }
}
