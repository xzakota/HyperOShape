package com.xzakota.oshape.startup.rule.systemui.api

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.CallSuper
import com.xzakota.android.hook.core.loader.base.IMemberHookHandler
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.getAdditionalInstanceField
import com.xzakota.android.hook.extension.hookMethod
import com.xzakota.android.hook.extension.removeAdditionalInstanceField
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.log.Logger
import com.xzakota.code.util.reflect.InvokeUtils.loadClass
import com.xzakota.code.util.reflect.InvokeUtils.loadClassOrNull
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

class PluginManager(instance: Any) : BaseReflectObject(instance)

open class Plugin(instance: Any) : BaseReflectObject(instance) {
    fun getVersion(): Int = instance.callMethodAs("getVersion")
}

class PluginFragment(instance: Any) : Plugin(instance)

class PluginInstance(instance: Any) : BaseReflectObject(instance) {
    // systemUIContext
    val appContext by lazy {
        WeakReference(instance.getObjectFieldAs<Context>("mAppContext"))
    }

    val componentName by lazy {
        instance.getObjectFieldAs<ComponentName>("mComponentName")
    }

    val pluginContext: WeakReference<Context>? by lazy {
        WeakReference(instance.getObjectFieldAs<Context>("mPluginContext"))
    }

    val plugin: Plugin?
        get() = instance.getObjectField("mPlugin")?.let {
            Plugin(it)
        }

    val pluginFactory by lazy {
        PluginFactory(instance.getObjectFieldAs("mPluginFactory"))
    }

    fun loadClass(className: String): Class<*> = loadClass(className, pluginFactory.classLoader)
    fun loadClassOrNull(className: String): Class<*>? = loadClassOrNull(className, pluginFactory.classLoader)

    override fun getInstanceClass(): String = PLUGIN_INSTANCE

    companion object {
        const val PLUGIN_INSTANCE = "com.android.systemui.shared.plugins.PluginInstance"
        const val PLUGIN_COMPONENT_MIUI_SYSTEMUI = "miui.systemui.plugin"
        const val PLUGIN_COMPONENT_MIUI_AOD = "com.miui.aod"

        const val PLUGIN_FOCUS_NOTIFICATION = "miui.systemui.notification.FocusNotificationPluginImpl"
        const val PLUGIN_CONTROL_CENTER = "miui.systemui.controlcenter.MiuiControlCenter"
        const val PLUGIN_GLOBAL_ACTIONS = "miui.systemui.globalactions.GlobalActionsPlugin"
        const val PLUGIN_LOCAL_MIUI_TILE = "miui.systemui.quicksettings.LocalMiuiQSTilePlugin"
        const val PLUGIN_VOLUME_DIALOG = "miui.systemui.volume.VolumeDialogPlugin"

        const val PLUGIN_SHORTCUT = "com.miui.keyguard.shortcuts.ShortcutPluginImpl"

        private var isHooked = AtomicBoolean(false)
        private val listeners = mutableListOf<PluginListener>()

        fun miuiSystemUI(className: String): ComponentName = ComponentName(PLUGIN_COMPONENT_MIUI_SYSTEMUI, className)
        fun miuiAod(className: String): ComponentName = ComponentName(PLUGIN_COMPONENT_MIUI_AOD, className)

        fun addListener(listener: PluginListener) {
            listeners += listener

            if (isHooked.getAndSet(true)) {
                hookDispatchListeners()
            }
        }

        fun removeListener(listener: PluginListener) {
            listeners -= listener
        }

        @Synchronized
        private fun hookDispatchListeners() {
            val pluginInstanceClass = loadClass(PLUGIN_INSTANCE)
            pluginInstanceClass.hookMethod("loadPlugin") {
                var needDispatch = true
                var pluginInstance: PluginInstance? = null
                var pluginFactory: Any? = null
                var hookedMethod: IMemberHookHandler? = null

                before @Synchronized { param ->
                    pluginInstance = PluginInstance(param.nonNullThisObject)
                    if (pluginInstance.plugin != null) {
                        needDispatch = false
                        return@before
                    }

                    pluginFactory = pluginInstance.pluginFactory.instance
                    pluginFactory.setAdditionalInstanceField("pluginInstance", pluginInstance)
                    hookedMethod = pluginFactory::class.java.afterHookedMethodByName("createPluginContext") {
                        if (it.nonNullThisObject.getAdditionalInstanceField("pluginInstance") == pluginInstance) {
                            val context = it.resultAs<Context?>()
                            val plugin = pluginInstance.plugin
                            if (context == null || plugin == null) {
                                return@afterHookedMethodByName
                            }

                            listeners.forEach { listener ->
                                if (listener.componentName != pluginInstance.componentName) {
                                    return@forEach
                                }

                                Logger.d("onPluginBeforeLoad $plugin", pluginInstance.componentName.packageName)
                                listener.onPluginBeforeLoad(plugin, context, pluginInstance)
                            }
                        }
                    }
                }

                after @Synchronized {
                    if (!needDispatch || pluginInstance == null) {
                        return@after
                    }

                    hookedMethod?.unhook()
                    pluginFactory?.removeAdditionalInstanceField("pluginInstance")
                }
            }

            // 在 200 版本官方写法问题导致 unloadPlugin 重复执行，待优化
            pluginInstanceClass.beforeHookedMethod("unloadPlugin") @Synchronized { param ->
                val pluginInstance = PluginInstance(param.nonNullThisObject)
                val plugin = pluginInstance.plugin ?: return@beforeHookedMethod

                listeners.forEach { listener ->
                    if (listener.componentName != pluginInstance.componentName) {
                        return@forEach
                    }

                    Logger.d("onPluginUnloaded $plugin", pluginInstance.componentName.packageName)
                    listener.onPluginUnloaded(plugin, pluginInstance)

                    if (listener.isOnce) {
                        listeners -= listener
                    }
                }
            }
        }
    }

    inner class PluginFactory(instance: Any) : BaseReflectObject(instance) {
        val pluginClass by lazy {
            instance.getObjectFieldAs<Class<*>>("mPluginClass")
        }

        val appInfo by lazy {
            instance.getObjectFieldAs<ApplicationInfo>("mAppInfo")
        }

        val componentName by lazy {
            instance.getObjectFieldAs<Any>("mComponentName")
        }

        val classLoader by lazy {
            runCatching {
                instance.getObjectFieldAs<Supplier<ClassLoader>>("mClassLoaderFactory").get()
            }.getOrElse {
                instance.chainGetObjectAs("mClassLoaderFactory.get()")
            }
        }
    }
}

abstract class PluginListener(val componentName: ComponentName, val isOnce: Boolean = false) {
    private val hookedMethods = mutableListOf<IMemberHookHandler>()

    abstract fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance)

    @CallSuper
    open fun onPluginUnloaded(plugin: Plugin, pluginInstance: PluginInstance) {
        unHookAllMethods()
    }

    fun addHook(hook: IMemberHookHandler) {
        hookedMethods.add(hook)
    }

    private fun unHookAllMethods() {
        val iterator = hookedMethods.iterator()
        while (iterator.hasNext()) {
            iterator.next().unhook()
            iterator.remove()
        }
    }
}
