package com.xzakota.oshape.startup.rule.systemui.plugin

import com.xzakota.android.hook.core.loader.base.IMemberHookHandler
import com.xzakota.android.hook.core.loader.base.IPackageLoadedParam
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener

abstract class BasePluginHook(
    protected val pluginInstance: PluginInstance,
    private val listener: PluginListener
) : BaseHook() {
    override fun load(param: IPackageLoadedParam) {
        val newParam = param.copy(
            pluginInstance.componentName.packageName,
            pluginInstance.pluginFactory.appInfo,
            pluginInstance.pluginFactory.classLoader
        )

        super.load(newParam)
    }

    protected fun addHook(hook: IMemberHookHandler) = listener.addHook(hook)
}
