package com.xzakota.oshape.startup.rule.home.api

import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.util.reflect.InvokeUtils

object HomeApplication {
    private val INSTANCE by lazy {
        InvokeUtils.loadClass("com.miui.home.launcher.Application").callStaticMethodAs<Any>("getInstance")
    }

    fun getIconCache(): Any = INSTANCE.callMethodAs("getIconCache")
}
