package com.xzakota.oshape.startup.rule.home.shared

import com.xzakota.oshape.startup.base.BaseShare

object ScreenWorkspace : BaseShare() {
    val isUnlockGrids by lazy {
        prefs.getBoolean("home_layout_screen_workspace_unlock_grids")
    }

    val isTopMargin by lazy {
        prefs.getBoolean("home_layout_screen_workspace_top_margin_enabled")
    }

    val isBottomMargin by lazy {
        prefs.getBoolean("home_layout_screen_workspace_bottom_margin_enabled")
    }

    val isHorizontalMargin by lazy {
        prefs.getBoolean("home_layout_screen_workspace_horizontal_margin_enabled")
    }
}
