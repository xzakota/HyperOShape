package com.xzakota.oshape.startup.rule.securitycenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.code.extension.callMethod
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.util.DexKit
import java.lang.reflect.Method

object SkipInterceptWait : BaseHook() {
    override fun onHookLoad() {
        when (prefs.getStringAsInt("security_center_skip_intercept_wait_method")) {
            0 -> loadClass("com.miui.permcenter.privacymanager.model.InterceptBaseActivity").beforeHookedMethod(
                "onCreate", Bundle::class.java
            ) { param ->
                val feat: Bundle.() -> Unit = {
                    putInt("KET_STEP_COUNT", 0)
                    putBoolean("KEY_ALLOW_ENABLE", true)
                }

                val bundle = param.argAs<Bundle?>()
                if (bundle == null) {
                    param.args[0] = Bundle().apply(feat)
                } else {
                    bundle.feat()
                }
            }

            1 -> {
                val interceptBaseFragment = loadClass("com.miui.permcenter.privacymanager.InterceptBaseFragment")
                val finishFragment = DexKit.findMemberOrLog<Method>(this, "$name(finishFragment)") { bridge ->
                    bridge.findMethod {
                        matcher {
                            declaredClass(interceptBaseFragment)
                            usingNumbers(-1, 0)
                        }
                    }.singleOrNull()
                }

                interceptBaseFragment.afterHookedMethod(
                    "onInflateView", LayoutInflater::class.java, ViewGroup::class.java, Bundle::class.java
                ) { param ->
                    param.nonNullThisObject.callMethod(finishFragment.name, true)
                }
            }
        }
    }
}
