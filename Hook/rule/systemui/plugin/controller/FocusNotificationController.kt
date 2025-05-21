package com.xzakota.oshape.startup.rule.systemui.plugin.controller

import android.content.Context
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.Plugin
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_FOCUS_NOTIFICATION
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.plugin.FocusNotificationWhitelist
import com.xzakota.oshape.startup.rule.systemui.shared.Notification

object FocusNotificationController : BaseHook(false) {
    override fun onHookLoad() {
        PluginInstance.addListener(object : PluginListener(PluginInstance.miuiSystemUI(PLUGIN_FOCUS_NOTIFICATION)) {
            override fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance) {
                dependHook(FocusNotificationWhitelist(pluginInstance, this))
            }
        })
    }
}
