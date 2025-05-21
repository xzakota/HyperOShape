package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnAttach
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.hook.XHookHelper
import com.xzakota.android.hook.core.loader.base.IMemberHookHandler
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.concat
import com.xzakota.code.extension.createInstance
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.newArray
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.KotlinJob
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import miui.sdk.isMoreHyperOSVersion
import miui.telephony.TelephonyManager
import java.util.concurrent.atomic.AtomicBoolean

object FiveGSwitch : BaseHook() {
    private const val QS_DETAIL_CONTENT = "com.android.systemui.qs.QSDetailContent"
    private const val QS_DETAIL_CONTENT_ITEM = "$QS_DETAIL_CONTENT\$Item"

    private val isHooked = AtomicBoolean(false)

    override fun onHookLoad() {
        val fiveGServiceClient = loadClassOrNull("com.android.systemui.statusbar.policy.MiuiFiveGServiceClient")
        if (fiveGServiceClient != null) {
            var handler: IMemberHookHandler? = null
            handler = fiveGServiceClient.afterHookedMethod("update5GIcon") {
                if (TelephonyManager.getDefault().isFiveGCapable && !isHooked.getAndSet(true)) {
                    handler?.unhook()
                    putSwitch()
                    handler = null
                }
            }
        } else {
            loadClass("com.android.systemui.statusbar.pipeline.mobile.domain.interactor.MiuiMobileIconInteractorImpl")
                .afterHookedFirstConstructor { param ->
                val fiveGConnected = param.nonNullThisObject.getObjectFieldAs<Any>("fiveGConnected")
                var job: KotlinJob? = null
                job = MiuiStub.javaAdapter.alwaysCollectFlow<Boolean>(fiveGConnected) {
                    if (TelephonyManager.getDefault().isFiveGCapable && !isHooked.getAndSet(true)) {
                        job?.cancel(null)
                        putSwitch()
                        job = null
                    }
                }
            }
        }
    }

    private fun putSwitch() {
        val cellularDetailAdapter = loadClass("com.android.systemui.qs.tiles.MiuiCellularTile\$CellularDetailAdapter")
        cellularDetailAdapter.afterHookedMethod(
            "createDetailView", Context::class.java, View::class.java, ViewGroup::class.java
        ) { param ->
            if (XHookHelper.hostContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return@afterHookedMethod
            }

            val content = param.resultAs<View>()
            content.doOnAttach {
                var view = content.parentGroup
                while (view != null) {
                    val id = view.id
                    if (id == View.NO_ID) {
                        break
                    }

                    val idName = view.resources.getResourceName(id)
                    if (idName.endsWith("detail_container")) {
                        val maxHeight = view.callMethodAs<Int>("getMaxHeight")
                        view.callMethod("setMaxHeight", maxHeight + dp2px(45f))
                        break
                    }

                    view = view.parentGroup
                }
            }
        }

        cellularDetailAdapter.beforeHookedMethod("onDetailItemClick", loadClass(QS_DETAIL_CONTENT_ITEM)) { param ->
            val item = param.argAs<Any>()
            val type = item.callMethodAs<Int>("getType")

            if (type == 2333) {
                val title = item.getObjectField("title")
                // val isChecked = item.getBooleanField("isChecked")
                val fiveGTitle = getHostString(R.string.prefer_five_g)
                if (fiveGTitle == title) {
                    val manager = TelephonyManager.getDefault()
                    val userFiveGEnabled = manager.isUserFiveGEnabled
                    manager.setUserFiveGEnabled(!userFiveGEnabled)
                    if (userFiveGEnabled) {
                        logD("from 5G to none 5G")
                    } else {
                        logD("from none 5G to 5G")
                    }
                    param.result = null
                }
            }
        }

        loadClass(QS_DETAIL_CONTENT).beforeHookedMethod("setItems", loadClass("$QS_DETAIL_CONTENT_ITEM[]")) { param ->
            val content = param.thisObjectAs<ViewGroup>()

            val suffix = content.getObjectField("suffix")
            if ("Cellular" != suffix) {
                return@beforeHookedMethod
            }

            val rawItems = param.argAs<Array<*>>()
            if (rawItems.isEmpty()) {
                return@beforeHookedMethod
            }

            val finalItems = loadClass(QS_DETAIL_CONTENT_ITEM).newArray(rawItems.size + 2)

            for (i in rawItems.size - 1 downTo 0) {
                val item = rawItems[i]

                if (item != null && item::class.java.getName().endsWith("SelectableItem")) {
                    System.arraycopy(rawItems, 0, finalItems, 0, i + 1)

                    val networkText = getHostString(R.string.network_settings)
                    finalItems[i + 1] = loadClass("$QS_DETAIL_CONTENT\$TextDividerItem").createInstance(networkText)

                    val fiveGTitle = getHostString(R.string.prefer_five_g)
                    val args = arrayOf<Any?>(fiveGTitle, null, TelephonyManager.getDefault().isUserFiveGEnabled)
                    val toggleItem = loadClass("$QS_DETAIL_CONTENT\$ToggleItem")
                    finalItems[i + 2] = toggleItem.createInstance(
                        *if (isMoreHyperOSVersion(2F)) {
                            args
                        } else {
                            args.concat(arrayOf(null))
                        }
                    )

                    if (i + 3 != finalItems.size) {
                        System.arraycopy(rawItems, i + 1, finalItems, i + 3, rawItems.size - i - 1)
                    }
                    break
                }
            }

            param.args[0] = finalItems
        }
    }
}
