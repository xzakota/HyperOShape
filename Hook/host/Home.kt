package com.xzakota.oshape.startup.host

import com.xzakota.oshape.startup.base.BaseHost
import com.xzakota.oshape.startup.rule.home.DockBlur
import com.xzakota.oshape.startup.rule.home.RemoveMarketSearch
import com.xzakota.oshape.startup.rule.home.TrueXSquareGrid
import com.xzakota.oshape.startup.rule.home.UnlockDockIconNumLimit
import com.xzakota.oshape.startup.rule.home.WidgetBlur
import com.xzakota.oshape.startup.rule.home.folder.MoreBigFolder
import com.xzakota.oshape.startup.rule.home.folder.OptFolderAnim
import com.xzakota.oshape.startup.rule.home.layout.DockBottomMargin
import com.xzakota.oshape.startup.rule.home.layout.DockHeight
import com.xzakota.oshape.startup.rule.home.layout.HidePageIndicator
import com.xzakota.oshape.startup.rule.home.layout.OpenedFolderColumns
import com.xzakota.oshape.startup.rule.home.layout.OpenedFolderVerticalSpacing
import com.xzakota.oshape.startup.rule.home.layout.PageIndicatorBottomMargin
import com.xzakota.oshape.startup.rule.home.layout.ScreenWorkspaceLayoutRules
import com.xzakota.oshape.startup.rule.home.layout.SearchBoxBottomMargin
import com.xzakota.oshape.startup.rule.home.layout.SearchBoxSize
import com.xzakota.oshape.startup.rule.home.shared.Blur.isWidgetBlurFollowHookTheme
import com.xzakota.oshape.startup.rule.home.shared.Folder.isTitleCentered
import com.xzakota.oshape.startup.rule.home.shared.Folder.isUseScreenWidth
import com.xzakota.oshape.startup.rule.home.shared.Folder.openedColumns
import com.xzakota.oshape.startup.rule.home.shared.Folder.unlockMoreGrid
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isBottomMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isHorizontalMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isTopMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isUnlockGrids
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isLayoutOptimization
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isSearchBoxHeight
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isSearchBoxWidth
import com.xzakota.xposed.annotation.HookHost

@HookHost(targetPackage = "com.miui.home", isSupportSafeMode = true)
class Home : BaseHost() {
    override fun onInitHook() {
        // 抽屉
        loadHookIfEnabled(RemoveMarketSearch, "home_drawer_remove_market_search_enabled")

        // 布局
        loadHook(ScreenWorkspaceLayoutRules, isUnlockGrids || isTopMargin || isBottomMargin || isHorizontalMargin)
        loadHookIfEnabled(HidePageIndicator, "home_layout_page_indicator_hide_enabled")
        loadHookIfEnabled(PageIndicatorBottomMargin, "home_layout_page_indicator_bottom_margin_enabled")
        loadHookIfEnabled(DockHeight, "home_layout_dock_height_enabled")
        loadHookIfEnabled(DockBottomMargin, "home_layout_dock_bottom_margin_enabled")
        loadHook(
            SearchBoxSize,
            isSearchBoxWidth || isSearchBoxHeight || isLayoutOptimization || isWidgetBlurFollowHookTheme
        )
        loadHookIfEnabled(SearchBoxBottomMargin, "home_layout_search_box_bottom_margin_enabled")
        loadHook(OpenedFolderColumns, isTitleCentered || isUseScreenWidth || openedColumns != 3)
        loadHookIfEnabled(OpenedFolderVerticalSpacing, "home_layout_opened_folder_vertical_spacing_enabled")

        // 底栏
        loadHookIfEnabled(DockBlur, "home_dock_mi_blur_bg_enabled")
        loadHookIfEnabled(UnlockDockIconNumLimit, "home_unlock_dock_icon_num_limit_enabled")

        // 大文件夹
        loadHookIfEnabled(TrueXSquareGrid, "home_big_folder_true_x_square_grid_enabled")
        loadHook(MoreBigFolder, unlockMoreGrid)
        loadHook(OptFolderAnim, unlockMoreGrid || openedColumns != 3)

        // 高级
        loadHook(WidgetBlur, isWidgetBlurFollowHookTheme)
        // loadHook(OpenedFolderBlur)
    }

    override fun isSupportCustomTheme(): Boolean = true
}
