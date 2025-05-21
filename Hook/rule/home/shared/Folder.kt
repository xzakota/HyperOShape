package com.xzakota.oshape.startup.rule.home.shared

import com.xzakota.oshape.startup.base.BaseShare

object Folder : BaseShare() {
    // 标题居中
    val isTitleCentered by lazy {
        prefs.getBoolean("home_layout_opened_folder_title_centered_enabled")
    }

    // 使用屏宽
    val isUseScreenWidth by lazy {
        prefs.getBoolean("home_layout_opened_folder_full_width_enabled")
    }

    // 水平边距
    val isHorizontalMargin by lazy {
        prefs.getBoolean("home_layout_opened_folder_horizontal_margin_enabled")
    }

    // 列数量
    val openedColumns by lazy {
        prefs.getInt("home_layout_opened_folder_columns", 3)
    }

    // 解锁更多尺寸
    val unlockMoreGrid by lazy {
        prefs.getBoolean("home_big_folder_more_grid_enabled")
    }

    val folder by lazy {
        loadClass("com.miui.home.launcher.Folder")
    }

    val folderIcon by lazy {
        loadClass("com.miui.home.launcher.FolderIcon")
    }

    val folderCling by lazy {
        loadClass("com.miui.home.launcher.FolderCling")
    }

    val folderIcon1x1 by lazy {
        loadClass("com.miui.home.launcher.folder.FolderIcon1x1")
    }

    val folderIcon2x2 by lazy {
        loadClass("com.miui.home.launcher.folder.FolderIcon2x2")
    }

    val folderIconPreviewInfo by lazy {
        loadClass("com.miui.home.launcher.folder.FolderIconPreviewInfo")
    }

    val folderAnimController by lazy {
        loadClass("com.miui.home.launcher.folder.FolderAnimController")
    }
}
