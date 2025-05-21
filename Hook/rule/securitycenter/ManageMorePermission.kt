package com.xzakota.oshape.startup.rule.securitycenter

import android.Manifest
import android.app.AppOpsManager
import android.content.pm.PackageInfo
import android.extension.app.AppOpsManagerEx
import android.extension.app.getOpModeForPackage
import android.extension.content.IntentEx
import android.extension.os.UserHandleEx
import android.provider.Settings
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.removeAdditionalInstanceField
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceCategory
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceFragmentCompat
import com.xzakota.android.util.SystemUtils
import com.xzakota.code.extension.findFirstFieldByType
import com.xzakota.code.extension.findFirstFieldByTypeAs
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.securitycenter.api.AppPermissionsEditorPreference
import com.xzakota.oshape.util.DexKit
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean

object ManageMorePermission : BaseHook() {
    private const val KEY_SPECIAL_PERMISSIONS_PREFIX = "special_permissions@"
    private const val CODE_SPECIAL_PERMISSIONS_REQUEST = 0x1000

    private var packageInfo: PackageInfo? = null
    private var packageName: String? = null
    private var permissionPreferences: Map<String, Pair<AppPermissionsEditorPreference, String>>? = null
    private var clickPreferenceKey: String? = null

    private var isHookedOnPreferenceClick = AtomicBoolean(false)

    override fun onHookLoad() {
        preferenceClickListener.javaClass
        permissionLifecycleOnChanged.createAfterHook { param ->
            val instance = param.nonNullThisObject
            val fragment = RePreferenceFragmentCompat(instance)

            val preferenceScreen = fragment.preferenceScreen
            if (preferenceScreen.getPreferenceCount() == 0) {
                return@createAfterHook
            }

            packageInfo = instance.findFirstFieldByType(PackageInfo::class.java)
            val permissions = getSpecialPermissions()
            if (permissions.isNullOrEmpty()) {
                return@createAfterHook
            }

            val activity = fragment.requireActivity()

            val activityIntent = activity.intent
            val packageName = activityIntent.getStringExtra("extra_pkgname")?.also {
                packageName = it
            } ?: return@createAfterHook
            val uid = activityIntent.getIntExtra("userId", UserHandleEx.myUserId())
            instance.setAdditionalInstanceField(name, true)

            val category = RePreferenceCategory(activity).apply {
                title = getHostString(R.string.special_permissions)
            }
            preferenceScreen.addPreference(category)

            val listener = preferenceClickListener.get(instance)
            if (listener != null) {
                hookPreferenceClickListener(listener)
            }

            val permissionPreferences = mutableMapOf<String, Pair<AppPermissionsEditorPreference, String>>().also {
                permissionPreferences = it
            }
            permissions.forEach {
                val preferenceKey = KEY_SPECIAL_PERMISSIONS_PREFIX + it.value.second
                val preference = AppPermissionsEditorPreference(activity).apply {
                    key = preferenceKey
                    title = getHostString(it.key)
                    intent = IntentEx.newPackageIntent(it.value.second, packageName, uid)
                    isIconVisible = true

                    setOnPreferenceClickListener(listener)
                }
                resetPreferenceAction(preference, it.value.third)

                category.addPreference(preference)
                permissionPreferences[preferenceKey] = Pair(preference, it.value.third)
            }
        }

        newPermissionsEditorFragment.beforeHookedMethodByName("onActivityResult", true) { param ->
            val fragment = param.nonNullThisObject
            val requestCode = param.args[0]

            val tag = fragment.getAdditionalInstanceFieldAs<Boolean?>(name)
            if (tag == true && requestCode == CODE_SPECIAL_PERMISSIONS_REQUEST) {
                permissionPreferences?.get(clickPreferenceKey)?.run {
                    resetPreferenceAction(first, second)
                }
            }
        }

        newPermissionsEditorFragment.afterHookedMethodByName("onResume", true) { param ->
            val fragment = param.nonNullThisObject
            if (fragment.getAdditionalInstanceFieldAs<Boolean?>(name) == true) {
                permissionPreferences?.forEach {
                    resetPreferenceAction(it.value.first, it.value.second)
                }
            }
        }

        newPermissionsEditorFragment.afterHookedMethodByName("onDestroy", true) { param ->
            val fragment = param.nonNullThisObject
            if (fragment.getAdditionalInstanceFieldAs<Boolean?>(name) == true) {
                fragment.removeAdditionalInstanceField(name)

                packageInfo = null
                packageName = null
                clickPreferenceKey = null
                permissionPreferences = null
            }
        }
    }

    private fun hookPreferenceClickListener(listener: Any) {
        if (isHookedOnPreferenceClick.getAndSet(true)) {
            return
        }

        listener.javaClass.beforeHookedMethodByName("onPreferenceClick") { param ->
            val preference = RePreference(param.argAs<Any>())

            if (preference.key?.startsWith(KEY_SPECIAL_PERMISSIONS_PREFIX) == true) {
                val intent = preference.intent
                if (intent != null) {
                    clickPreferenceKey = preference.key

                    RePreferenceFragmentCompat(
                        param.nonNullThisObject.findFirstFieldByTypeAs(newPermissionsEditorFragment)
                    ).startActivityForResult(intent, CODE_SPECIAL_PERMISSIONS_REQUEST)
                }

                param.result = true
            }
        }
    }

    private fun resetPreferenceAction(preference: AppPermissionsEditorPreference, op: String) {
        val applicationInfo = packageInfo?.applicationInfo ?: return
        val packageName = packageName ?: return
        val mode = SystemUtils.appOpsManager.getOpModeForPackage(applicationInfo.uid, packageName, op)
        preference.action = when (mode) {
            AppOpsManager.MODE_ALLOWED -> AppPermissionsEditorPreference.ACTION_ACCEPT
            else -> AppPermissionsEditorPreference.ACTION_REJECT
        }
    }

    private fun getSpecialPermissions() = packageInfo?.requestedPermissions?.run {
        mapOf(
            R.string.manage_external_storage to Triple(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                AppOpsManagerEx.OPSTR_MANAGE_EXTERNAL_STORAGE
            ),
            R.string.write_system_settings to Triple(
                Manifest.permission.WRITE_SETTINGS,
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                AppOpsManagerEx.OPSTR_WRITE_SETTINGS
            ),
            R.string.install_other_apps to Triple(
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                AppOpsManagerEx.OPSTR_REQUEST_INSTALL_PACKAGES
            ),
            R.string.draw_overlay to Triple(
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                AppOpsManagerEx.OPSTR_SYSTEM_ALERT_WINDOW
            )
        ).filter {
            contains(it.value.first)
        }
    }

    private val newPermissionsEditorFragment by lazy {
        loadClass("com.miui.permcenter.permissions.NewPermissionsEditorFragment")
    }

    private val permissionLifecycleOnChanged by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(permissionLifecycleOnChanged)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(newPermissionsEditorFragment)
                    usingStrings("com.miui.securitycenter", "miui.intent.action.PRIVACY_INPUT_MODE_ACTIVITY")
                }
            }.singleOrNull()
        }
    }

    private val preferenceClickListener by lazy {
        DexKit.findMemberOrLog<Field>(this, "$name(preferenceClickListener)") { bridge ->
            bridge.findField {
                matcher {
                    declaredClass(newPermissionsEditorFragment)
                    type("androidx.preference.Preference$", StringMatchType.StartsWith)
                }
            }.singleOrNull()
        }
    }
}
