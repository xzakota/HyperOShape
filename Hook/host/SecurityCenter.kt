package com.xzakota.oshape.startup.host

import com.xzakota.oshape.startup.base.BaseHost
import com.xzakota.oshape.startup.rule.securitycenter.AppDetailShowMoreInfo
import com.xzakota.oshape.startup.rule.securitycenter.ManageMorePermission
import com.xzakota.oshape.startup.rule.securitycenter.OpenByDefaultPreference
import com.xzakota.oshape.startup.rule.securitycenter.OptimizeMenuItems
import com.xzakota.oshape.startup.rule.securitycenter.SimplifiedHomeFragment
import com.xzakota.oshape.startup.rule.securitycenter.SkipInterceptWait
import com.xzakota.xposed.annotation.HookHost

@HookHost(targetPackage = "com.miui.securitycenter", isSupportSafeMode = true)
class SecurityCenter : BaseHost() {
    override fun onInitHook() {
        // UI
        loadHookIfEnabled(SimplifiedHomeFragment, "security_center_interface_simplified_home_fragment_enabled")

        // 安全
        loadHookIfEnabled(SkipInterceptWait, "security_center_security_skip_intercept_wait_enabled")

        // 应用信息
        loadHookIfEnabled(OptimizeMenuItems, "security_center_application_detail_optimize_menu_items_enabled")
        loadHookIfEnabled(ManageMorePermission, "security_center_application_detail_manage_more_permission_enabled")
        loadHookIfEnabled(OpenByDefaultPreference, "security_center_application_detail_open_by_default_enabled")
        loadHookIfEnabled(AppDetailShowMoreInfo, "security_center_application_detail_show_more_info_enabled")
    }
}
