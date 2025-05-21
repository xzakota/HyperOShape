package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.extension.chainGetObject
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callStaticMethod
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStaticObjectField
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object Dependency {
    private val DEPENDENCY by lazy {
        loadClass("com.android.systemui.Dependency")
    }

    /* ========================== only for HyperOS2 ========================== */
    @JvmStatic
    val dependency: Any?
        get() = DEPENDENCY.getStaticObjectField("sDependency")

    @JvmStatic
    val dependencies: Map<*, *>?
        get() = dependency?.getObjectFieldAs("mDependencies")

    @JvmStatic
    val providers: Map<*, *>?
        get() = dependency?.getObjectFieldAs("mProviders")

    @JvmStatic
    val miuiLegacyDependency: Any?
        get() = dependency?.getObjectField("mMiuiLegacyDependency")

    @JvmStatic
    val volumeDialogController by lazy {
        dependency?.chainGetObject("mVolumeDialogController.get()")?.let {
            VolumeDialogController(it)
        }
    }

    @JvmStatic
    val darkIconDispatcher by lazy {
        dependency?.chainGetObject("mDarkIconDispatcher.get()")?.let {
            DarkIconDispatcher(it)
        }
    }

    @JvmStatic
    fun getDependencyInner(depClz: Class<*>): Any? = dependency?.callMethod("getDependencyInner", depClz)

    @JvmStatic
    fun getDependencyInner(depClzName: String): Any? = getDependencyInner(loadClass(depClzName))

    /* ========================== only for HyperOS1 ========================== */
    @JvmStatic
    fun get(depClz: Class<*>): Any? = DEPENDENCY.callStaticMethod("get", depClz)
}
