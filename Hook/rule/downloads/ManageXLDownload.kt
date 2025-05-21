package com.xzakota.oshape.startup.rule.downloads

import android.content.Context
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.code.extension.setStaticStringFieldWith
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.util.StandardFolder

object ManageXLDownload : BaseHook() {
    private var logPath: String? = null

    override fun onHookLoad() {
        val xlConfig = loadClass("com.android.providers.downloads.config.XLConfig")

        xlConfig.setStaticStringFieldWith("logDir") {
            replacePath(it).also { path -> logPath = path }
        }
        xlConfig.setStaticStringFieldWith("logSoDir", ::replacePath)

        xlConfig.beforeHookedMethod("setDebug", Context::class.java, Boolean::class.java, String::class.java) {
            if (logPath != it.args[2]) {
                it.args[2] = logPath
                logPath = null
            }
        }
    }

    private fun replacePath(raw: String?): String = StandardFolder.replacePathUnderDownload(raw, ".xlDownload")
}
