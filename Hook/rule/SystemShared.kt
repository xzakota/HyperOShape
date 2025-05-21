package com.xzakota.oshape.startup.rule

import com.xzakota.oshape.startup.base.BaseShare
import miui.sdk.isMoreHyperOSMinorVersion

object SystemShared : BaseShare() {
    val isMoreHyperOS200 = isMoreHyperOSMinorVersion(200)
}
