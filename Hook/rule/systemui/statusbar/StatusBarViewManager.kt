package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.content.Context
import android.view.ViewGroup
import com.android.internal.statusbar.StatusBarIcon
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedFirstConstructor
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.code.extension.util.take
import com.xzakota.oshape.application.SystemShared.isMoreHyperOS3
import com.xzakota.oshape.startup.base.BaseRule
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.api.StatusBarIconHolder
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.batteryStateMethod
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.centralSurfacesImpl
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.iconManager
import com.xzakota.reflect.extension.callMethodAs
import com.xzakota.reflect.extension.findField
import com.xzakota.reflect.extension.findStaticField
import com.xzakota.reflect.extension.instanceOf
import com.xzakota.reflect.extension.typeInstanceOf

object StatusBarViewManager : BaseRule(false) {
    private val iconMap = mutableMapOf<String, IStatusBarView>()

    override fun onRuleLoad() {
        queryIcon {
            if (iconMap.isEmpty()) {
                return
            }
        }

        getClass("com.android.systemui.statusbar.phone.ui.StatusBarIconList").beforeHookedFirstConstructor { param ->
            val slots = param.args<Array<String>>().toMutableList()
            iconMap.forEach { (k, v) ->
                val index = v.insertIndexOf(slots)
                slots.add(index, k)
                logD("OnViewPreAdd ${v.getSlot()} -> $index")
            }

            param.args[0] = slots.toTypedArray()
        }

        centralSurfacesImpl.afterHookedMethod("start") { param ->
            iconMap.values.forEach {
                val iconController = MiuiStub.myProvider.statusBarIconController
                val slot = it.getSlot()

                if (it is IStaticStatusBarIcon) {
                    iconController.setIcon(it.description, slot, it.iconResID)
                    iconController.setIconVisibility(slot, false)
                } else {
                    iconController.setIcon(slot, StatusBarIconHolder())
                }
            }
        }

        iconManager.beforeHookedMethodByName("addHolder") { param ->
            val slot = param.args<String>(1)
            iconMap.take(slot) {
                if (it is IDisplayableStatusBarView) {
                    val manager = param.nnThis
                    val context = manager.findField<Context>("mContext").get()
                    val group = manager.findField<ViewGroup>("mGroup").get()

                    val controller = if (it.isHelperInitialized()) {
                        it.javaClass.typeInstanceOf()
                    } else {
                        it
                    }
                    val content = controller.construct(context)
                    group.addView(
                        content,
                        param.args(),
                        if (isMoreHyperOS3) {
                            manager.callMethodAs<ViewGroup.LayoutParams>(
                                "onCreateLayoutParams", StatusBarIcon.Shape.WRAP_CONTENT
                            )
                        } else {
                            manager.callMethodAs<ViewGroup.LayoutParams>("onCreateLayoutParams")
                        }
                    )

                    param.result = content
                }
            }
        }

        getClass("com.android.systemui.statusbar.phone.MiuiIconManagerUtils").let {
            val list1 = it.findStaticField<ArrayList<String>>("CONTROL_CENTER_BLOCK_LIST").get()
            val list2 = it.findStaticField<ArrayList<String>>("RIGHT_BLOCK_LIST").get()
            val list3 = it.findStaticField<ArrayList<String>>("MINI_RIGHT_BLOCK_LIST").get()

            iconMap.forEach { (k, v) ->
                if (v.isBlockInControlCenter()) {
                    list1.add(k)
                }

                if (v.isBlockInRight()) {
                    list2.add(k)
                }

                if (v.isBlockInMiniRight()) {
                    list3.add(k)
                }
            }
        }

        iconMap.values.forEach { controller ->
            controller.companionRule()?.let {
                dependRule(it)
            }
        }
    }

    private inline fun queryIcon(after: () -> Unit) {
        iconMap.clear()

        mapOf(
            "systemui_status_bar_add_bluetooth_battery_enabled" to BluetoothBattery::class.java
        ).forEach { (isEnabled, viewClass) ->
            if (prefsMap.getBoolean(isEnabled)) {
                addIcon(viewClass)
            }
        }

        if (batteryStateMethod == 2 || batteryStateMethod == 3) {
            addIcon(PhoneBatteryState::class.java)
        }

        after()
    }

    private fun addIcon(viewClass: Class<out IStatusBarView>) {
        viewClass.instanceOf().apply {
            iconMap[getSlot()] = this
        }
    }
}
