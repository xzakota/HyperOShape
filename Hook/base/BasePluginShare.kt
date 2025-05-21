package com.xzakota.oshape.startup.base

import androidx.annotation.CallSuper

abstract class BasePluginShare : BaseShare() {
    protected var classLoader: ClassLoader? = null

    @CallSuper
    open fun init(cl: ClassLoader) {
        if (classLoader == null || classLoader != cl) {
            classLoader = cl
        }
    }

    override fun safeClassLoader(): ClassLoader? = classLoader
}
