package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.findFirstFieldByType
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object StateFlowHelper {
    private val stateFlow by lazy {
        loadClass("kotlinx.coroutines.flow.StateFlow")
    }

    private val stateFlowKt by lazy {
        loadClass("kotlinx.coroutines.flow.StateFlowKt")
    }

    private val readonlyStateFlow by lazy {
        loadClass("kotlinx.coroutines.flow.ReadonlyStateFlow")
    }

    private val readonlyStateFlowConstructor by lazy {
        readonlyStateFlow.getConstructor(stateFlow)
    }

    @JvmStatic
    fun newStateFlow(initValue: Any?): Any = stateFlowKt.callStaticMethodAs("MutableStateFlow", initValue)

    @JvmStatic
    fun newReadonlyStateFlow(initValue: Any?): Any = readonlyStateFlowConstructor.newInstance(newStateFlow(initValue))

    @JvmStatic
    fun setStateFlowValue(flow: Any?, value: Any?) {
        flow ?: return

        when (stateFlow::class.java.simpleName) {
            "ReadonlyStateFlow" -> stateFlow.findFirstFieldByType(stateFlow)
            "StateFlowImpl" -> stateFlow
            else -> null
        }?.callMethod("setValue", value)
    }

    @JvmStatic
    fun getStateFlowValue(flow: Any?): Any? = flow?.callMethod("getValue")
}
