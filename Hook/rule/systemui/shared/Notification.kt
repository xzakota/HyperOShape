package com.xzakota.oshape.startup.rule.systemui.shared

import com.xzakota.android.hook.startup.base.BaseShare
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.util.DexKit
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Method

object Notification : BaseShare() {
    val isMiniWindowBarHide by lazy {
        prefs.getBoolean("systemui_notification_center_mini_window_bar_hide_enabled")
    }

    val isMiniWindowBarWidth by lazy {
        prefs.getBoolean("systemui_notification_center_mini_window_bar_width_enabled")
    }

    val isMiniWindowBarHeight by lazy {
        prefs.getBoolean("systemui_notification_center_mini_window_bar_height_enabled")
    }

    val isMiniWindowTriggerThreshold by lazy {
        prefs.getBoolean("systemui_notification_center_mini_window_trigger_threshold_enabled")
    }

    val isMiniWindowMaxTriggerThreshold by lazy {
        prefs.getBoolean("systemui_notification_center_mini_window_max_trigger_threshold_enabled")
    }

    val autoExpandNotificationsPackages by lazy {
        prefs.getNonNullStringSet("systemui_notification_center_auto_expand_notifications")
    }

    val focusNotificationWhitelistPackages by lazy {
        prefs.getNonNullStringSet(
            "systemui_notification_center_focus_notification_whitelist", setOf(AppConstant.APP_ID)
        )
    }

    val forceOverlapImIconPackages by lazy {
        prefs.getNonNullStringSet("systemui_notification_center_force_overlap_im_icon", setOf("com.tencent.mm"))
    }

    val showLyric by lazy {
        prefs.getStringAsInt("systemui_notification_center_show_lyric", 0)
    }

    val expandNotificationRow by lazy {
        loadClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow")
    }

    val miuiExpandableNotificationRow by lazy {
        loadClass("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow")
    }

    val notificationPanelViewController by lazy {
        loadClass("com.android.systemui.shade.NotificationPanelViewController")
    }

    val miuiNotificationPanelViewController by lazy {
        loadClass("com.android.systemui.shade.MiuiNotificationPanelViewController")
    }

    val combinedHeaderController by lazy {
        loadClass("com.android.systemui.controlcenter.shade.CombinedHeaderController")
    }

    val miuiNotificationHeaderView by lazy {
        loadClass("com.android.systemui.qs.MiuiNotificationHeaderView")
    }

    val miuiMediaControlPanel by lazy {
        loadClass("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
    }

    val miuiBaseNotifUtil by lazy {
        loadClass("com.miui.systemui.notification.MiuiBaseNotifUtil")
    }

    val setNotificationPanelVisible by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(setNotificationPanelVisible)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass("com.android.systemui.shade.NotificationPanelExpandController")
                    name("setVisible", StringMatchType.StartsWith)
                }
            }.single()
        }
    }
}
