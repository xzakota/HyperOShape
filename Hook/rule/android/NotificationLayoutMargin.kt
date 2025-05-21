package com.xzakota.oshape.startup.rule.android

import com.xzakota.oshape.startup.base.BaseHook

object NotificationLayoutMargin : BaseHook() {
    override fun onHookLoad() {
        val notificationLayoutMargin = prefs.getInt("android_notification_layout_margin", 0)

        resHooker.replaceDimen("android", "notification_headerless_margin_oneline", notificationLayoutMargin)
        resHooker.replaceDimen("android", "notification_headerless_margin_twoline", notificationLayoutMargin)
        resHooker.replaceDimen("android", "notification_right_icon_headerless_margin", notificationLayoutMargin)
        // resHooker.replaceDimen("android", "notification_text_margin_top", notificationLayoutMargin)
    }
}
