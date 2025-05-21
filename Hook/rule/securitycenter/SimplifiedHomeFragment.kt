package com.xzakota.oshape.startup.rule.securitycenter

import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.oshape.startup.base.BaseHook

object SimplifiedHomeFragment : BaseHook() {
    @Suppress("UNCHECKED_CAST")
    override fun onHookLoad() {
        loadClass("com.miui.common.card.CardViewRvAdapter").beforeHookedMethod("addAll", List::class.java) { param ->
            val oldModelList = param.argAs<List<Any>>()

            val removedModel = listOf(
                // 功能推荐
                "com.miui.common.card.models.FuncListBannerCardModel",
                // 常用功能
                // "com.miui.common.card.models.CommonlyUsedFunctionCardModel",
                // 大家都在用
                "com.miui.common.card.models.PopularActionCardModel"
            )

            param.args[0] = oldModelList.filterNot { model ->
                removedModel.contains(model.javaClass.name)
            }
        }
    }
}
