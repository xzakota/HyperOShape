package com.xzakota.oshape.startup.rule.android.safemode

import android.content.Context
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook

object HandleAppCrash : BaseHook() {
    private const val METHOD_HANDLE_APP_CRASH = "handleAppCrashInActivityController"

    private val handlers = mutableSetOf<AndroidCrashHandler>()

    override fun onHookLoad() {
        loadClass("com.android.server.am.AppErrors", loadedParam.classLoader).methodFinder()
            .filterByName(METHOD_HANDLE_APP_CRASH)
            .filterByReturnType(Boolean::class.java)
            .first()
            .createAfterHook { param ->
                val context = param.nonNullThisObject.getObjectFieldAs<Context>("mContext")

                val crashBean = runCatching {
                    AndroidCrashBean.form(param.args)
                }.getOrElse {
                    return@createAfterHook
                }

                logE(
                    """
                    |$METHOD_HANDLE_APP_CRASH(
                    |    context    = $context
                    |    process    = ${crashBean.process}
                    |    crashInfo  = ${crashBean.crashInfo}
                    |    shortMsg   = ${crashBean.shortMsg}
                    |    longMsg    = ${crashBean.longMsg}
                    |    timeMillis = ${crashBean.timeMillis}
                    |    callingPID = ${crashBean.callingPID}
                    |    callingUID = ${crashBean.callingUID}
                    |)
                    """.trimMargin(),
                    context.packageName
                )

                handlers.forEach {
                    it.handle(context, crashBean)
                }
            }
    }

    fun addHandler(handler: AndroidCrashHandler) = handlers.add(handler)

    fun removeHandler(handler: AndroidCrashHandler) = handlers.remove(handler)

    fun interface AndroidCrashHandler {
        fun handle(context: Context, crashBean: AndroidCrashBean)
    }
}
