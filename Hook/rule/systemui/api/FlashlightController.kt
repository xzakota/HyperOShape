package com.xzakota.oshape.startup.rule.systemui.api

import android.app.ActivityOptions
import android.app.ActivityTaskManager
import android.content.Context
import android.content.Intent
import android.extension.content.getBasePackageName
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.android.util.KeyboardUtils
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callVoidMethod
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.util.reflect.InvokeUtils.loadClass
import miui.extension.content.addMiuiFlags
import java.util.concurrent.atomic.AtomicBoolean

class FlashlightController(instance: Any) : BaseReflectObject(instance) {
    val isBatteryOff by lazy {
        instance.getBooleanField("mBatteryOff")
    }

    fun isEnabled(): Boolean = instance.callMethodAs("isEnabled")

    fun isAvailable(): Boolean = instance.callMethodAs("isAvailable")

    fun supportFlashlightUIDisplay(): Boolean = runCatching {
        instance.callMethodAs<Boolean>("supportFlashlightUIDisplay")
    }.getOrDefault(false)

    fun setFlashlight(enabled: Boolean) = instance.callVoidMethod("setFlashlight", enabled)

    fun toggleFlashlight() = setFlashlight(!isEnabled())

    fun getShowFlashlightIntent(): Intent? = if (supportFlashlightUIDisplay() && isAvailable()) {
        Intent(ACTION_SHOW_FLASHLIGHT).apply {
            addMiuiFlags(8)
            putExtra("flashlight_enter_from", 0)
        }
    } else {
        null
    }

    fun showFlashlight(context: Context = MiuiStub.baseProvider.context, intent: Intent? = getShowFlashlightIntent()) {
        val splitIntent = runCatching {
            ControlCenterUtils.getSettingsSplitIntent(context, intent)
        }.getOrDefault(intent) ?: return
        val isKeyguardLocked = KeyboardUtils.isKeyguardLocked()

        if (isKeyguardLocked) {
            splitIntent.addFlags(
                splitIntent.flags and (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            ActivityTaskManager.getService().startActivityAsUser(
                null, context.getBasePackageName(), context.attributionTag, splitIntent,
                splitIntent.resolveTypeIfNeeded(context.contentResolver), null, null, 0,
                Intent.FLAG_ACTIVITY_NEW_TASK, null, ActivityOptions.makeBasic().toBundle(),
                MiuiStub.sysUIProvider.selectedUserInteractor.getSelectedUserId()
            )
        } else {
            MiuiStub.sysUIProvider.activityStarter.postStartActivityDismissingKeyguard(splitIntent, 0, null)
        }
    }

    companion object {
        const val ACTION_SHOW_FLASHLIGHT = "miui.systemui.action.ACTION_SHOW_FLASHLIGHT"

        private var isHooked = AtomicBoolean(false)
        private val listeners = mutableListOf<FlashlightListener>()

        fun addListener(listener: FlashlightListener) {
            listeners += listener
            hookDispatchListeners()
        }

        fun removeListener(listener: FlashlightListener) {
            listeners -= listener
        }

        @Synchronized
        private fun hookDispatchListeners() {
            if (isHooked.getAndSet(true)) {
                return
            }

            loadClass("com.android.systemui.controlcenter.policy.MiuiFlashlightControllerImpl").afterHookedMethod(
                "dispatchListeners", Int::class.java, Boolean::class.java
            ) { param ->
                synchronized(listeners) {
                    val event = param.argAs<Int>()
                    val isEnabled = param.argAs<Boolean>(1)

                    listeners.forEach {
                        when (event) {
                            0 -> it.onFlashlightError()
                            1 -> it.onFlashlightChanged(isEnabled)
                            2 -> it.onFlashlightAvailabilityChanged(isEnabled)
                        }
                    }
                }
            }
        }
    }

    interface FlashlightListener {
        fun onFlashlightError() {}
        fun onFlashlightChanged(isEnabled: Boolean)
        fun onFlashlightAvailabilityChanged(isEnabled: Boolean) {}
    }
}
