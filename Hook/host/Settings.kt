package com.xzakota.oshape.startup.host

import com.xzakota.oshape.startup.base.BaseHost
import com.xzakota.oshape.startup.rule.settings.ChangeDeviceBgEffect
import com.xzakota.oshape.startup.rule.settings.FixJumpDrawOverlayDetails
import com.xzakota.oshape.startup.rule.settings.ShowNotificationChannelID
import com.xzakota.xposed.annotation.HookHost

@HookHost(targetPackage = "com.android.settings")
class Settings : BaseHost() {
    override fun onInitHook() {
        // 基础
        loadHookIfEnabled(FixJumpDrawOverlayDetails, "settings_base_fix_jump_draw_overlay_details_enabled")

        // 应用信息
        loadHookIfEnabled(ShowNotificationChannelID, "settings_application_detail_show_notification_channel_id_enabled")

        // 高级
        loadHookIfEnabled(ChangeDeviceBgEffect, "settings_device_bg_effect_follow_hook_theme_enabled")
    }

    override fun isSupportCustomTheme(): Boolean = true
}
