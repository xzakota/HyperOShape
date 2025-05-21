package com.xzakota.oshape.startup

import com.xzakota.android.hook.XHookHelper
import com.xzakota.android.hook.core.loader.base.IModuleStartupParam
import com.xzakota.android.hook.core.loader.base.IPackageLoadedParam
import com.xzakota.android.hook.core.provider.XHookPrefsProvider
import com.xzakota.android.hook.startup.LSPModuleEntry
import com.xzakota.android.hook.startup.base.BaseHost
import com.xzakota.android.util.SystemUtils
import com.xzakota.code.extension.createInstance
import com.xzakota.code.log.Logger
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.application.MainApplication
import com.xzakota.oshape.startup.host.Android
import com.xzakota.oshape.startup.host.HostDataProvider
import com.xzakota.oshape.startup.host.Other
import com.xzakota.oshape.util.DexKit
import com.xzakota.oshape.util.LogUtils
import com.xzakota.oshape.util.SafeMode
import com.xzakota.xposed.annotation.LSPosedModule
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam

@LSPosedModule
class MainEntry(base: XposedInterface, param: ModuleLoadedParam) : LSPModuleEntry(base, param) {
    override fun onCreate(param: IModuleStartupParam) {
        XHookPrefsProvider.readSharedPreferences(AppConstant.APP_ID, MainApplication.HOOK_PREFS)
        XHookHelper.initModule(AppConstant.APP_ID).initLogger(
            AppConstant.APP_FULL_NAME,
            LogUtils.getHookLogLevel(XHookPrefsProvider.prefs.getString(AppConstant.PREF_KEY_COMMON_HOOK_LOG_LEVEL))
        )

        DexKit.SAVE_DIR_NAME = AppConstant.APP_FULL_NAME
    }

    override fun onAndroidPackageLoaded(param: IPackageLoadedParam) {
        super.onAndroidPackageLoaded(param)

        Android.handleLoadPackage(param)
    }

    override fun onFirstPackageLoaded(param: IPackageLoadedParam): Boolean {
        super.onFirstPackageLoaded(param)

        var isHandleScopeHost = false
        val packageName = param.packageName

        XHookHelper.logPackageInfo(param)
        for (data in HostDataProvider.map()) {
            val app = data.value

            val targetPackageName = app.targetPackageName
            if (targetPackageName.isEmpty() || packageName != targetPackageName) {
                continue
            }

            val minSDK = app.minSDK
            if (minSDK != -1 && !SystemUtils.isMoreAndroidVersion(minSDK)) {
                continue
            }

            if (app.isSupportSafeMode && SafeMode.isUserSafeModeOnHook(packageName)) {
                Logger.i("Block all hook.", packageName, SafeMode.USER_SAFE_MODE_TAG)
                return false
            }

            Logger.i("In supported hosts list, handleLoadPackage...", packageName)
            isHandleScopeHost = true

            val baseHost = XHookHelper.moduleClassLoader.loadClass(data.key).createInstance() as? BaseHost
            if (baseHost != null) {
                baseHost.handleLoadPackage(param)
                return false
            }
        }

        if (!isHandleScopeHost) {
            Other.handleLoadPackage(param)
        }

        return false
    }

    override fun onCheckPackage(param: IPackageLoadedParam): Boolean = AppConstant.checkPackage(param.packageName)
}
