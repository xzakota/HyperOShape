package com.xzakota.oshape.startup.rule.home.api

import android.content.Context
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.deviceConfig

object DeviceConfig {
    fun getHotSeatsMarginBottom(): Int = deviceConfig.callStaticMethodAs("getHotSeatsMarginBottom")

    fun getCellWidth(): Int = deviceConfig.callStaticMethodAs("getCellWidth")

    fun getIconWidth(): Int = deviceConfig.callStaticMethodAs("getIconWidth")

    fun isLayoutRtl(): Boolean = deviceConfig.callStaticMethodAs("isLayoutRtl")

    fun isInFoldDeviceLargeScreen(
        context: Context
    ): Boolean = deviceConfig.callStaticMethodAs("isInFoldDeviceLargeScreen", context)
}
