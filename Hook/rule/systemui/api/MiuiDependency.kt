package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.extension.chainGetObject
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callStaticMethod
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStaticObjectField
import com.xzakota.code.extension.getStaticObjectFieldAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

@Suppress("unused")
object MiuiDependency {
    private val DEPENDENCY by lazy {
        loadClass("com.miui.systemui.MiuiDependency")
    }

    @JvmStatic
    val dependency: Any
        get() = DEPENDENCY.getStaticObjectFieldAs("sDependency")

    @JvmStatic
    val providers: Map<*, *>
        get() = dependency.getObjectFieldAs("mProviders")

    @JvmStatic
    val dependencies: Map<*, *>
        get() = dependency.getObjectFieldAs("mDependencies")

    @JvmStatic
    val hapticFeedBack by lazy {
        HapticFeedBack(dependency.chainGetObjectAs("mHapticFeedBack.get()"))
    }

    @JvmStatic
    fun get(depClz: Class<*>): Any? = DEPENDENCY.callStaticMethod("get", depClz)

    @JvmStatic
    fun createDependency(obj: Any): Any? = dependency.callMethod("createDependency", obj)
}
