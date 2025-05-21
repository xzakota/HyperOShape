package com.xzakota.oshape.startup.rule.systemui.plugin

import android.view.View
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.code.extension.callMethod
import com.xzakota.oshape.startup.rule.systemui.CCHeaderButton
import com.xzakota.oshape.startup.rule.systemui.api.MiuiDependency
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener

class NewCCButtonEvent(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    private lateinit var qsListController: Any
    private lateinit var secondaryPanelRouter: Any

    override fun onHookLoad() {
        addHook(loadClass("miui.systemui.controlcenter.panel.main.qs.EditButtonController_Factory").afterHookedFirstConstructor {
            qsListController = it.nonNullThisObject.chainGetObjectAs("qsListControllerProvider.get()")
        })

        addHook(loadClass("miui.systemui.controlcenter.panel.main.devicecontrol.DeviceControlsEntryController_Factory").afterHookedFirstConstructor {
            secondaryPanelRouter = it.nonNullThisObject.chainGetObjectAs("secondaryPanelRouterProvider.get()")
        })

        val mainPanelControllerMode = loadClass("miui.systemui.controlcenter.panel.main.MainPanelController\$Mode")
        addHook(loadClass("miui.systemui.controlcenter.panel.main.header.MainPanelHeaderController").afterHookedFirstConstructor {
            val headerView = it.nonNullThisObject.chainGetObjectAs<View>("controlCenterHeader.controlCenterHeaderView")
            headerView.findViewById<View?>(CCHeaderButton.ID_EDIT)?.setOnClickListener {
                MiuiDependency.hapticFeedBack.clickFeedback()

                val mainPanelMode = mainPanelControllerMode.enumConstants ?: return@setOnClickListener
                qsListController.callMethod("startQuery", mainPanelMode[2])
            }

            loadClass("miui.systemui.controlcenter.panel.main.devicecontrol.DeviceControlsEntryController\$onBindViewHolder$1")
            headerView.findViewById<View?>(CCHeaderButton.ID_SMART_HOME)?.setOnClickListener {
                MiuiDependency.hapticFeedBack.clickFeedback()

                secondaryPanelRouter.callMethod("routeToSmartHome", null)
            }
        })
    }
}
