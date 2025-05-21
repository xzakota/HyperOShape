package com.xzakota.oshape.startup.rule.securitycenter.shared

import com.xzakota.android.hook.startup.base.BaseShare

object ApplicationsDetails : BaseShare() {
    val applicationsDetailsFragment by lazy {
        loadClass("com.miui.appmanager.fragment.ApplicationsDetailsFragment")
    }
}
