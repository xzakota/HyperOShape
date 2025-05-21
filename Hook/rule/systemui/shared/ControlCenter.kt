package com.xzakota.oshape.startup.rule.systemui.shared

import com.xzakota.android.hook.startup.base.BaseShare

object ControlCenter : BaseShare() {
    val isHideHeaderCarrier by lazy {
        prefs.getBoolean("systemui_control_center_hide_header_carrier_enabled")
    }

    val controlCenterHeaderController by lazy {
        loadClass("com.android.systemui.controlcenter.shade.ControlCenterHeaderController")
    }

    val miuiQSFactory by lazy {
        loadClass("com.android.systemui.qs.tileimpl.MiuiQSFactory")
    }

    val qsTileImpl by lazy {
        loadClass("com.android.systemui.qs.tileimpl.QSTileImpl")
    }

    val tileDrawableIcon by lazy {
        loadClass("com.android.systemui.qs.tileimpl.QSTileImpl\$DrawableIcon")
    }

    val tileResourceIcon by lazy {
        loadClass("com.android.systemui.qs.tileimpl.QSTileImpl\$ResourceIcon")
    }

    val tileState by lazy {
        loadClass("com.android.systemui.plugins.qs.QSTile\$State")
    }

    val tileAdapterState by lazy {
        loadClass("com.android.systemui.plugins.qs.QSTile\$AdapterState")
    }

    val tileBooleanState by lazy {
        loadClass("com.android.systemui.plugins.qs.QSTile\$BooleanState")
    }
}
