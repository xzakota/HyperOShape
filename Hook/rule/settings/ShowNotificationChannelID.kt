package com.xzakota.oshape.startup.rule.settings

import android.app.NotificationChannel
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceCategory
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceFragmentCompat
import com.xzakota.android.util.SystemUtils
import com.xzakota.android.util.ToastUtils
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.settings.api.ValuePreference

object ShowNotificationChannelID : BaseHook() {
    private const val KEY_CHANNEL_ID = "channel_id"

    override fun onHookLoad() {
        val channelNotificationSettings = loadClass("com.android.settings.notification.ChannelNotificationSettings")

        channelNotificationSettings.afterHookedMethod("setupChannelDefaultPrefs") { param ->
            val instance = param.nonNullThisObject
            val channel = instance.getObjectFieldAs<NotificationChannel>("mChannel")
            val fragment = RePreferenceFragmentCompat(instance)
            val activity = fragment.requireActivity()

            val category = RePreferenceCategory(activity)
            fragment.preferenceScreen.addPreference(category)

            category.addPreference(
                ValuePreference(activity).apply {
                    key = KEY_CHANNEL_ID
                    title = getHostString(R.string.channel_id)
                    summary = channel.id
                    // value = channel.id
                }
            )
        }

        channelNotificationSettings.beforeHookedMethodByName("onPreferenceTreeClick") { param ->
            val fragment = RePreferenceFragmentCompat(param.nonNullThisObject)
            val preference = RePreference(param.argAs<Any>())
            val key = preference.key
            val text = preference.summary
            if (KEY_CHANNEL_ID == preference.key && text != null) {
                val context = fragment.requireContext()
                SystemUtils.addClipboardText(text, key)
                ToastUtils.showToast(context, getHostString(R.string.copied_to_clipboard))
                param.result = true
            }
        }
    }
}
