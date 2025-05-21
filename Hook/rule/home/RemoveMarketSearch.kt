package com.xzakota.oshape.startup.rule.home

import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.oshape.startup.base.BaseHook

object RemoveMarketSearch : BaseHook() {
    override fun onHookLoad() {
        loadClass("com.miui.home.launcher.allapps.AllAppsGridAdapter").replaceMethodByName("setLastSearchQuery", null)
    }
}
