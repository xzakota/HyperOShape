package com.xzakota.oshape.startup.rule.home.layout

import android.content.Context
import com.xzakota.android.hook.core.loader.base.IMemberAfterHookCallback
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setIntField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.gridConfig
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isBottomMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isHorizontalMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isTopMargin
import com.xzakota.oshape.startup.rule.home.shared.ScreenWorkspace.isUnlockGrids
import kotlin.math.max

object ScreenWorkspaceLayoutRules : BaseHook() {
    private var sCellCountX = 0
    private var sCellCountY = 0

    private var currentCellCountX = 0
    private var currentCellCountY = 0
    private var currentCellWidth = 0
    private var currentCellHeight = 0

    override fun onHookLoad() {
        if (isUnlockGrids) {
            sCellCountX = prefs.getInt("home_layout_screen_workspace_unlock_grids_x", 4)
            sCellCountY = prefs.getInt("home_layout_screen_workspace_unlock_grids_y", 7)
            logD("Setup layout rules: ${sCellCountX}x${sCellCountY}")

            loadClass("com.miui.home.settings.MiuiHomeSettings").beforeHookedMethodByName("setUpScreenCellsConfig") { it ->
                val settings = it.nonNullThisObject

                val miuiHomeConfig = settings.getObjectField("mMiuiHomeConfig")
                val screenCellsConfig = settings.getObjectField("mScreenCellsConfig")

                miuiHomeConfig?.callMethod("removePreference", screenCellsConfig)
                logD("Remove preference($screenCellsConfig) form MIUIHomeSettings")
                it.result = null
            }
        }

        phoneDeviceRules.afterHookedMethod(
            "calGridSize", Context::class.java, Int::class.java, Int::class.java, Boolean::class.java,
            block = PhoneCalGridSizeHook
        )

        gridConfig.beforeHookedMethod("getCellWidth") {
            if (currentCellWidth != 0) {
                it.result = currentCellWidth
            }
        }

        gridConfig.beforeHookedMethod("getCellHeight") {
            if (currentCellHeight != 0) {
                it.result = currentCellHeight
            }
        }

        gridConfig.beforeHookedMethod("getCountCellX") {
            if (isUnlockGrids && currentCellCountX != 0) {
                it.result = currentCellCountX
            }
        }

        gridConfig.beforeHookedMethod("getCountCellY") {
            if (isUnlockGrids && currentCellCountY != 0) {
                it.result = currentCellCountY
            }
        }
    }

    object PhoneCalGridSizeHook : IMemberAfterHookCallback {
        override fun onMemberHooked(param: IMemberHookParam) {
            val rules = param.nonNullThisObject

            val maxGridWidth = rules.getIntField("mMaxGridWidth")
            val workspaceCellSideDefault = rules.getIntField("mWorkspaceCellSideDefault")
            val cellSize = rules.getIntField("mCellSize")
            val cellCountY = rules.getIntField("mCellCountY")
            val workspaceTopPadding = rules.callMethodAs<Int>("getWorkspacePaddingTop")
            val workspaceCellPaddingBottom = rules.getObjectFieldAs<Any>("mWorkspaceCellPaddingBottom")
                .callMethodAs<Int>("getValue")

            val sWorkspacePaddingTop = if (isTopMargin) {
                topMargin
            } else {
                -1
            }

            val sWorkspacePaddingBottom = if (isBottomMargin) {
                bottomMargin
            } else {
                -1
            }

            val sWorkspaceCellSide = if (isHorizontalMargin) {
                horizontalMargin
            } else {
                -1
            }

            currentCellCountX = if (sCellCountX == 0) {
                param.argAs(1)
            } else {
                sCellCountX
            }
            currentCellCountY = if (sCellCountY == 0) {
                cellCountY
            } else {
                sCellCountY
            }

            currentCellWidth = cellSize
            currentCellHeight = cellSize

            val cellWorkspaceHeight = cellSize * cellCountY

            if (isUnlockGrids || isHorizontalMargin) {
                currentCellWidth = (maxGridWidth - if (isHorizontalMargin) {
                    sWorkspaceCellSide
                } else {
                    workspaceCellSideDefault
                }) / currentCellCountX
            }

            if (isUnlockGrids || isTopMargin || isBottomMargin) {
                currentCellHeight = (cellWorkspaceHeight + if (isTopMargin) {
                    workspaceTopPadding - sWorkspacePaddingTop
                } else {
                    0
                } + if (isBottomMargin) {
                    workspaceCellPaddingBottom - sWorkspacePaddingBottom
                } else {
                    0
                }) / currentCellCountY
            }

            rules.setIntField("mCellSize", max(currentCellWidth, currentCellHeight))

            if (isTopMargin) {
                rules.getObjectFieldAs<Any>("mWorkspaceTopPadding").callMethod("setValue", sWorkspacePaddingTop)
            }

            if (isBottomMargin) {
                rules.getObjectFieldAs<Any>("mWorkspaceCellPaddingBottom")
                    .callMethod("setValue", sWorkspacePaddingBottom)
            }

            if (isHorizontalMargin) {
                rules.setIntField("mWorkspaceCellSide", (maxGridWidth - currentCellWidth * currentCellCountX) / 2)
            }

            logD(
                """ |
                    |Applied layout rules:
                    |  cellCountX    => $currentCellCountX
                    |  cellCountY    => $currentCellCountY
                    |  paddingTop    => $sWorkspacePaddingTop
                    |  paddingBottom => $sWorkspacePaddingBottom
                    |  cellSide      => $sWorkspaceCellSide
                    |  cellSizeO     => $cellSize
                    |  cellWidth     => $currentCellWidth
                    |  cellHeight    => $currentCellHeight
                """.trimMargin()
            )
        }
    }

    private val topMargin by lazy {
        dp2px(prefs.getInt("home_layout_screen_workspace_top_margin", 30))
    }

    private val bottomMargin by lazy {
        dp2px(prefs.getInt("home_layout_screen_workspace_bottom_margin", 120))
    }

    private val horizontalMargin by lazy {
        dp2px(prefs.getInt("home_layout_screen_workspace_horizontal_margin", 20))
    }

    private val phoneDeviceRules by lazy {
        loadClass("com.miui.home.launcher.compat.PhoneDeviceRules")
    }
}
