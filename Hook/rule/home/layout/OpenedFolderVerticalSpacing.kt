package com.xzakota.oshape.startup.rule.home.layout

import android.widget.GridView
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.Folder.folder

object OpenedFolderVerticalSpacing : BaseHook() {
    override fun onHookLoad() {
        folder.afterHookedMethodByName("bind") { param ->
            param.nonNullThisObject.getObjectFieldAs<GridView>("mContent").verticalSpacing = verticalSpacing
        }
    }

    private val verticalSpacing by lazy {
        dp2px(prefs.getInt("home_layout_opened_folder_vertical_spacing", 0))
    }
}
