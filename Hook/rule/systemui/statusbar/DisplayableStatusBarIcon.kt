package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.telephony.SubscriptionManager
import android.view.ViewGroup
import androidx.core.view.forEach
import com.xzakota.android.dexkit.DexKit
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.getAdditionalInstanceField
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.oshape.startup.base.BaseRule
import com.xzakota.oshape.startup.rule.shared.Kotlin
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.api.StateFlowHelper
import com.xzakota.oshape.startup.rule.systemui.api.StatusBarIconController
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.connectivitySlot
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.miuiCellularIconVM
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.miuiMobileIconInteractorImpl
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.mobileConnectionsRepositoryImpl
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.modernStatusBarMobileView
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.modernStatusBarWifiView
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.statusBarIconControllerImpl
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.statusIconDisplayable
import com.xzakota.reflect.extension.callMethod
import com.xzakota.reflect.extension.callMethodAs
import com.xzakota.reflect.extension.findField
import com.xzakota.reflect.extension.findStaticField
import com.xzakota.reflect.extension.instanceOf
import org.luckypray.dexkit.query.enums.StringMatchType
import java.util.function.Consumer

object DisplayableStatusBarIcon : BaseRule() {
    private var defaultDataSlotId = 1

    override fun onRuleLoad() {
        statusBarIconControllerImpl.afterHookedMethodByName("refreshIconGroup") { param ->
            val controller = StatusBarIconController(param.nnThis)

            param.args<Any>().findField<ViewGroup>("mGroup").get().forEach {
                if (!statusIconDisplayable.isInstance(it)) {
                    return@forEach
                }

                if (it.callMethodAs<String>("getSlot") == "mobile") {
                    val subId = it.callMethodAs<Int>("getSubId")
                    val slotId = SubscriptionManager.getSlotIndex(subId) + 1
                    if (slotId == 0) {
                        return@forEach
                    }

                    val observer = controller.statusBarIconObserver
                    if (observer.isIconBlocked("mobile_data")) {
                        it.callMethod("setBlocked", observer.isIconBlocked("mobile_$slotId"))
                    } else if (defaultDataSlotId != 0) {
                        it.callMethod("setBlocked", slotId != defaultDataSlotId)
                    }
                }
            }
        }

        // refreshIconGroupConsumer.javaClass
        mobileConnectionsRepositoryImpl.afterHookedFirstConstructor { param ->
            val defaultDataSubId = param.nnThis.findField<Any>("defaultDataSubId").get()
            MiuiStub.myProvider.javaAdapter.collectFlow<Int?>(defaultDataSubId) {
                if (it == null) {
                    return@collectFlow
                }

                defaultDataSlotId = miui.telephony.SubscriptionManager.getDefault().callMethodAs<Int>("getSlotId", it) + 1
                if (defaultDataSlotId != 1 && defaultDataSlotId != 2) {
                    defaultDataSlotId = miui.telephony.SubscriptionManager.getDefault().defaultDataSlotId + 1
                }
            }
        }

        modernStatusBarMobileView.afterHookedMethodByName("constructAndBind") { param ->
            param.result?.setAdditionalInstanceField("viewModel", param.args.last())
        }

        modernStatusBarMobileView.beforeHookedMethod("setBlocked", Boolean::class.java) { param ->
            if (defaultDataSlotId == 0) {
                return@beforeHookedMethod
            }

            val vm = param.nnThis.getAdditionalInstanceField("viewModel") ?: return@beforeHookedMethod
            val isVisible = vm.callMethodAs<Any>("isVisible")
            val flowValue = StateFlowHelper.getStateFlowValue(isVisible) ?: return@beforeHookedMethod
            if (Kotlin.pair.isInstance(flowValue)) {
                val second = flowValue.callMethodAs<Boolean>("getSecond")
                StateFlowHelper.setStateFlowValue(isVisible, Kotlin.pair.instanceOf(!param.args<Boolean>(), second))
            } else if (flowValue is Boolean) {
                StateFlowHelper.setStateFlowValue(isVisible, !param.args<Boolean>())
            }
        }

        miuiMobileIconInteractorImpl.afterHookedFirstConstructor { param ->
            // index of subId
            val index = param.args.indexOfFirst {
                it is Int
            }

            param.nnThis.setAdditionalInstanceField("defaultDataSubId", param.args[index + 1])
        }

        miuiCellularIconVM.afterHookedFirstConstructor { param ->
            val isVisible = param.nnThis.findField<Any>("isVisible").get()
            val viewModelCommon = param.args<Any>()
            val subId = viewModelCommon.callMethodAs<Int>("getSubscriptionId")
            var slotId = SubscriptionManager.getSlotIndex(subId) + 1
            val iconObserver = MiuiStub.myProvider.statusBarIconController.statusBarIconObserver
            val interactor = param.args<Any>(2)

            // val serviceState = interactor.findField<Any>("serviceState").get()
            val defaultDataSubId = interactor.getAdditionalInstanceField("defaultDataSubId")
            if (defaultDataSubId != null) {
                MiuiStub.myProvider.javaAdapter.collectFlow<Any>(defaultDataSubId) {
                    if (slotId == 0) {
                        slotId = miui.telephony.SubscriptionManager.getDefault().callMethodAs<Int>("getSlotId", subId) + 1
                    }

                    if (slotId != 1 && slotId != 2) {
                        slotId = 1
                    }

                    if (!iconObserver.isIconBlocked("mobile_data")) {
                        val flowValue = StateFlowHelper.getStateFlowValue(isVisible) ?: return@collectFlow
                        var dataSlotId = it.findField<Int>("defaultDataSlotId").get() + 1
                        if (dataSlotId != 1 && dataSlotId != 2) {
                            dataSlotId = miui.telephony.SubscriptionManager.getDefault().defaultDataSlotId + 1
                        }

                        if (Kotlin.pair.isInstance(flowValue)) {
                            val second = flowValue.callMethodAs<Boolean>("getSecond")
                            StateFlowHelper.setStateFlowValue(isVisible, Kotlin.pair.instanceOf(dataSlotId == slotId, second))
                        } else if (flowValue is Boolean) {
                            StateFlowHelper.setStateFlowValue(isVisible, dataSlotId == slotId)
                        }
                    }
                }
            }

            MiuiStub.myProvider.javaAdapter.collectFlow<Any>(isVisible) {
                if (iconObserver.isIconBlocked("mobile_data")) {
                    if (!iconObserver.isIconBlocked("mobile_$slotId")) {
                        return@collectFlow
                    }
                } else {
                    if (defaultDataSlotId == 0) {
                        defaultDataSlotId = miui.telephony.SubscriptionManager.getDefault().defaultDataSlotId + 1
                    }

                    if (slotId == defaultDataSlotId) {
                        return@collectFlow
                    }
                }

                if (Kotlin.pair.isInstance(it)) { // isMoreHyperOS3 -> Pair
                    if (it.callMethodAs<Boolean>("getFirst")) {
                        val second = it.callMethodAs<Boolean>("getSecond")
                        StateFlowHelper.setStateFlowValue(isVisible, Kotlin.pair.instanceOf(false, second))
                    }
                } else if (it is Boolean) {
                    if (it) {
                        StateFlowHelper.setStateFlowValue(isVisible, false)
                    }
                }
            }
        }

        modernStatusBarWifiView.beforeHookedMethod("setBlocked", Boolean::class.java) { param ->
            val flow = MiuiStub.myProvider.connectivityRepository.findField<Any>("forceHiddenSlots").get()
            val wifiSlot = connectivitySlot.findStaticField<Any>("WIFI").get()

            val hiddenSet = (StateFlowHelper.getStateFlowValue(flow) as Set<*>).toMutableSet()
            if (param.args()) {
                hiddenSet += wifiSlot
            } else {
                hiddenSet -= wifiSlot
            }
            StateFlowHelper.setStateFlowValue(flow, hiddenSet)
        }
    }

    private val refreshIconGroupConsumer by lazy {
        DexKit.findMemberOrLog<Class<*>>(this, "$baseTag(refreshIconGroupConsumer)") { bridge ->
            bridge.findClass {
                matcher {
                    className("StatusBarIconControllerImpl$", StringMatchType.Contains)

                    interfaces {
                        add(Consumer::class.java.name)
                    }

                    addMethod {
                        addInvoke {
                            name("refreshIconGroup")
                        }
                    }
                }
            }.single()
        }
    }
}
