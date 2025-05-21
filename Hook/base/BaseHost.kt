package com.xzakota.oshape.startup.base

import android.content.Context
import com.xzakota.android.hook.core.loader.base.IPackageLoadedParam
import com.xzakota.android.hook.startup.base.BaseHost
import com.xzakota.code.log.LogExtensions.logEBeforeThrow
import com.xzakota.oshape.util.DexKit
import com.xzakota.oshape.util.HookThemeManager
import com.xzakota.oshape.util.SafeMode
import com.xzakota.oshape.util.SafeMode.SAFE_MODE_TAG

abstract class BaseHost : BaseHost() {
    override fun handleLoadPackage(param: IPackageLoadedParam) {
        if (SafeMode.isActiveSafeMode(param.packageName)) {
            logI("Block all hook.", param.packageName, SAFE_MODE_TAG)
            return
        }

        DexKit.use(param) {
            super.handleLoadPackage(param)
        }
    }

    override fun onAttachContext(context: Context) {
        super.onAttachContext(context)

        runCatching {
            HookThemeManager.loadDefaultThemeByHost()
            if (isSupportCustomTheme()) {
                HookThemeManager.loadCurrentThemeByHost()
            }
        }.logEBeforeThrow("Hook theme load failed")
    }

    protected open fun isSupportCustomTheme(): Boolean = false
}
