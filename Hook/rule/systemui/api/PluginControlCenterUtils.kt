package com.xzakota.oshape.startup.rule.systemui.api

import android.content.Context
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object PluginControlCenterUtils {
    private lateinit var CONTROL_CENTER_UTILS : Class<*>

    fun init(cl: ClassLoader) {
        CONTROL_CENTER_UTILS = loadClass("miui.systemui.controlcenter.utils.ControlCenterUtils", cl)
    }

    fun getBackgroundBlurOpenedInDefaultTheme(
        context: Context
    ): Boolean = CONTROL_CENTER_UTILS.callStaticMethodAs("getBackgroundBlurOpenedInDefaultTheme", context)
}
