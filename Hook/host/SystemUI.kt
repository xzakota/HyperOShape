package com.xzakota.oshape.startup.host

import com.xzakota.oshape.startup.base.BaseHost
import com.xzakota.oshape.startup.rule.MusicHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.android.NotificationExpandButton
import com.xzakota.oshape.startup.rule.android.NotificationLayoutMargin
import com.xzakota.oshape.startup.rule.android.NotificationRightIconSize
import com.xzakota.oshape.startup.rule.systemui.CCHeaderButton
import com.xzakota.oshape.startup.rule.systemui.CustomizeBottomShortcut
import com.xzakota.oshape.startup.rule.systemui.ExpandNotificationRow
import com.xzakota.oshape.startup.rule.systemui.FixClock
import com.xzakota.oshape.startup.rule.systemui.ForceNotificationOverlapImIcon
import com.xzakota.oshape.startup.rule.systemui.HideCCHeaderCarrier
import com.xzakota.oshape.startup.rule.systemui.HideFingerprintIcon
import com.xzakota.oshape.startup.rule.systemui.KeyguardNotificationControl
import com.xzakota.oshape.startup.rule.systemui.KeyguardScramblePIN
import com.xzakota.oshape.startup.rule.systemui.KeyguardShowCarrier
import com.xzakota.oshape.startup.rule.systemui.KeyguardStatusBarMode
import com.xzakota.oshape.startup.rule.systemui.MIUINotificationStyleIconCentered
import com.xzakota.oshape.startup.rule.systemui.ManageGesturesDat
import com.xzakota.oshape.startup.rule.systemui.MediaPanelLyric
import com.xzakota.oshape.startup.rule.systemui.MiniWindowBarSize
import com.xzakota.oshape.startup.rule.systemui.MiniWindowTriggerThreshold
import com.xzakota.oshape.startup.rule.systemui.NCHeaderLyric
import com.xzakota.oshape.startup.rule.systemui.NCHeaderWeather
import com.xzakota.oshape.startup.rule.systemui.RemoveKeyguardShortcutTips
import com.xzakota.oshape.startup.rule.systemui.RemoveMagazineGesture
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.plugin.controller.FocusNotificationController
import com.xzakota.oshape.startup.rule.systemui.plugin.controller.ControlCenterController
import com.xzakota.oshape.startup.rule.systemui.plugin.controller.GlobalActionsController
import com.xzakota.oshape.startup.rule.systemui.plugin.HideRingerModeLayout
import com.xzakota.oshape.startup.rule.systemui.plugin.controller.KeyguardShortcutController
import com.xzakota.oshape.startup.rule.systemui.shared.Advanced.globalActionsStyle
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.isHideHeaderCarrier
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.fixClock
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.isCustomizeBottomButton
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.isRemoveButtonTips
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.statusBarMode
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.autoExpandNotificationsPackages
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.focusNotificationWhitelistPackages
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.forceOverlapImIconPackages
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarHeight
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarHide
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarWidth
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowMaxTriggerThreshold
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowTriggerThreshold
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.showLyric
import com.xzakota.oshape.startup.rule.systemui.tile.FiveGSwitch
import com.xzakota.oshape.startup.rule.systemui.tile.OptReduceBrightColorsTile
import com.xzakota.oshape.startup.rule.systemui.tile.TileManagement
import com.xzakota.xposed.annotation.HookHost

@HookHost(targetPackage = "com.android.systemui", isSupportSafeMode = true)
class SystemUI : BaseHost() {
    override fun onInitHook() {
        // loadHook(ExtrasSafeMode)
        MiuiStub.createHook()

        // 焦点通知
        loadHook(FocusNotificationController, focusNotificationWhitelistPackages.isNotEmpty())

        // 原生通知
        loadHookIfEnabled(NotificationExpandButton, "android_notification_expand_button_icon_custom_enabled")
        loadHookIfEnabled(NotificationLayoutMargin, "android_notification_layout_margin_enabled")
        loadHookIfEnabled(NotificationRightIconSize, "android_notification_right_icon_size_enabled")

        // 通知中心
        loadHookIfEnabled(NCHeaderWeather(), "systemui_notification_center_add_weather_enabled")
        loadHookIfEnabled(
            MIUINotificationStyleIconCentered, "systemui_notification_center_miui_style_icon_centered_enabled"
        )
        loadHook(MiniWindowBarSize, isMiniWindowBarHide || isMiniWindowBarWidth || isMiniWindowBarHeight)
        loadHook(MiniWindowTriggerThreshold, isMiniWindowTriggerThreshold || isMiniWindowMaxTriggerThreshold)
        loadHook(ForceNotificationOverlapImIcon, forceOverlapImIconPackages != setOf("com.tencent.mm"))
        loadHook(ExpandNotificationRow, autoExpandNotificationsPackages.isNotEmpty())
        loadHook(MusicHook, showLyric != 0)
        loadHook(MediaPanelLyric(), showLyric == 1)
        loadHook(NCHeaderLyric(), showLyric == 2)

        // 控制中心
        loadHook(ControlCenterController)
        loadHook(TileManagement)
        loadHookIfEnabled(OptReduceBrightColorsTile, "systemui_control_center_opt_reduce_bright_colors_tile_enabled")
        loadHook(HideCCHeaderCarrier, isHideHeaderCarrier)
        loadHookIfEnabled(CCHeaderButton(), "systemui_control_center_add_header_shortcut_buttons_enabled")
        loadHookIfEnabled(FiveGSwitch, "systemui_control_center_five_g_switch_enabled")

        // 全局音量条
        loadHookIfEnabled(HideRingerModeLayout, "systemui_volume_dialog_hide_ringer_mode_enabled")

        // 锁屏
        loadHookIfEnabled(KeyguardShowCarrier, "systemui_lockscreen_show_carrier_enabled")
        loadHookIfEnabled(HideFingerprintIcon, "systemui_lockscreen_remove_fingerprint_icon_enabled")
        loadHook(FixClock, fixClock != 0)
        loadHookIfEnabled(KeyguardNotificationControl, "systemui_lockscreen_move_notification_down_enabled")
        loadHook(KeyguardShortcutController)
        loadHook(RemoveKeyguardShortcutTips, isRemoveButtonTips)
        loadHook(CustomizeBottomShortcut, isCustomizeBottomButton && !isMoreHyperOS200)
        // loadHookIfEnabled(CustomizeKeyguardGesture,"systemui_lockscreen_customize_gesture_enabled")
        loadHookIfEnabled(KeyguardScramblePIN, "systemui_lockscreen_scramble_pin_enabled")
        loadHookIfEnabled(RemoveMagazineGesture, "systemui_lockscreen_remove_open_magazine_gesture_enabled")
        loadHook(KeyguardStatusBarMode, statusBarMode != 0)

        // 标准化存储
        loadHookIfEnabled(ManageGesturesDat, "systemui_standardized_storage_statusbar_gestures_dat_enabled")

        // 高级
        loadHook(GlobalActionsController, globalActionsStyle >= 2)
    }

    override fun isSupportCustomTheme(): Boolean = true
}
