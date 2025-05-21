package com.xzakota.oshape.startup.rule.home

import android.view.View
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.hook.core.provider.XHookBridgeProvider.PRIORITY_HIGHEST
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.hookMethod
import com.xzakota.android.hook.extension.reflect.createBeforeHook
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIcon2x2

object TrueXSquareGrid : BaseHook() {
    override fun onHookLoad() {
        folderIcon2x2.beforeHookedMethod("createOrRemoveView") { param ->
            val folder = param.thisObjectAs<View>()
            val info = folder.getObjectFieldAs<Any>("mInfo")
            val container = folder.callMethodAs<Any>("getMPreviewContainer")

            val childCount1 = info.callMethodAs<Int>("count")
            val childCount2 = container.callMethodAs<Int>("getMRealPvChildCount")
            if (childCount1 == childCount2) {
                return@beforeHookedMethod
            }

            val iconNum = if (folder.getTag(R.id.home_folder_icon_item_type) != null) {
                folder.getTag(R.id.home_folder_icon_large_item_count) as Int + 1
            } else {
                Character.getNumericValue(container::class.java.simpleName.last())
            }
            if (childCount2 - iconNum >= 3) {
                return@beforeHookedMethod
            }

            container.callMethod(
                "setMItemsMaxCount",
                if (childCount1 <= iconNum) {
                    iconNum
                } else {
                    iconNum + 3
                }
            )
        }

        folderIcon2x2.hookMethod("addItemOnclickListener") {
            var oldLargeIconNum = 0

            before { param ->
                val folder = param.thisObjectAs<View>()
                val container = folder.callMethodAs<Any>("getMPreviewContainer")
                val childCount = container.callMethodAs<Int>("getMRealPvChildCount")

                val iconNum = if (folder.getTag(R.id.home_folder_icon_item_type) != null) {
                    folder.getTag(R.id.home_folder_icon_large_item_count) as Int + 1
                } else {
                    Character.getNumericValue(container::class.java.simpleName.last())
                }
                oldLargeIconNum = folder.callMethodAs("getMLargeIconNum")
                folder.callMethod(
                    "setMLargeIconNum",
                    if (childCount <= iconNum) {
                        iconNum
                    } else {
                        iconNum - 1
                    }
                )
            }

            after { param ->
                param.nonNullThisObject.callMethod("setMLargeIconNum", oldLargeIconNum)
            }
        }

        val hookFolderIconPreviewContainer = { size: Int ->
            loadClass("com.miui.home.launcher.folder.FolderIconPreviewContainer2X2_$size").methodFinder()
                .filterByName("preSetup2x2")
                .first()
                .createBeforeHook(PRIORITY_HIGHEST) { param ->
                    val container = param.thisObjectAs<View>()
                    val childCount = container.callMethodAs<Int>("getMRealPvChildCount")
                    val folder = container.parentGroup(3)

                    val iconNum = if (container.getTag(R.id.home_folder_icon_item_type) != null) {
                        container.getTag(R.id.home_folder_icon_large_item_count) as Int + 1
                    } else if (folder?.getTag(R.id.home_folder_icon_item_type) != null) {
                        folder.getTag(R.id.home_folder_icon_large_item_count) as Int + 1
                    } else {
                        size
                    }

                    container.callMethod(
                        "setMItemsMaxCount",
                        if (childCount <= iconNum) {
                            iconNum
                        } else {
                            iconNum + 3
                        }
                    )

                    container.callMethod(
                        "setMLargeIconNum",
                        if (childCount <= iconNum) {
                            iconNum
                        } else {
                            iconNum - 1
                        }
                    )
                }
        }

        hookFolderIconPreviewContainer(4)
        hookFolderIconPreviewContainer(9)
    }
}
