package com.xzakota.oshape.startup.rule.home.folder

import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.folder.MoreBigFolder.HM_FOLDER
import com.xzakota.oshape.startup.rule.home.folder.MoreBigFolder.VM_FOLDER
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderAnimController
import com.xzakota.oshape.startup.rule.home.shared.Folder.openedColumns

object OptFolderAnim : BaseHook() {
    override fun onHookLoad() {
        folderAnimController.beforeHookedMethodByName("initIconLoc") {
            val controller = it.nonNullThisObject
            val folderIconLocMap = controller.getObjectFieldAs<HashMap<Int, Int>>("mFolderIconLocMap")

            val gridColumn = openedColumns
            val preColumn = it.argAs<Int>(1)
            val itemType = it.argAs<Any>(2)

            var i = 0
            if (preColumn == gridColumn) {
                var position = 0
                val size = when (itemType) {
                    HM_FOLDER -> 3
                    VM_FOLDER -> 1
                    else -> preColumn * preColumn
                }
                while (i < size) {
                    folderIconLocMap[i] = i
                    position = i
                    i++
                }
                it.result = position
                return@beforeHookedMethodByName
            }

            if (itemType == VM_FOLDER) {
                val folderIcon = it.argAs<Any>(3)
                val animaFolderGridView = controller.getObjectFieldAs<Any>("mAnimaFolderGridView")
                val largeIconNum = folderIcon.callMethodAs<Int>("getLargeIconNum")
                val childCountWithoutCloudIcon = animaFolderGridView.callMethodAs<Int>("getChildCountWithoutCloudIcon")

                var j = 0
                for (k in 0 until largeIconNum) {
                    if (k < 1 || childCountWithoutCloudIcon <= gridColumn) {
                        folderIconLocMap[k] = k
                    } else {
                        folderIconLocMap[k] = k / preColumn * gridColumn + k % preColumn
                    }
                    j = k
                }

                if (childCountWithoutCloudIcon <= gridColumn) {
                    it.result = j
                    return@beforeHookedMethodByName
                }

                while (i < gridColumn - preColumn) {
                    j = largeIconNum + i
                    folderIconLocMap[j] = preColumn + i
                    i++
                }

                it.result = j
                return@beforeHookedMethodByName
            }
        }
    }
}
