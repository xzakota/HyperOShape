package com.xzakota.oshape.startup.rule.home.shared

import com.xzakota.oshape.startup.base.BaseShare

object SearchBox : BaseShare() {
    val isSearchBoxWidth by lazy {
        prefs.getBoolean("home_layout_search_box_width_enabled")
    }

    val isSearchBoxHeight by lazy {
        prefs.getBoolean("home_layout_search_box_height_enabled")
    }

    val isLayoutOptimization by lazy {
        prefs.getBoolean("home_layout_search_box_optimization_enabled")
    }

    val isChildElementSize by lazy {
        prefs.getBoolean("home_layout_search_box_child_element_size_enabled")
    }

    val searchBarDesktopLayout by lazy {
        loadClass("com.miui.home.launcher.SearchBarDesktopLayout")
    }

    val searchBarDesktopLayoutUpdateStyle by lazy {
        loadClass("com.miui.home.launcher.SearchBarDesktopLayout$1")
    }
}
