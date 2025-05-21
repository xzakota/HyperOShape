package com.xzakota.oshape.startup.host

import com.xzakota.android.hook.startup.base.BaseHost
import com.xzakota.android.util.SystemUtils.getAndroidVersion
import com.xzakota.code.log.Logger
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.startup.rule.android.AllowedStartBackground
import com.xzakota.oshape.startup.rule.android.safemode.HandleAppCrash
import com.xzakota.oshape.util.SafeMode
import miui.sdk.getHyperOSVersion

object Android : BaseHost() {
    init {
        AllowedStartBackground.ALLOWED_APP_ID = AppConstant.APP_ID
        HandleAppCrash.addHandler(SafeMode)
    }

    override fun onInitHook() {
        logI("System ClassLoader: ${packageParam.classLoader}", from = null)
        logI("AndroidVersion = ${getAndroidVersion()}, HyperOSVersion = ${getHyperOSVersion()}", from = null)
        Logger.level()

        // base
        loadHook(AllowedStartBackground)
        loadHook(HandleAppCrash)
    }
}
