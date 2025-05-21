package com.xzakota.oshape.startup.rule.home.api

import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object Utilities {
    private val UTILITIES by lazy {
        loadClass("com.miui.home.launcher.common.Utilities")
    }

    fun isSupportSuperXiaoai(): Boolean = UTILITIES.callStaticMethodAs("isSupportSuperXiaoai")
}
