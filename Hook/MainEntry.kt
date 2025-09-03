package com.xzakota.oshape.startup

import android.content.SharedPreferences
import com.xzakota.android.hook.XHookHelper
import com.xzakota.android.hook.annotation.AutoEntry
import com.xzakota.android.hook.core.loader.base.IEntry
import com.xzakota.android.hook.core.loader.base.IModuleStartupParam
import com.xzakota.android.hook.core.loader.base.IPackageLoadedParam
import com.xzakota.android.hook.core.provider.XHookPreferenceProvider.cachedPrefsMap
import com.xzakota.android.hook.startup.base.BaseHostByDexKit
import com.xzakota.android.util.SystemUtils
import com.xzakota.android.xposed.XposedFramework
import com.xzakota.code.log.Logger
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.application.SystemShared.androidVersion
import com.xzakota.oshape.application.SystemShared.hyperOSVersion
import com.xzakota.oshape.startup.host.Android
import com.xzakota.oshape.startup.host.HostDataProvider
import com.xzakota.oshape.startup.host.Common
import com.xzakota.android.dexkit.DexKit
import com.xzakota.android.hook.core.provider.XHookPreferenceProvider
import com.xzakota.oshape.util.LogUtils
import com.xzakota.oshape.util.SafeMode
import com.xzakota.reflect.extension.instanceOf

@AutoEntry(framework = [XposedFramework.LSPOSED], priority = [49, 51], isAddMakerAnnotation = true)
class MainEntry : IEntry, SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(param: IModuleStartupParam) {
        XHookHelper.config(AppConstant.HOOK_PREFS) {
            loggerMainTag = AppConstant.APP_FULL_NAME
            onSharedPreferenceChanged(null, null)
        }

        XHookPreferenceProvider.registerOnSharedPreferenceChangeListener(this)
        DexKit.SAVE_DIR_NAME = AppConstant.APP_FULL_NAME
    }

    override fun onAndroidPackageLoaded(param: IPackageLoadedParam) {
        Logger.i("System ClassLoader: ${param.classLoader}", baseTag = param.packageName)
        Logger.i("AndroidVersion = $androidVersion, HyperOSVersion = $hyperOSVersion", baseTag = param.packageName)
        Logger.level(param.packageName)

        Android.handleLoadPackage(param)
    }

    override fun onFirstPackageLoaded(param: IPackageLoadedParam) {
        val packageName = param.packageName

        XHookHelper.logPackageInfo()
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
                return
            }

            Logger.i("In supported host list, start load hooks...", packageName)

            val baseHost = XHookHelper.moduleClassLoader.loadClass(data.key).instanceOf() as? BaseHostByDexKit
            if (baseHost != null) {
                baseHost.handleLoadPackage(param)
                return
            }
        }

        Common.handleLoadPackage(param)
    }

    override fun isSupportMultipleLoadPackage(): Boolean = false

    override fun onCheckPackage(param: IPackageLoadedParam): Boolean = AppConstant.checkPackage(param.packageName)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Logger.loggerBaseLevel = LogUtils.getHookLogLevel(
            cachedPrefsMap.getString(AppConstant.PREF_KEY_COMMON_HOOK_LOG_LEVEL)
        )
    }
}
