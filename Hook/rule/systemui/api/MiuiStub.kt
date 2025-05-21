package com.xzakota.oshape.startup.rule.systemui.api

import android.content.Context
import android.os.Handler
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStaticObjectFieldAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass
import java.util.concurrent.Executor

object MiuiStub {
    private val INSTANCE by lazy {
        loadClass("miui.stub.MiuiStub").getStaticObjectFieldAs<Any>("INSTANCE")
    }

    lateinit var javaAdapter: JavaAdapter

    val baseProvider by lazy {
        BaseProvider(INSTANCE.getObjectFieldAs("mBaseProvider"))
    }

    val miuiModuleProvider by lazy {
        MiuiModuleProvider(INSTANCE.getObjectFieldAs("mMiuiModuleProvider"))
    }

    val sysUIProvider by lazy {
        SysUIProvider(INSTANCE.getObjectFieldAs("mSysUIProvider"))
    }

    fun createHook() {
        runCatching {
            loadClass("com.android.systemui.util.kotlin.JavaAdapter").afterHookedFirstConstructor {
                javaAdapter = JavaAdapter(it.nonNullThisObject)
            }
        }
    }

    override fun toString(): String = INSTANCE.toString()

    class BaseProvider(instance: Any) : BaseReflectObject(instance) {
        val mainHandler by lazy {
            instance.getObjectFieldAs<Handler>("mMainHandler")
        }

        val bgHandler by lazy {
            instance.getObjectFieldAs<Handler>("mBgHandler")
        }

        val context by lazy {
            instance.getObjectFieldAs<Context>("mContext")
        }

        val uiBackgroundExecutor by lazy {
            instance.getObjectFieldAs<Executor>("mUiBackgroundExecutor")
        }
    }

    class MiuiModuleProvider(instance: Any) : BaseReflectObject(instance) {
        val keyguardClockInjector by lazy {
            KeyguardClockInjector(instance.chainGetObjectAs("mKeyguardClockInjector.get()"))
        }

        val keyguardStateController by lazy {
            KeyguardStateController(instance.chainGetObjectAs("mKeyguardStateController.get()"))
        }
    }

    class SysUIProvider(instance: Any) : BaseReflectObject(instance) {
        val flashlightController by lazy {
            FlashlightController(instance.chainGetObjectAs("mFlashlightController.get()"))
        }

        val activityStarter by lazy {
            ActivityStarter(instance.chainGetObjectAs("mActivityStarter.get()"))
        }

        val selectedUserInteractor by lazy {
            SelectedUserInteractor(instance.chainGetObjectAs("mSelectedUserInteractor.get()"))
        }

        val pluginManager by lazy {
            PluginManager(instance.chainGetObjectAs("mPluginManager.get()"))
        }
    }
}
