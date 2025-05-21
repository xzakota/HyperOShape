package com.xzakota.oshape.startup.rule

import androidx.annotation.EmptySuper
import cn.lyric.getter.api.API
import cn.lyric.getter.api.data.LyricData
import cn.lyric.getter.api.listener.LyricListener
import cn.lyric.getter.api.listener.LyricReceiver
import cn.lyric.getter.api.tools.Tools
import com.xzakota.android.hook.XHookHelper
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStringField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiMediaControlPanel

object MusicHook : BaseHook() {
    var isPlaying = false
        private set
    var lastLyric: String? = null
        private set

    private var lastFlag: String? = null
    private var isReset = true

    private val lyricListener = mutableMapOf<MusicLyricListener, Boolean>()

    override fun onHookLoad() {
        Tools.registerLyricListener(XHookHelper.hostContext, API.API_VERSION, receiver)

        miuiMediaControlPanel.afterHookedMethod("onDestroy") { param ->
            destroyMusic()
        }

        miuiMediaControlPanel.afterHookedMethodByName("bindPlayer") { param ->
            if (lastLyric == null) {
                return@afterHookedMethodByName
            }

            val holder = param.argAs<Any>()
            val song = holder.getStringField("song")
            val artist = holder.getStringField("artist")
            val flag = "$song($artist)"

            if (holder.getObjectFieldAs("isPlaying")) {
                if (lastFlag == null && isReset) {
                    lastFlag = flag
                    isReset = false
                    updateLyricImmediately(false)
                    return@afterHookedMethodByName
                }

                if (lastFlag != flag) {
                    lastFlag = flag
                    lastLyric = null

                    switchMusic()
                } else {
                    updateLyricImmediately(true)
                }
            } else {
                if (lastFlag == flag) {
                    lastFlag = null
                    isReset = true
                }
                return@afterHookedMethodByName
            }
        }
    }

    fun addLyricListener(listener: MusicLyricListener) {
        lyricListener.put(listener, true)
    }

    fun removeLyricListener(listener: MusicLyricListener) {
        lyricListener.put(listener, false)
    }

    private val receiver = LyricReceiver(object : LyricListener() {
        override fun onUpdate(lyricData: LyricData) {
            updateLyric(lyricData)
        }

        override fun onStop(lyricData: LyricData) {
            stopLyric(lyricData)
        }
    })

    private fun updateLyric(lyricData: LyricData) {
        isPlaying = true
        lastLyric = lyricData.lyric
        lyricListener.forEach { (k, v) ->
            if (v) {
                k.onLyricUpdate(lyricData)
            }
        }
    }

    private fun updateLyricImmediately(isByController: Boolean) {
        isPlaying = true
        lyricListener.forEach { (k, v) ->
            if (v) {
                k.onLyricUpdateImmediately(isByController)
            }
        }
    }

    private fun stopLyric(lyricData: LyricData?) {
        isPlaying = false
        lyricListener.forEach { (k, v) ->
            if (v) {
                k.onLyricStop(lyricData)
            }
        }
    }

    private fun switchMusic() {
        isPlaying = false
        lyricListener.forEach { (k, _) ->
            k.onMusicSwitch()
        }
    }

    private fun destroyMusic() {
        if (isPlaying) {
            stopLyric(null)
            isPlaying = false
        }
        lastLyric = null
        lastFlag = null
        lyricListener.forEach { (k, _) ->
            k.onMusicDestroy()
        }
        lyricListener.clear()
    }

    interface MusicLyricListener {
        fun onLyricUpdate(lyricData: LyricData)

        @EmptySuper
        fun onLyricUpdateImmediately(isByController: Boolean) {}

        @EmptySuper
        fun onLyricStop(lyricData: LyricData?) {}

        @EmptySuper
        fun onMusicSwitch() {}

        fun onMusicDestroy() {
            onLyricStop(null)
        }
    }
}
