package com.xzakota.oshape.startup.rule.systemui.plugin

import com.xzakota.android.hook.extension.replaceMethod
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener

class HideRawCCEditButton(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    override fun onHookLoad() {
        addHook(loadClass("miui.systemui.controlcenter.panel.main.qs.EditButtonController").replaceMethod(
            "available", false, Boolean::class.java
        ))
    }
}
