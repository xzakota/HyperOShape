package com.xzakota.oshape.startup.rule.home.shared

import com.xzakota.android.hook.startup.base.BaseShare
import com.xzakota.code.util.reflect.InvokeUtils

object HomeShared : BaseShare() {
    val deviceConfig by lazy {
        loadClass("com.miui.home.launcher.DeviceConfig")
    }

    val gridConfig by lazy {
        loadClass("com.miui.home.launcher.GridConfig")
    }

    val launcher by lazy {
        loadClass("com.miui.home.launcher.Launcher")
    }

    val fixedAspectRatioLottieAnimView by lazy {
        InvokeUtils.loadClass("com.miui.home.settings.FixedAspectRatioLottieAnimView")
    }
}
