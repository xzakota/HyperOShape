package com.xzakota.oshape.startup.rule.securitycenter

import android.view.Menu
import android.view.MenuInflater
import com.xzakota.android.extension.app.openApplication
import com.xzakota.android.extension.content.getIdByName
import com.xzakota.android.extension.content.getString
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.startup.component.androidx.fragment.ReFragment
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.securitycenter.shared.ApplicationsDetails.applicationsDetailsFragment

object OptimizeMenuItems : BaseHook() {
    private const val ID_MENU_ITEM_OPEN_APP = 0x1000

    override fun onHookLoad() {
        applicationsDetailsFragment.afterHookedMethod(
            "onCreateOptionsMenu", Menu::class.java, MenuInflater::class.java
        ) { param ->
            val fragment = ReFragment(param.nonNullThisObject)
            val activity = fragment.requireActivity()

            val menu = param.argAs<Menu>()
            val reportMenu = menu.findItem(activity.getIdByName("app_report")).also {
                it.isVisible = false
            }

            menu.add(
                reportMenu.groupId, ID_MENU_ITEM_OPEN_APP, reportMenu.order,
                activity.getString("select_app_title_default")
            ).apply {
                setOnMenuItemClickListener {
                    activity.intent.getStringExtra("package_name")?.let {
                        activity.openApplication(it)
                    }

                    true
                }
            }
        }
    }
}
