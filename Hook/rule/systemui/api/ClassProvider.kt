package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object ClassProvider {
    val ACTIVITY_STARTER by lazy {
        loadClass("com.android.systemui.plugins.ActivityStarter")
    }

    val HAPTIC_FEED_BACK by lazy {
        loadClass("com.miui.interfaces.IHapticFeedBack")
    }
}
