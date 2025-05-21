package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowMaxTriggerThreshold
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowTriggerThreshold

object MiniWindowTriggerThreshold : BaseHook() {
    override fun onHookLoad() {
        if (isMiniWindowTriggerThreshold) {
            resHooker.replaceDimen(
                nonNullBasePackageName,
                "mini_window_trigger_threshold",
                prefs.getInt("systemui_notification_center_mini_window_trigger_threshold")
            )
        }

        if (isMiniWindowMaxTriggerThreshold) {
            resHooker.replaceDimen(
                nonNullBasePackageName,
                "mini_window_max_trigger_threshold",
                prefs.getInt("systemui_notification_center_mini_window_max_trigger_threshold")
            )
        }
    }
}
