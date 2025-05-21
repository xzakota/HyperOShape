package com.xzakota.oshape.startup.rule.systemui.shared

import com.xzakota.oshape.startup.base.BasePluginShare
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_SHORTCUT

object AodPlugin : BasePluginShare() {
    lateinit var shortcutViewLayoutController : Class<*>
    lateinit var shortcutPluginImpl : Class<*>

    override fun init(cl: ClassLoader) {
        super.init(cl)

        shortcutViewLayoutController = loadClass("com.miui.keyguard.shortcuts.controller.ShortcutViewLayoutController")
        shortcutPluginImpl = loadClass(PLUGIN_SHORTCUT)
    }
}
