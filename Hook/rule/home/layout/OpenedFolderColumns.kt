package com.xzakota.oshape.startup.rule.home.layout

import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.TextView
import androidx.core.view.isNotEmpty
import androidx.core.view.setPadding
import com.xzakota.android.extension.view.setEllipsize
import com.xzakota.android.extension.view.setPaddingHorizontal
import com.xzakota.android.extension.view.setPaddingLeft
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setIntField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.Folder.folder
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderAnimController
import com.xzakota.oshape.startup.rule.home.shared.Folder.isHorizontalMargin
import com.xzakota.oshape.startup.rule.home.shared.Folder.isTitleCentered
import com.xzakota.oshape.startup.rule.home.shared.Folder.isUseScreenWidth
import com.xzakota.oshape.startup.rule.home.shared.Folder.openedColumns
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.deviceConfig

object OpenedFolderColumns : BaseHook() {
    override fun onHookLoad() {
        if (openedColumns != 3) {
            folderAnimController.afterHookedMethodByName("setupView") {
                val controller = it.nonNullThisObject
                val gridView = controller.getObjectFieldAs<GridView>("mAnimaFolderGridView")
                controller.setIntField("DISPLAY_COUNT_MAX", gridView.callMethodAs<Int>("getMaxRow") * openedColumns)
                controller.setIntField("mFolderColumnCount", openedColumns)
            }

            optFolderAnim()
        }

        folder.afterHookedMethodByName("onOpen") { param ->
            param.nonNullThisObject.getObjectFieldAs<TextView>("mTitleText").run {
                if (ellipsize == TextUtils.TruncateAt.MARQUEE) {
                    return@run
                }

                setEllipsize()
            }
        }

        val firstItemRect = Rect()
        folder.afterHookedMethodByName("bind") { param ->
            val folder = param.thisObjectAs<ViewGroup>()

            val backgroundView = folder.getObjectFieldAs<ViewGroup>("mBackgroundView")
            val header = folder.getObjectFieldAs<View>("mHeader")
            val titleText = folder.getObjectFieldAs<TextView>("mTitleText")
            val renameEdit = folder.getObjectFieldAs<EditText>("mRenameEdit")
            val folderGrid = folder.getObjectFieldAs<FrameLayout>("mFolderGrid")
            val content = folder.getObjectFieldAs<GridView>("mContent")
            content.numColumns = openedColumns

            if (isUseScreenWidth) {
                backgroundView.setPadding(0)

                val setFullScreenWidth = { view: View ->
                    view.layoutParams = view.layoutParams.also { lp ->
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }

                setFullScreenWidth(folderGrid)
                setFullScreenWidth(content)
                setFullScreenWidth(header)
                setFullScreenWidth(titleText)
                setFullScreenWidth(renameEdit)

                var sidePadding = 0
                if (isHorizontalMargin) {
                    sidePadding = horizontalMargin
                    folderGrid.setPaddingHorizontal(sidePadding)
                    content.setPaddingHorizontal(sidePadding)
                    header.setPaddingHorizontal(sidePadding)
                }

                if (!isTitleCentered) {
                    val setTitlePadding = {
                        if (firstItemRect.left >= sidePadding) {
                            val space = (deviceConfig.callStaticMethodAs<Int>("getCellWidth") -
                                deviceConfig.callStaticMethodAs<Int>("getIconWidth")) / 2

                            val fixedValue = dp2px(4.0f)
                            titleText.setPaddingLeft(firstItemRect.left - sidePadding + space + fixedValue)
                            renameEdit.setPaddingLeft(firstItemRect.left - sidePadding + space + fixedValue)
                        }
                    }

                    if (firstItemRect.left == 0) {
                        content.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                if (content.isNotEmpty() && content.getChildAt(content.childCount - 1).bottom <= content.bottom) {
                                    content.getChildAt(0).getGlobalVisibleRect(firstItemRect)
                                    setTitlePadding()
                                }

                                content.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                        })
                    } else {
                        setTitlePadding()
                    }
                }
            }

            if (isTitleCentered) {
                titleText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                renameEdit.textAlignment = View.TEXT_ALIGNMENT_CENTER

                titleText.setPaddingLeft(titleText.paddingRight)
                renameEdit.setPaddingLeft(renameEdit.paddingRight)
            }
        }
    }

    private fun optFolderAnim() {
        folderAnimController.beforeHookedMethodByName("getGridItemTranslationY") {
            val controller = it.nonNullThisObject
            val endLocY = controller.getObjectFieldAs<Map<Int, Int>>("mEndLocY")
            val normalEditModeCloseState = controller.getIntField("mNormalEditModeCloseState")

            val y = it.argAs<Int>()
            val i = it.argAs<Int>(1)

            var r = y - endLocY[i / openedColumns * openedColumns] as Int
            if (normalEditModeCloseState != 0) {
                val gridEndLoc = controller.getObjectFieldAs<IntArray>("mGridEndLoc")
                if (gridEndLoc[1] != 0) {
                    r = y - gridEndLoc[1]
                }
            }

            it.result = r.toDouble()
        }
    }

    private val horizontalMargin by lazy {
        dp2px(prefs.getInt("home_layout_opened_folder_horizontal_margin", 0))
    }
}
