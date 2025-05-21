package com.xzakota.oshape.startup.rule.systemui.plugin.aod

import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.plugin.BasePluginHook
import com.xzakota.oshape.startup.rule.systemui.shared.AodPlugin.shortcutViewLayoutController

class RemoveKeyguardShortcutTips200(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    override fun onHookLoad() {
        addHook(shortcutViewLayoutController.replaceMethodByName("updateAffordanceViewTipsLayoutParams"))
        addHook(shortcutViewLayoutController.replaceMethodByName("showShortcutTipsAnim"))
        addHook(shortcutViewLayoutController.replaceMethodByName("onTipsAnimationEnd"))
    }
}
