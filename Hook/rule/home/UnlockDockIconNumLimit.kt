package com.xzakota.oshape.startup.rule.home

import com.xzakota.android.hook.extension.replaceMethod
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.deviceConfig

object UnlockDockIconNumLimit : BaseHook() {
    override fun onHookLoad() {
        deviceConfig.replaceMethod("getHotseatMaxCount", 20)
    }
}
