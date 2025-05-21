package com.xzakota.oshape.startup.rule.systemui.shared

import com.xzakota.oshape.startup.base.BasePluginShare
import com.xzakota.oshape.startup.rule.systemui.api.PluginControlCenterUtils

object SystemUIPlugin : BasePluginShare() {
    override fun init(cl: ClassLoader) {
        super.init(cl)

        PluginControlCenterUtils.init(cl)
    }
}
