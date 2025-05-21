package com.xzakota.oshape.startup.rule.systemui

import android.view.ViewGroup
import android.widget.TextView
import cn.lyric.getter.api.data.LyricData
import com.xzakota.android.extension.view.setEllipsize
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.reflect.createBeforeHook
import com.xzakota.android.hook.startup.base.BaseHook
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.rule.MusicHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiNotificationHeaderView
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.setNotificationPanelVisible
import miui.sdk.isVerticalMode

class NCHeaderLyric : BaseHook(), MusicHook.MusicLyricListener {
    private var attachTextView: TextView? = null
    private var clockListeners: ArrayList<Any?>? = null
    private var lyric: String? = null
    private var isPlaying = false

    override fun onHookLoad() {
        miuiNotificationHeaderView.afterHookedMethod("onFinishInflate") { param ->
            val viewGroup = param.nonNullThisObject

            viewGroup.getObjectFieldAs<TextView>("mDateView").also {
                clockListeners = it.chainGetObjectAs("mMiuiStatusBarClockController.mClockListeners")
                it.setEllipsize(1)
            }
            viewGroup.getObjectFieldAs<TextView>("mLandClock").setEllipsize(1)
        }

        miuiNotificationHeaderView.beforeHookedMethod("updateLayout") { param ->
            val viewGroup = param.thisObjectAs<ViewGroup>()
            val context = viewGroup.context
            val configuration = context.resources.configuration
            val orientation = viewGroup.getObjectFieldAs<Int>("mOrientation")
            val screenLayout = viewGroup.getObjectFieldAs<Int>("mScreenLayout")

            if (orientation == configuration.orientation && screenLayout == configuration.screenLayout) {
                return@beforeHookedMethod
            }

            val dateView = viewGroup.getObjectFieldAs<TextView>("mDateView")
            val landClock = viewGroup.getObjectFieldAs<TextView>("mLandClock")

            val clockListeners = clockListeners
            val oldTextView = if (isVerticalMode(context)) {
                attachTextView = dateView
                landClock
            } else {
                attachTextView = landClock
                dateView
            }

            oldTextView.callMethod("updateTime")
            if (clockListeners != null && !clockListeners.contains(oldTextView)) {
                clockListeners.add(oldTextView)
            }
        }

        setNotificationPanelVisible.createBeforeHook { param ->
            val controller = param.nonNullThisObject
            val isKeyguard = controller.chainGetObjectAs<Boolean>("keyguardStateController.mShowing")
            if (isKeyguard) {
                return@createBeforeHook
            }

            val isVisible = controller.getBooleanField("visible")
            val setVisible = param.argAs<Boolean>()
            if (isVisible == setVisible) {
                return@createBeforeHook
            }

            if (setVisible) {
                clockListeners?.remove(attachTextView)

                onLyricUpdateImmediately(false)
                MusicHook.addLyricListener(this)
            } else {
                clockListeners?.add(attachTextView)
                lyric = null
                MusicHook.removeLyricListener(this)
            }
        }
    }

    override fun onLyricUpdate(lyricData: LyricData) {
        lyric = lyricData.lyric
        updateLyric()
        val clockListeners = clockListeners
        if (clockListeners != null && clockListeners.contains(attachTextView)) {
            clockListeners.remove(attachTextView)
        }
    }

    override fun onLyricUpdateImmediately(isByController: Boolean) {
        if (isByController) {
            return
        }

        isPlaying = if (!MusicHook.isPlaying && isPlaying) {
            onLyricStop(null)
            false
        } else {
            if (MusicHook.lastLyric.also { lyric = it } != null) {
                updateLyric()
            }
            true
        }
    }

    override fun onLyricStop(lyricData: LyricData?) {
        attachTextView?.callMethod("updateTime")
        val clockListeners = clockListeners
        if (clockListeners != null && !clockListeners.contains(attachTextView)) {
            clockListeners.add(attachTextView)
        }
    }

    override fun onMusicSwitch() {
        onLyricStop(null)
    }

    override fun onMusicDestroy() {
        lyric = null
    }

    private fun updateLyric() {
        attachTextView?.text = lyric
    }
}
