package com.xzakota.oshape.startup.rule.systemui

import android.widget.TextView
import cn.lyric.getter.api.data.LyricData
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.MusicHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiMediaControlPanel

class MediaPanelLyric : BaseHook(), MusicHook.MusicLyricListener {
    private var attachTextView: TextView? = null
    private var lyric: String? = null

    override fun onHookLoad() {
        miuiMediaControlPanel.afterHookedMethodByName("attachPlayer", true) { param ->
            attachTextView = param.argAs<Any>().getObjectFieldAs("artistText")

            MusicHook.addLyricListener(this)
        }
    }

    override fun onLyricUpdate(lyricData: LyricData) {
        lyric = lyricData.lyric
        updateLyric()
    }

    override fun onLyricUpdateImmediately(isByController: Boolean) {
        lyric = MusicHook.lastLyric
        updateLyric()
    }

    override fun onMusicDestroy() {
        MusicHook.removeLyricListener(this)

        lyric = null
        attachTextView = null
    }

    private fun updateLyric() {
        attachTextView?.text = lyric
    }
}
