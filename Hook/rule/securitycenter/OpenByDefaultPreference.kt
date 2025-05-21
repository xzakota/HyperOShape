package com.xzakota.oshape.startup.rule.securitycenter

import android.app.Activity
import android.content.Intent
import android.extension.content.IntentEx
import android.extension.os.UserHandleEx
import android.provider.Settings
import androidx.core.net.toUri
import com.xzakota.android.extension.content.startActivityWithAction
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.removeAdditionalInstanceField
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceFragmentCompat
import com.xzakota.android.util.SystemUtils
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.securitycenter.shared.ApplicationsDetails.applicationsDetailsFragment
import com.xzakota.oshape.util.DexKit
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.ref.WeakReference
import java.lang.reflect.Method

object OpenByDefaultPreference : BaseHook() {
    private var uid: Int? = null
    private var packageName: String? = null
    private var appDefaultPref: RePreference? = null
    private var activityRef: WeakReference<Activity>? = null

    override fun onHookLoad() {
        applicationsDetailsFragment.afterHookedMethodByName("initView") { param ->
            val instance = param.nonNullThisObject
            val fragment = RePreferenceFragmentCompat(instance)

            val activity = WeakReference(fragment.requireActivity()).also {
                activityRef = it
            }
            val intent = activity.get()?.intent
            packageName = intent?.getStringExtra("package_name") ?: return@afterHookedMethodByName
            uid = intent.getIntExtra("miui.intent.extra.USER_ID", UserHandleEx.myUserId())
            appDefaultPref = fragment.findPreference("app_default_pref")?.also {
                it.setTitle(R.string.open_by_default)
                it.summary = null
                it.setOnPreferenceClickListener(null)

                instance.setAdditionalInstanceField(name, true)
            }
        }

        loadAppDetailsFinish.createAfterHook { param ->
            val packageName = packageName
            val appDefaultPref = appDefaultPref
            val activity = activityRef?.get()

            if (packageName != null && appDefaultPref != null && appDefaultPref.isVisible && activity != null) {
                val isLinkHandlingAllowed = SystemUtils.domainVerificationManager
                    .getDomainVerificationUserState(packageName)
                    ?.isLinkHandlingAllowed == true

                appDefaultPref.setSummary(
                    if (isLinkHandlingAllowed) {
                        R.string.allow
                    } else {
                        R.string.deny
                    }
                )

                appDefaultPref.setOnPreferenceClickListener(param.nonNullThisObject)
            }
        }

        applicationsDetailsFragment.beforeHookedMethodByName("onPreferenceClick") { param ->
            val uid = uid
            val packageName = packageName
            val appDefaultPref = appDefaultPref
            val activity = activityRef?.get()

            if (uid != null && packageName != null && appDefaultPref != null && activity != null &&
                param.args[0] == appDefaultPref.instance && packageName.isNotEmpty()
            ) {
                activity.startActivityWithAction(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS) {
                    it.data = "package:${packageName}".toUri()
                    it.putExtra(IntentEx.EXTRA_USER_HANDLE, UserHandleEx.newInstance(uid))
                    it.addCategory(Intent.CATEGORY_DEFAULT)
                    it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }

                param.result = true
            }
        }

        applicationsDetailsFragment.afterHookedMethodByName("onDestroy", true) { param ->
            val fragment = param.nonNullThisObject
            if (fragment.getAdditionalInstanceFieldAs<Boolean?>(name) == true) {
                fragment.removeAdditionalInstanceField(name)

                uid = null
                packageName = null
                appDefaultPref = null
                activityRef = null
            }
        }
    }

    private val loadAppDetailsFinish by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(loadAppDetailsFinished)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(applicationsDetailsFragment)
                    addUsingString("enter_way", StringMatchType.Equals)
                }
            }.singleOrNull()
        }
    }
}
