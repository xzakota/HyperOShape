package com.xzakota.oshape.startup.host

import com.xzakota.oshape.startup.base.BaseHost
import com.xzakota.oshape.startup.rule.downloads.ManageXLDownload
import com.xzakota.xposed.annotation.HookHost

@HookHost(targetPackage = "com.android.providers.downloads")
class Downloads : BaseHost() {
    override fun onInitHook() {
        // 标准化存储
        loadHookIfEnabled(ManageXLDownload, "downloads_standardized_storage_xlDownload_enabled")
    }
}
