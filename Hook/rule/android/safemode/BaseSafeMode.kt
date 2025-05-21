package com.xzakota.oshape.startup.rule.android.safemode

import android.content.Context
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.oshape.startup.rule.android.safemode.HandleAppCrash.AndroidCrashHandler

abstract class BaseSafeMode : AndroidCrashHandler {
    private val records = mutableMapOf<String, CrashRecord>()

    override fun handle(context: Context, crashBean: AndroidCrashBean) {
        val currentPackageName = crashBean.process?.chainGetObjectAs<String>("info.packageName") ?: return
        if (!isScopeApp(currentPackageName)) {
            return
        }

        val record = records.getOrPut(currentPackageName) {
            CrashRecord(crashBean.timeMillis, 0)
        }

        val intervalTime = crashBean.timeMillis - record.timeMillis
        record.timeMillis = crashBean.timeMillis
        if (intervalTime > 60000) {
            record.crashCount = 0
        } else if (intervalTime < 10240) {
            if (record.crashCount >= 2) {
                if (onHandleCrash(context, currentPackageName)) {
                    record.crashCount = 0
                }
            } else {
                record.crashCount++
            }
        }
    }

    protected abstract fun onHandleCrash(context: Context, packageName: String): Boolean

    protected abstract fun isScopeApp(packageName: String): Boolean
}
