package com.xzakota.oshape.startup.rule.android.safemode

import android.app.ApplicationErrorReport

data class AndroidCrashBean(
    val process: Any?,
    val crashInfo: ApplicationErrorReport.CrashInfo,
    val shortMsg: String,
    val longMsg: String,
    val stackTrace: String,
    val timeMillis: Long,
    val callingPID: Int,
    val callingUID: Int
) {
    companion object {
        @Throws(Throwable::class)
        fun form(array: Array<Any?>): AndroidCrashBean = AndroidCrashBean(
            array[0],
            array[1] as ApplicationErrorReport.CrashInfo,
            array[2] as String,
            array[3] as String,
            array[4] as String,
            array[5] as Long,
            array[6] as Int,
            array[7] as Int
        )
    }
}

data class CrashRecord(var timeMillis: Long, var crashCount: Int)
