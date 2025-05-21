package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.code.extension.setBooleanField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.fixClock
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiNotificationPanelViewController
import com.xzakota.oshape.util.DexKit
import java.lang.reflect.Method

object FixClock : BaseHook() {
    private val isSimplified = fixClock != 1

    override fun onHookLoad() {
        clockAnimationHelper.beforeHookedMethodByName("doAnimationToNotification") { param ->
            param.args[0] = isSimplified
            param.nonNullThisObject.setBooleanField("mHasNotification", isSimplified)
        }

        clockAnimationHelper.beforeHookedMethodByName("doAnimationToAod") { param ->
            param.args[1] = isSimplified
        }

        updateNotificationState.createAfterHook {
            MiuiStub.miuiModuleProvider
                .keyguardClockInjector
                .keyguardClockView
                ?.setBooleanField("mHasNotification", isSimplified)
        }
    }

    private val clockAnimationHelper by lazy {
        loadClass("com.android.keyguard.clock.animation.AnimationHelper")
    }

    private val updateNotificationState by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(updateNotificationState)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(miuiNotificationPanelViewController)
                    addInvoke {
                        name = "getVisibleNotificationCount"
                    }
                    addInvoke {
                        name = "updateNotification"
                    }
                }
            }.singleOrNull()
        }
    }
}
