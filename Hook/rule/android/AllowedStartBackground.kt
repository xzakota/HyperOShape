package com.xzakota.oshape.startup.rule.android

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.core.loader.base.IMemberHooker
import com.xzakota.android.util.SystemUtils.isMoreAndroidVersion
import com.xzakota.code.extension.getStaticObjectField
import com.xzakota.oshape.startup.base.BaseHook

/**
 * copy from [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)
 */
object AllowedStartBackground : BaseHook() {
    @JvmStatic
    var ALLOWED_APP_ID = ""

    override fun onHookLoad() {
        if (ALLOWED_APP_ID.isEmpty()) {
            return
        }

        logI("Allow package: $ALLOWED_APP_ID")

        try {
            hookMethod(
                "com.android.server.wm.ActivityStarter", loadedParam.classLoader,
                "shouldAbortBackgroundActivityStart",
                Int::class.java, Int::class.java, String::class.java, Int::class.java, Int::class.java,
                "com.android.server.wm.WindowProcessController", "com.android.server.am.PendingIntentRecord",
                Boolean::class.java, Intent::class.java, ActivityOptions::class.java,
                object : IMemberHooker() {
                    override fun before(param: IMemberHookParam) {
                        val packageName = param.argAs<String?>(2) ?: return
                        if (packageName == ALLOWED_APP_ID) {
                            param.result = false
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            if (isMoreAndroidVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM)) {
                hookMethod(
                    "com.android.server.wm.BackgroundActivityStartController", loadedParam.classLoader,
                    "checkBackgroundActivityStart",
                    Int::class.java, Int::class.java, String::class.java, Int::class.java, Int::class.java,
                    "com.android.server.wm.WindowProcessController", "com.android.server.am.PendingIntentRecord",
                    "android.app.BackgroundStartPrivileges", "com.android.server.wm.ActivityRecord",
                    Intent::class.java, ActivityOptions::class.java,
                    object : IMemberHooker() {
                        override fun before(param: IMemberHookParam) {
                            val packageName = param.argAs<String?>(2) ?: return
                            if (packageName == ALLOWED_APP_ID) {
                                param.result = loadClass(
                                    "com.android.server.wm.BackgroundActivityStartController\$BalVerdict"
                                ).getStaticObjectField("ALLOW_BY_DEFAULT")
                            }
                        }
                    }
                )
            } else {
                hookMethod(
                    "com.android.server.wm.BackgroundActivityStartController", loadedParam.classLoader,
                    "checkBackgroundActivityStart",
                    Int::class.java, Int::class.java, String::class.java, Int::class.java, Int::class.java,
                    "com.android.server.wm.WindowProcessController", "com.android.server.am.PendingIntentRecord",
                    "android.app.BackgroundStartPrivileges", Intent::class.java, ActivityOptions::class.java,
                    object : IMemberHooker() {
                        override fun before(param: IMemberHookParam) {
                            val packageName = param.argAs<String?>(2) ?: return
                            if (packageName == ALLOWED_APP_ID) {
                                param.result = 1
                            }
                        }
                    }
                )
            }
        }

        hookAllMethods(
            "com.android.server.wm.ActivityStarterImpl", loadedParam.classLoader,
            "isAllowedStartActivity",
            object : IMemberHooker() {
                override fun before(param: IMemberHookParam) {
                    var count = -1
                    for (clz in param.args) {
                        count += 1
                        if (clz is String) {
                            break
                        }
                    }

                    val packageName = param.args[count] ?: return
                    if (packageName == ALLOWED_APP_ID) {
                        param.result = true
                    }
                }
            }
        )
    }
}
