package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import java.util.concurrent.CancellationException
import java.util.function.Consumer

class JavaAdapter(instance: Any) : BaseReflectObject(instance) {
    fun <T> alwaysCollectFlow(
        flow: Any,
        consumer: Consumer<T>
    ): KotlinJob = KotlinJob(instance.callMethodAs("alwaysCollectFlow", flow, consumer))
}

class KotlinJob(instance: Any) : BaseReflectObject(instance) {
    fun cancel(e: CancellationException? = null) {
        instance.callMethod("cancel", e)
    }
}
