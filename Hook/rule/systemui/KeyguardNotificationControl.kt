package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.expandNotificationRow
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiNotificationPanelViewController
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.notificationPanelViewController

object KeyguardNotificationControl : BaseHook() {
    private var panelViewController: Any? = null
    private var notificationLayout: Any? = null

    private var lastDownwardValue = 0
    private var defaultTopPadding = 0

    override fun onHookLoad() {
        miuiNotificationPanelViewController.afterHookedFirstConstructor { param ->
            val panelViewController = param.nonNullThisObject.also {
                panelViewController = it
            }

            notificationLayout = panelViewController.chainGetObjectAs<Any>(
                "mNotificationStackScrollLayoutController.mView"
            )
        }

        if (method == 1) {
            // 还原通知组件可用空间
            notificationPanelViewController.afterHookedMethod("getVerticalSpaceForLockscreenNotifications") { param ->
                val space = param.resultAs<Float>()
                val isOnKeyguard = param.nonNullThisObject.callMethodAs<Boolean>("isOnKeyguard")

                // 只考虑在锁屏下的情况
                if (lastDownwardValue >= 0 && isOnKeyguard) {
                    param.result = space + lastDownwardValue
                }
            }

            // 开启智能显示锁屏通知且人脸解锁成功后更新通知位置
            expandNotificationRow.afterHookedMethod("notifyHeightChanged", Boolean::class.java) { param ->
                val row = param.nonNullThisObject

                val isChanged = param.argAs<Boolean>()
                val isOnKeyguard = row.getBooleanField("mOnKeyguard")
                val isShowingPublic = row.getBooleanField("mShowingPublic")

                // 只考虑在锁屏下可见通知的情况
                if (isChanged && isOnKeyguard && isShowingPublic) {
                    val isUserLocked = row.getBooleanField("mUserLocked")
                    if (!isUserLocked) {
                        panelViewController?.callMethod("positionClockAndNotifications", false)
                    }
                }
            }

            // 计算最多显示的通知数量
            // computeMaxKeyguardNotifications
        }

        loadClass("com.android.keyguard.clock.KeyguardClockContainer").afterHookedMethod("getClockBottom") { param ->
            val clockBottom = param.resultAs<Int>()
            // 静态下沉
            if (method == 0) {
                param.result = clockBottom + value
                return@afterHookedMethod
            }

            val notificationLayout = notificationLayout
            val panelViewController = panelViewController
            if (notificationLayout == null || panelViewController == null) {
                return@afterHookedMethod
            }

            val shelfHeight = notificationLayout.getObjectFieldAs<Any>("mShelf").callMethodAs<Int>("getIntrinsicHeight")
            val contentHeight = notificationLayout.callMethodAs<Int>("getIntrinsicContentHeight")
            // val layoutHeight = notificationLayout.callMethodAs<Int>("getLayoutHeight")
            // val childCount = notificationLayout.callMethodAs<Int>("getNotGoneChildCount")
            // val emptyBottomMargin = notificationLayout.callMethodAs<Int>("getEmptyBottomMargin")
            // val stackHeight = notificationLayout.callMethodAs<Int>("getIntrinsicStackHeight")
            // var maxContentHeight = ?

            // 只获取第一次顶部边距值
            if (defaultTopPadding == 0) {
                panelViewController.getObjectFieldAs<Float>("mKeyguardNotificationTopPadding").let {
                    defaultTopPadding = it.toInt()
                }
            }

            // 跳过初始化阶段
            if (shelfHeight == 0 || contentHeight == shelfHeight) {
                return@afterHookedMethod
            }

            // 预设动态下沉
            if (method == 1) {
                lastDownwardValue = maxValue - defaultTopPadding - contentHeight
                if (lastDownwardValue >= 0) {
                    param.result = clockBottom + lastDownwardValue
                }
            }
        }
    }

    // 下沉方式
    private val method by lazy {
        prefs.getStringAsInt("systemui_lockscreen_move_notification_down_method")
    }

    // 下沉高度
    private val value by lazy {
        dp2px(prefs.getInt("systemui_lockscreen_move_notification_down_value", 0))
    }

    // 最大下沉值
    private val maxValue by lazy {
        prefs.getInt("systemui_lockscreen_move_notification_down_max_value", 0)
    }
}
