package com.xzakota.oshape.startup.rule.home.folder

import android.database.CursorWrapper
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.xzakota.android.extension.view.findViewById
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.extension.view.setPadding
import com.xzakota.android.extension.view.setPaddingTop
import com.xzakota.android.hook.core.loader.base.IMemberHookHandler
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.chainGetObject
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.hookMethod
import com.xzakota.android.hook.extension.reflect.constructorFinder
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.reflect.createAfterHooks
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.android.hook.startup.component.androidx.constraintlayout.ReConstraintLayout
import com.xzakota.android.hook.startup.component.androidx.constraintlayout.ReConstraintSet
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callStaticMethod
import com.xzakota.code.extension.createInstance
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStaticObjectField
import com.xzakota.code.extension.getStaticObjectFieldAs
import com.xzakota.code.extension.setFloatField
import com.xzakota.code.extension.setIntField
import com.xzakota.code.extension.setObjectField
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.api.DeviceConfig
import com.xzakota.oshape.startup.rule.home.api.FixedAspectRatioLottieAnimView
import com.xzakota.oshape.startup.rule.home.api.HomeApplication
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderCling
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIcon
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIcon2x2
import com.xzakota.oshape.startup.rule.home.shared.HomeShared
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.fixedAspectRatioLottieAnimView
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.launcher
import miuix.visual.check.BorderLayout
import miuix.visual.check.VisualCheckBox
import miuix.visual.check.VisualCheckedTextView
import kotlin.math.min

object MoreBigFolder : BaseHook() {
    const val HM_FOLDER = 0x20013
    const val VM_FOLDER = 0x20031

    override fun onHookLoad() {
        var hmGrid3CheckBox: View? = null
        var vmGrid3CheckBox: View? = null

        var hmGrid3PreviewContainerBg: View? = null
        var vmGrid3PreviewContainerBg: View? = null

        var hmGrid3PreviewContainer: View? = null
        var vmGrid3PreviewContainer: View? = null

        hookFolderIconPreviewContainer2X2()
        hookFolderIcon2X2()
        hookDBLoadTask()

//        folderInfo.beforeHookedMethod("isBigFolder") { param ->
//            val itemType = param.nonNullThisObject.getIntField("itemType")
//            if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
//                param.result = true
//            }
//        }

        folderInfo.beforeHookedMethod("canAcceptByHotSeats") { param ->
            val itemType = param.nonNullThisObject.getIntField("itemType")
            if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                param.result = false
            }
        }

        folderInfo.beforeHookedMethod("getPreviewMaxCount") { param ->
            val itemType = param.nonNullThisObject.getIntField("itemType")
            if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                param.result = 6
            }
        }

        folderInfo.beforeHookedMethod("getFolderGridSize") { param ->
            val itemType = param.nonNullThisObject.getIntField("itemType")
            if (itemType == HM_FOLDER) {
                param.result = "3*1"
            } else if (itemType == VM_FOLDER) {
                param.result = "1*3"
            }
        }

        folderCling.afterHookedMethod("onFinishInflate") { param ->
            val cling = param.thisObjectAs<ViewGroup>()
            val icon1 = LayoutInflater.from(cling.context)
                .inflate(getHosLayoutIdByName("folder_icon_2x2_9"), cling, false)
                .apply {
                    id = R.id.home_folder_cling_icon_3X1
                    isVisible = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            val icon2 = LayoutInflater.from(cling.context)
                .inflate(getHosLayoutIdByName("folder_icon_2x2_9"), cling, false)
                .apply {
                    id = R.id.home_folder_cling_icon_1X3
                    isVisible = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

            cling.addView(icon1, 2)
            cling.addView(icon2, 3)
        }

        folderCling.beforeHookedMethodByName("loadAnimFolderIcon") { param ->
            val itemType = param.argAs<Any>().getIntField("itemType")
            if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                val cling = param.thisObjectAs<View>()

                if (itemType == HM_FOLDER) {
                    cling.findViewById(R.id.home_folder_cling_icon_3X1) {
                        it.callMethod("setMIconColumCount", 3)
                    }
                } else {
                    cling.findViewById(R.id.home_folder_cling_icon_1X3) {
                        it.callMethod("setMIconColumCount", 1)
                    }
                }?.run {
                    val container = callMethodAs<View>("getMPreviewContainer")
                    if (container.tag != "is_set") {
                        callMethod("setMLargeIconNum", 2)
                        callMethod("setMItemsMaxCount", 6)
                        setTag(R.id.home_folder_icon_item_type, itemType)
                        setTag(R.id.home_folder_icon_large_item_count, 2)
                        setTag(R.id.home_folder_icon_max_item_count, 6)

                        container.callMethod("setMLargeIconNum", 2)
                        container.callMethod("setMItemsMaxCount", 6)
                        container.setFloatField("m2x2LargeItemMergeEdgePercent", 0.06f)
                        container.setFloatField("m2x2LargeItemMergeInnerPercent", 0.046f)
                        container.setFloatField("m2x2SmallItemMergeInnerPercent", 0.025f)

                        container.tag = "is_set"
                    }

                    callMethod("setUseCustomWidth", true)
                    callMethod("setAnimFolderIconTag")
                    visibility = View.VISIBLE
                    cling.setObjectField("mAnimFolderIcon", this)
                }

                param.result = null
            }
        }

        uninstallController.beforeHookedMethodByName("deleteItem") { param ->
            val info = param.args[0]
            if (info == null) {
                param.result = null
                return@beforeHookedMethodByName
            }

            val itemType = info.getIntField("itemType")
            if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                val controller = param.nonNullThisObject
                val launcher = controller.getObjectFieldAs<Any>("mLauncher")

                launcherModel.callStaticMethod("deleteUserFolderContentsFromDatabase", launcher, info)
                controller.callMethod("deleteItemFromMultiSelectMonitor", info)
                HomeShared.launcher.getDeclaredMethod("removeFolder", folderInfo).invoke(launcher, info)

                if (info.callMethodAs("isInWorkspace")) {
                    HomeShared.launcher.getDeclaredMethod("fillEmpty", itemInfo).invoke(launcher, info)
                }
                controller.callMethod("announceDeleted", info)
                param.result = null
            }
        }

        folderIconConvertSizeController.beforeHookedMethodByName("getFolderSpanXFromType") { param ->
            val itemType = param.argAs<Int>()
            if (itemType == HM_FOLDER) {
                param.result = 2
            } else if (itemType == VM_FOLDER) {
                param.result = 1
            }
        }

        folderIconConvertSizeController.beforeHookedMethodByName("getFolderSpanYFromType") { param ->
            val itemType = param.argAs<Int>()
            if (itemType == HM_FOLDER) {
                param.result = 1
            } else if (itemType == VM_FOLDER) {
                param.result = 2
            }
        }

        folderSheet.beforeHookedMethod("getFolderSizeByType") { param ->
            val itemType = param.nonNullThisObject.chainGetObjectAs<Int>("mFolderInfo.itemType")
            if (itemType == HM_FOLDER) {
                param.result = "3*1"
            } else if (itemType == VM_FOLDER) {
                param.result = "1*3"
            }
        }

        folderSheet.afterHookedMethod("initTitleAndRecommendAppsSwitchView") { param ->
            val sheet = param.thisObjectAs<View>()
            val defaultFolderCheckBox = sheet.getObjectFieldAs<View>("mDefaultFolderCheckBox")
            val folderCheckGroup = defaultFolderCheckBox.parentGroup ?: return@afterHookedMethod

            hmGrid3CheckBox = initFolderCheckBox(sheet, R.string.hm_grid, "hm_grid_checkbox").also {
                folderCheckGroup.addView(it)
            }

            vmGrid3CheckBox = initFolderCheckBox(sheet, R.string.vm_grid, "vm_grid_checkbox").also {
                folderCheckGroup.addView(it)
            }

            val folderBg = sheet.getObjectFieldAs<View>("mFolderPickerSelectBigFolderBg")
            val folderPicker = folderBg.parentGroup ?: return@afterHookedMethod
            var indexOf = folderPicker.indexOfChild(folderBg)

            hmGrid3PreviewContainerBg = initFolderPreviewContainerBg(
                folderPicker, R.id.home_folder_preview_container_bg_hm, HM_FOLDER
            ).also {
                folderPicker.addView(it, indexOf + 1)
            }

            vmGrid3PreviewContainerBg = initFolderPreviewContainerBg(
                folderPicker, R.id.home_folder_preview_container_bg_vm, VM_FOLDER
            ).also {
                folderPicker.addView(it, indexOf + 2)
            }

            val folderImg = sheet.getObjectFieldAs<View>("mFolderPickerSelectBigFolderImg2x2_4")
            indexOf = folderPicker.indexOfChild(folderImg)

            hmGrid3PreviewContainer = initFolderPreviewContainer(
                folderPicker, R.id.home_folder_preview_container_hm, HM_FOLDER
            ).also {
                folderPicker.addView(it, indexOf + 1)
            }

            vmGrid3PreviewContainer = initFolderPreviewContainer(
                folderPicker, R.id.home_folder_preview_container_vm, VM_FOLDER
            ).also {
                folderPicker.addView(it, indexOf + 2)
            }

            initFolderPreviewContainerLayoutParam(folderPicker)
        }

        folderSheet.afterHookedMethod("setCheckedBox", Int::class.java) { param ->
            val hmGrid3 = hmGrid3CheckBox
            val vmGrid3 = vmGrid3CheckBox
            if (hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val itemType = param.args[0]
            hmGrid3.callMethod("setChecked", itemType == HM_FOLDER)
            vmGrid3.callMethod("setChecked", itemType == VM_FOLDER)
        }

        folderSheet.afterHookedMethod(
            "updateSelectBorderLayoutParams", fixedAspectRatioLottieAnimView, Int::class.java
        ) { param ->
            val hmGrid3 = hmGrid3CheckBox
            val vmGrid3 = vmGrid3CheckBox
            if (hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val sheet = param.nonNullThisObject
            if (param.args[0] == sheet.getObjectFieldAs<View>("mDefaultFolderSelectBorder")) {
                param.callOriginalMethod(hmGrid3.findViewWithTag("hm_grid_checkbox_bg"), param.args[1])
                param.callOriginalMethod(vmGrid3.findViewWithTag("vm_grid_checkbox_bg"), param.args[1])
            }
        }

        folderSheet.afterHookedMethod("onDarkModeChange") { param ->
            val hmGrid3Bg = hmGrid3PreviewContainerBg
            val vmGrid3Bg = vmGrid3PreviewContainerBg
            val hmGrid3 = hmGrid3CheckBox
            val vmGrid3 = vmGrid3CheckBox
            if (hmGrid3Bg == null || vmGrid3Bg == null || hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val sheet = param.thisObjectAs<View>()
            val context = sheet.context

            hmGrid3Bg.background = folderIcon4x4NormalBackgroundDrawable.createInstance(context, false) as Drawable
            hmGrid3Bg.background.setFloatField("mRadius", 46F)
            vmGrid3Bg.background = folderIcon4x4NormalBackgroundDrawable.createInstance(context, false) as Drawable
            vmGrid3Bg.background.setFloatField("mRadius", 46F)

            sheet.callMethod(
                "handleVisualCheckedTextViewColorDarkModeSyncInternal",
                hmGrid3.callMethod("isChecked"), hmGrid3.findViewWithTag("hm_grid_checkbox_name")
            )
            sheet.callMethod(
                "handleVisualCheckedTextViewColorDarkModeSyncInternal",
                vmGrid3.callMethod("isChecked"), vmGrid3.findViewWithTag("vm_grid_checkbox_name")
            )
        }

        folderSheet.afterHookedMethod("setFolderPickerDarkModeColor") { param ->
            val hmGrid3 = hmGrid3CheckBox
            val vmGrid3 = vmGrid3CheckBox
            if (hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val sheet = param.thisObjectAs<View>()
            hmGrid3.findViewWithTag<ImageView>("hm_grid_checkbox_bg").setImageDrawable(
                sheet.callMethodAs(
                    "getDrawableWithTheme", sheet.context,
                    R.drawable.ic_big_folder_2x1_6_select_border_bg,
                    R.drawable.ic_big_folder_2x1_6_select_border_bg_dark
                )
            )
            vmGrid3.findViewWithTag<ImageView>("vm_grid_checkbox_bg").setImageDrawable(
                sheet.callMethodAs(
                    "getDrawableWithTheme", sheet.context,
                    R.drawable.ic_big_folder_1x2_6_select_border_bg,
                    R.drawable.ic_big_folder_1x2_6_select_border_bg_dark
                )
            )
        }

        folderSheet.afterHookedMethodByName("onCheckedChanged") { param ->
            val hmGrid3 = hmGrid3PreviewContainer
            val vmGrid3 = vmGrid3PreviewContainer
            if (hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethodByName
            }

            val sheet = param.thisObjectAs<View>()
            if (sheet.getObjectField("mAppPredictSlidingButton") == param.args[0]) {
                val folderInfo = sheet.getObjectField("mFolderInfo")
                val serialExecutor = sheet.getObjectField("mSerialExecutor")
                val iconCache = HomeApplication.getIconCache()

                hmGrid3.callMethod("loadItemIcons", folderInfo, iconCache, param.args[1], serialExecutor, true)
                vmGrid3.callMethod("loadItemIcons", folderInfo, iconCache, param.args[1], serialExecutor, true)
            }
        }

        folderSheet.afterHookedMethod("initPreviewIcon") { param ->
            val hmGrid3Bg = hmGrid3PreviewContainerBg
            val vmGrid3Bg = vmGrid3PreviewContainerBg
            val hmGrid3 = hmGrid3PreviewContainer
            val vmGrid3 = vmGrid3PreviewContainer
            if (hmGrid3Bg == null || vmGrid3Bg == null || hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val sheet = param.thisObjectAs<View>()
            val context = sheet.context
            val folderInfo = sheet.getObjectFieldAs<Any>("mFolderInfo")
            val folderPickerSelectBigFolderBg = sheet.getObjectFieldAs<View>("mFolderPickerSelectBigFolderBg")
            val serialExecutor = sheet.getObjectFieldAs<Any>("mSerialExecutor")
            val isChecked = sheet.getObjectFieldAs<Any>("mAppPredictSlidingButton").callMethodAs<Boolean>("isChecked")
            val iconCache = HomeApplication.getIconCache()

            hmGrid3Bg.background = folderIcon4x4NormalBackgroundDrawable.createInstance(context, false) as Drawable
            hmGrid3Bg.background.setFloatField("mRadius", 46F)
            vmGrid3Bg.background = folderIcon4x4NormalBackgroundDrawable.createInstance(context, false) as Drawable
            vmGrid3Bg.background.setFloatField("mRadius", 46F)

            repeat(min(6, folderInfo.callMethodAs("count"))) {
                hmGrid3.callMethod("addPreView", folderPreviewIconView.createInstance(context))
                vmGrid3.callMethod("addPreView", folderPreviewIconView.createInstance(context))
            }

            hmGrid3.callMethod("setFolderIconPlaceholderDrawableMatchingWallpaperColor")
            vmGrid3.callMethod("setFolderIconPlaceholderDrawableMatchingWallpaperColor")

            hmGrid3.callMethod("loadItemIcons", folderInfo, iconCache, isChecked, serialExecutor, false)
            vmGrid3.callMethod("loadItemIcons", folderInfo, iconCache, isChecked, serialExecutor, false)

            val itemType = folderInfo.getIntField("itemType")
            val isHMFolder = itemType == HM_FOLDER
            val isVMFolder = itemType == VM_FOLDER

            if (isHMFolder || isVMFolder) {
                sheet.callMethod("setDefaultFolderGone")
                sheet.callMethod("setBigFolderGone2x2_4")
                sheet.callMethod("setBigFolderGone2x2_9")
                folderPickerSelectBigFolderBg.isVisible = false
            }

            hmGrid3Bg.isVisible = isHMFolder
            vmGrid3Bg.isVisible = isVMFolder
            hmGrid3.isVisible = isHMFolder
            vmGrid3.isVisible = isVMFolder
        }

        folderSheet.methodFinder().filter {
            name in arrayOf("setDefaultFolderVisible", "setBigFolderVisible2x2_4", "setBigFolderVisible2x2_9")
        }.toList().createAfterHooks {
            val hmGrid3Bg = hmGrid3PreviewContainerBg
            val vmGrid3Bg = vmGrid3PreviewContainerBg
            val hmGrid3 = hmGrid3PreviewContainer
            val vmGrid3 = vmGrid3PreviewContainer
            if (hmGrid3Bg == null || vmGrid3Bg == null || hmGrid3 == null || vmGrid3 == null) {
                return@createAfterHooks
            }

            hmGrid3Bg.isVisible = false
            vmGrid3Bg.isVisible = false
            hmGrid3.isVisible = false
            vmGrid3.isVisible = false
        }

        folderSheet.afterHookedMethod("onClick", View::class.java) { param ->
            val view = param.argAs<View>()
            val tag = view.tag as? String ?: return@afterHookedMethod

            val hmGrid3Bg = hmGrid3PreviewContainerBg
            val vmGrid3Bg = vmGrid3PreviewContainerBg
            val hmGrid3 = hmGrid3PreviewContainer
            val vmGrid3 = vmGrid3PreviewContainer
            if (hmGrid3Bg == null || vmGrid3Bg == null || hmGrid3 == null || vmGrid3 == null) {
                return@afterHookedMethod
            }

            val sheet = param.nonNullThisObject
            val folderPickerSelectBigFolderBg = sheet.getObjectFieldAs<View>("mFolderPickerSelectBigFolderBg")
            val folderPickerAppPredictExposed = sheet.getObjectFieldAs<View>("mFolderPickerAppPredictExposed")
            val appPredictSlidingButton = sheet.getObjectFieldAs<View>("mAppPredictSlidingButton")

            if (tag.contains("grid_checkbox")) {
                sheet.callMethod("setDefaultFolderGone")
                sheet.callMethod("setBigFolderGone2x2_4")
                sheet.callMethod("setBigFolderGone2x2_9")
                folderPickerSelectBigFolderBg.isVisible = false
                folderPickerAppPredictExposed.isClickable = true
                appPredictSlidingButton.isEnabled = true
                sheet.callMethod("internationalNotSupportAppPredict")
            } else {
                return@afterHookedMethod
            }

            when {
                tag.startsWith("hm_grid_checkbox") -> {
                    hmGrid3Bg.isVisible = true
                    vmGrid3Bg.isVisible = false
                    hmGrid3.isVisible = true
                    vmGrid3.isVisible = false
                    sheet.setIntField("mFolderType", HM_FOLDER)
                    sheet.callMethod("setCheckedBox", HM_FOLDER)
                }

                tag.startsWith("vm_grid_checkbox") -> {
                    hmGrid3Bg.isVisible = false
                    vmGrid3Bg.isVisible = true
                    hmGrid3.isVisible = false
                    vmGrid3.isVisible = true
                    sheet.setIntField("mFolderType", VM_FOLDER)
                    sheet.callMethod("setCheckedBox", VM_FOLDER)
                }
            }
        }

        folderSheet.afterHookedMethod("onDetachedFromWindow") {
            hmGrid3CheckBox = null
            vmGrid3CheckBox = null

            hmGrid3PreviewContainerBg = null
            vmGrid3PreviewContainerBg = null

            hmGrid3PreviewContainer = null
            vmGrid3PreviewContainer = null
        }
    }

    private fun hookFolderIconPreviewContainer2X2() {
        folderIconPreviewContainer2X2_9.constructorFinder().filterByParamCount(3).first().createAfterHook { param ->
            val itemType = param.argAs<Int>(2)
            if (itemType != HM_FOLDER && itemType != VM_FOLDER) {
                return@createAfterHook
            }

            val container = param.thisObjectAs<View>()
            container.setTag(R.id.home_folder_icon_item_type, itemType)

            when (itemType) {
                HM_FOLDER, VM_FOLDER -> {
                    container.callMethod("setMLargeIconNum", 2)
                    container.callMethod("setMItemsMaxCount", 6)
                    container.setFloatField("m2x2LargeItemMergeEdgePercent", 0.06f)
                    container.setFloatField("m2x2LargeItemMergeInnerPercent", 0.046f)
                    container.setFloatField("m2x2SmallItemMergeInnerPercent", 0.025f)
                    container.setTag(R.id.home_folder_icon_large_item_count, 2)
                    container.setTag(R.id.home_folder_icon_max_item_count, 6)
                }
            }
        }

        folderIconPreviewContainer2X2_9.beforeHookedMethodByName("preMeasure2x2") { param ->
            val container = getAddedFolderContainer(param) ?: return@beforeHookedMethodByName

            container.preMeasure(param.argAs(), param.argAs(1))
            param.result = null
        }

        folderIconPreviewContainer2X2_9.beforeHookedMethod("preSetup2x2") { param ->
            val container = getAddedFolderContainer(param) ?: return@beforeHookedMethod

            container.onPreSetup()
            param.result = null
        }

        folderIconPreviewContainer2X2_9.beforeHookedMethod("getSmallItemsRectF") { param ->
            val container = getAddedFolderContainer(param) ?: return@beforeHookedMethod

            param.result = container.getSmallItemsRectF()
        }
    }

    private fun hookFolderIcon2X2() {
        folderIcon2x2.beforeHookedMethod("createOrRemoveView") { param ->
            val folder = param.thisObjectAs<View>()
            val itemType = folder.chainGetObject("mInfo.itemType")
            val container = folder.callMethodAs<View>("getMPreviewContainer")
            if (container.tag != "is_set") {
                if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                    container.callMethod("setMLargeIconNum", 2)
                    container.callMethod("setMItemsMaxCount", 6)
                    container.setFloatField("m2x2LargeItemMergeEdgePercent", 0.06f)
                    container.setFloatField("m2x2LargeItemMergeInnerPercent", 0.046f)
                    container.setFloatField("m2x2SmallItemMergeInnerPercent", 0.025f)
                }

                container.tag = "is_set"
            }
        }

        folderIcon.hookMethod("fromXml", launcher, ViewGroup::class.java, folderInfo, Boolean::class.java) {
            var handler: IMemberHookHandler? = null

            before { param ->
                val itemType = param.args[2]?.getIntField("itemType") ?: return@before
                if (itemType == HM_FOLDER || itemType == VM_FOLDER) {
                    handler = folderInfo.beforeHookedMethod("notifyDataSetChanged") {
                        val icon = it.nonNullThisObject.getObjectFieldAs<View>("icon")
                        icon.callMethod("setMLargeIconNum", 2)
                        icon.callMethod("setMItemsMaxCount", 6)
                        icon.setTag(R.id.home_folder_icon_item_type, itemType)
                        icon.setTag(R.id.home_folder_icon_large_item_count, 2)
                        icon.setTag(R.id.home_folder_icon_max_item_count, 6)

                        when (itemType) {
                            HM_FOLDER -> icon.callMethod("setMIconColumCount", 3)
                            VM_FOLDER -> icon.callMethod("setMIconColumCount", 1)
                        }
                    }

                    param.result = folderIcon.callStaticMethod(
                        "fromXml",
                        getHosLayoutIdByName("folder_icon_2x2_9"),
                        param.args[0],
                        param.args[1],
                        param.args[2]
                    )
                }
            }

            after {
                handler?.unhook()
            }
        }

        launcherFolder2x2IconContainer.beforeHookedMethod("onMeasure", Int::class.java, Int::class.java) { param ->
            val container = param.thisObjectAs<View>()
            if (container.tag == "is_set") {
                return@beforeHookedMethod
            }

            val parentGroup = container.parentGroup?.parentGroup ?: return@beforeHookedMethod
            val itemType = parentGroup.getTag(R.id.home_folder_icon_item_type) as? Int ?: return@beforeHookedMethod

            when (itemType) {
                HM_FOLDER -> {
                    container.setIntField("cellX", 2)
                    container.setIntField("cellY", 1)
                }

                VM_FOLDER -> {
                    container.setIntField("cellX", 1)
                    container.setIntField("cellY", 2)
                }
            }

            container.tag = "is_set"
        }
    }

    private fun hookDBLoadTask() {
        launcherModelLoaderTask.beforeHookedMethod("getFolderItemCursor") { param ->
            val task = param.nonNullThisObject

            val uri = launcherSettingsFavorites.getStaticObjectFieldAs<Any>("CONTENT_URI")
            val c = itemQuery.getStaticObjectField("COLUMNS")

            param.result = task.callMethod(
                "fromQuery", uri, c, "itemType=? OR itemType=? OR itemType=? OR itemType=? OR itemType=?",
                arrayOf("2", "21", "22", "$HM_FOLDER", "$VM_FOLDER"),
                "cellY ASC, cellX ASC, itemType ASC"
            )
        }

        launcherModelLoaderTask.beforeHookedMethod("getFirstScreenItemCursor", Int::class.java) { param ->
            val task = param.nonNullThisObject
            val order = param.argAs<Int>()

            val part: String
            val args: Array<String>
            if (DeviceConfig.isInFoldDeviceLargeScreen(task.getObjectFieldAs("mContext"))) {
                part = "screenOrder BETWEEN ? AND ? "
                args = arrayOf(
                    "$order", "${order + 1}", "-100", "$order", "${order + 1}",
                    "2", "21", "22", "$HM_FOLDER", "$VM_FOLDER"
                )
            } else {
                part = "screenOrder=? "
                args = arrayOf(
                    "$order", "-100", "$order",
                    "2", "21", "22", "$HM_FOLDER", "$VM_FOLDER"
                )
            }

            param.result = task.callMethod(
                "fromQuery",
                launcherSettingsFavorites.callStaticMethod(
                    "getJoinContentUri", " LEFT JOIN screens ON favorites.screen=screens._id"
                ),
                itemInfo.callStaticMethod("getColumnsWithScreenTypeAndOrder"),
                "$part AND container=? or container in(select favorites._id from favorites JOIN screens ON favorites.screen=screens._id where $part and itemType in(?,?,?,?,?)) ",
                args,
                " case when itemType in ( 2,21,22,$HM_FOLDER,$VM_FOLDER) then 0 else 1 end,container ASC, cellY ASC, cellX ASC "
            )
        }

        launcherModelLoaderTask.beforeHookedMethod("getOtherScreenItemCursor", Int::class.java) { param ->
            val task = param.nonNullThisObject
            val order = param.argAs<Int>()

            val part: String
            val args: Array<String>
            if (DeviceConfig.isInFoldDeviceLargeScreen(task.getObjectFieldAs("mContext"))) {
                part = "screenOrder NOT BETWEEN ? AND ? "
                args = arrayOf(
                    "$order", "${order + 1}", "-100", "$order", "${order + 1}",
                    "2", "21", "22", "$HM_FOLDER", "$VM_FOLDER"
                )
            } else {
                part = "screenOrder<>? "
                args = arrayOf(
                    "$order", "-100", "$order",
                    "2", "21", "22", "$HM_FOLDER", "$VM_FOLDER"
                )
            }

            param.result = task.callMethod(
                "fromQuery",
                launcherSettingsFavorites.callStaticMethod(
                    "getJoinContentUri", " LEFT JOIN screens ON favorites.screen=screens._id"
                ),
                itemInfo.callStaticMethod("getColumnsWithScreenTypeAndOrder"),
                "$part AND container=? or container in(select favorites._id from favorites JOIN screens ON favorites.screen=screens._id where $part and itemType in(?,?,?,?,?)) ",
                args,
                " case when itemType in ( 2,21,22,$HM_FOLDER,$VM_FOLDER) then 0 else 1 end,container ASC, screens.screenOrder ASC, cellY ASC, cellX ASC "
            )
        }

        launcherModelLoaderTask.beforeHookedMethodByName("loadItems") { param ->
            val task = param.nonNullThisObject
            val cursor = param.argAs<CursorWrapper?>()
            if (cursor == null) {
                param.result = null
                return@beforeHookedMethodByName
            }

            try {
                while (!task.getBooleanField("mStopped") && !cursor.isClosed && cursor.moveToNext()) {
                    try {
                        when (val type = cursor.getInt(8)) {
                            0, 1, 11, 14, 17 -> task.callMethod(
                                "loadShortcut", cursor, type, param.args[1], param.args[2]
                            )

                            2, 21, 22, HM_FOLDER, VM_FOLDER -> task.callMethod("loadFolder", cursor)
                            4 -> task.callMethod("loadAppWidget", cursor)
                            5 -> task.callMethod("loadGadget", cursor)
                            19 -> task.callMethod("loadMaMl", cursor)
                            23 -> task.callMethod("loadServiceDelivery", cursor)
                        }
                    } catch (e: Throwable) {
                        Log.w("Launcher.Model", "Desktop items loading interrupted: ", e)
                    }
                }
            } catch (e: Throwable) {
                Log.w("Launcher.Model", "Desktop items loading interrupted moveToNext: ", e)
            } finally {
                cursor.close()
            }

            param.result = null
        }
    }

    private fun initFolderCheckBox(
        sheet: View,
        titleResId: Int,
        folderTag: String
    ): View = VisualCheckBox(sheet.context, null).transform<LinearLayout> {
        tag = folderTag
        orientation = LinearLayout.VERTICAL
        isFocusable = true
        clipChildren = false
        clipToPadding = false
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val onClickListener = sheet as View.OnClickListener
        setOnClickListener(onClickListener)

        addView(
            BorderLayout(context, null).let {
                it.transform<LinearLayout> {
                    it.setBackground(getHostDrawable("folder_picker_visualcheckbox_bg_shape_select"))
                    setOnClickListener(onClickListener)

                    addView(
                        FrameLayout(context).apply {
                            setPadding(getHostDimensionPixelSize("folder_border_layout_padding"))

                            val bgWidth = getHostDimensionPixelSize("folder_picker_folder_bg_width")
                            addView(
                                FixedAspectRatioLottieAnimView(context, null).transform<ImageView> {
                                    tag = folderTag + "_bg"
                                    scaleType = ImageView.ScaleType.FIT_XY
                                    isDuplicateParentStateEnabled = true
                                    setImageDrawable(getHostDrawable("ic_default_folder_select_border_bg"))
                                    setOnClickListener(onClickListener)
                                },
                                FrameLayout.LayoutParams(bgWidth, bgWidth).apply {
                                    gravity = Gravity.AXIS_SPECIFIED
                                }
                            )
                        },
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    )
                }
            },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        addView(
            VisualCheckedTextView(context, null).transform<TextView> {
                tag = folderTag + "_name"
                text = getHostString(titleResId)
                typeface = Typeface.create("mipro-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, getHostDimensionPixelSize("folder_picker_options_text_size").toFloat()
                )
                val list = getHostColorStateList("settings_visual_check_box_title_color")
                setIntField("mCheckedColor", list.getColorForState(intArrayOf(android.R.attr.state_checked), 0))
                setIntField("mUncheckedColor", list.defaultColor)
                setPaddingTop(getHostDimension("folder_picker_visual_check_box_title_marginTop").toInt())
                setOnClickListener(onClickListener)
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.AXIS_SPECIFIED
            }
        )
    }

    private fun initFolderPreviewContainerBg(parent: ViewGroup, bgId: Int, itemType: Int): View {
        val bg = ImageView(parent.context) as View
        bg.id = bgId

        val size = getHostDimensionPixelSize("big_folder_picker_fragment_checked_folder_bg_width")
        bg.layoutParams = ViewGroup.LayoutParams(size, size).apply {
            if (itemType == HM_FOLDER) {
                height = height / 3 + 10
            } else if (itemType == VM_FOLDER) {
                width = width / 3 + 10
            }
        }

        return bg
    }

    private fun initFolderPreviewContainer(parent: ViewGroup, containerId: Int, itemType: Int): View {
        val container = folderIconPreviewContainer2X2_9.createInstance(parent.context, null, itemType) as View
        container.id = containerId

        val size = getHostDimensionPixelSize("big_folder_picker_fragment_checked_img_width")
        container.layoutParams = ViewGroup.LayoutParams(size, size).apply {
            if (itemType == HM_FOLDER) {
                height /= 3
            } else if (itemType == VM_FOLDER) {
                width /= 3
            }
        }

        return container
    }

    private fun initFolderPreviewContainerLayoutParam(parent: ViewGroup) {
        val wallpaperBgId = getHostIdByName("folder_picker_select_wallpaper_bg")
        val defaultFolderBgId = getHostIdByName("folder_picker_select_default_folder_bg")
        val folderPicker = ReConstraintLayout(parent)

        ReConstraintSet().apply {
            clone(folderPicker)

            var containerId = R.id.home_folder_preview_container_bg_hm
            connect(containerId, ConstraintSet.TOP, wallpaperBgId, ConstraintSet.TOP, 0)
            connect(containerId, ConstraintSet.BOTTOM, wallpaperBgId, ConstraintSet.BOTTOM, 0)
            connect(containerId, ConstraintSet.START, wallpaperBgId, ConstraintSet.START, 0)
            connect(containerId, ConstraintSet.END, wallpaperBgId, ConstraintSet.END, 0)

            containerId = R.id.home_folder_preview_container_hm
            connect(containerId, ConstraintSet.TOP, defaultFolderBgId, ConstraintSet.TOP, 0)
            connect(containerId, ConstraintSet.BOTTOM, defaultFolderBgId, ConstraintSet.BOTTOM, 0)
            connect(containerId, ConstraintSet.START, defaultFolderBgId, ConstraintSet.START, 0)
            connect(containerId, ConstraintSet.END, defaultFolderBgId, ConstraintSet.END, 0)

            containerId = R.id.home_folder_preview_container_bg_vm
            connect(containerId, ConstraintSet.TOP, wallpaperBgId, ConstraintSet.TOP, 0)
            connect(containerId, ConstraintSet.BOTTOM, wallpaperBgId, ConstraintSet.BOTTOM, 0)
            connect(containerId, ConstraintSet.START, wallpaperBgId, ConstraintSet.START, 0)
            connect(containerId, ConstraintSet.END, wallpaperBgId, ConstraintSet.END, 0)

            containerId = R.id.home_folder_preview_container_vm
            connect(containerId, ConstraintSet.TOP, defaultFolderBgId, ConstraintSet.TOP, 0)
            connect(containerId, ConstraintSet.BOTTOM, defaultFolderBgId, ConstraintSet.BOTTOM, 0)
            connect(containerId, ConstraintSet.START, defaultFolderBgId, ConstraintSet.START, 0)
            connect(containerId, ConstraintSet.END, defaultFolderBgId, ConstraintSet.END, 0)
        }.applyTo(folderPicker)
    }

    private fun getAddedFolderContainer(param: IMemberHookParam): BaseFolderPreviewContainer? {
        val container = param.thisObjectAs<View>()
        val itemType1 = container.getTag(R.id.home_folder_icon_item_type) as? Int
        val itemType2 = container.parentGroup(3)?.getTag(R.id.home_folder_icon_item_type) as? Int

        return when (itemType1 ?: itemType2) {
            HM_FOLDER -> FolderPreviewContainer3X1(container)
            VM_FOLDER -> FolderPreviewContainer1X3(container)
            else -> null
        }
    }

    private val folderSheet by lazy {
        loadClass("com.miui.home.launcher.folder.FolderSheet")
    }

    private val itemInfo by lazy {
        loadClass("com.miui.home.launcher.ItemInfo")
    }

    private val folderInfo by lazy {
        loadClass("com.miui.home.launcher.FolderInfo")
    }

    private val folderIcon4x4NormalBackgroundDrawable by lazy {
        loadClass("com.miui.home.launcher.folder.FolderIcon4x4NormalBackgroundDrawable")
    }

    private val launcherFolder2x2IconContainer by lazy {
        loadClass("com.miui.home.launcher.folder.LauncherFolder2x2IconContainer")
    }

    private val folderIconPreviewContainer2X2_9 by lazy {
        loadClass("com.miui.home.launcher.folder.FolderIconPreviewContainer2X2_9")
    }

    private val folderPreviewIconView by lazy {
        loadClass("com.miui.home.launcher.folder.FolderPreviewIconView")
    }

    private val folderIconConvertSizeController by lazy {
        loadClass("com.miui.home.launcher.convertsize.FolderIconConvertSizeController")
    }

    private val uninstallController by lazy {
        loadClass("com.miui.home.launcher.uninstall.UninstallController")
    }

    private val launcherModel by lazy {
        loadClass("com.miui.home.launcher.LauncherModel")
    }

    private val launcherModelLoaderTask by lazy {
        loadClass("com.miui.home.launcher.LauncherModel\$LoaderTask")
    }

    private val launcherSettingsFavorites by lazy {
        loadClass("com.miui.home.launcher.LauncherSettings\$Favorites")
    }

    private val itemQuery by lazy {
        loadClass("com.miui.home.launcher.ItemQuery")
    }
}
