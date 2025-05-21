package com.xzakota.oshape.startup.rule.securitycenter

import android.content.pm.PackageInfo
import android.os.Bundle
import com.xzakota.android.extension.content.getString
import com.xzakota.android.extension.os.externalDataDir
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceCategory
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceCategory.Companion.PREFERENCE_CATEGORY
import com.xzakota.android.hook.startup.component.androidx.preference.RePreferenceFragmentCompat
import com.xzakota.android.util.SystemUtils
import com.xzakota.android.util.ToastUtils
import com.xzakota.code.extension.findFirstFieldByType
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.securitycenter.api.ReTextPreference
import java.lang.reflect.Modifier

object AppDetailShowMoreInfo : BaseHook() {
    private val addPreferenceKeys = arrayOf(
        "version_code", "min_sdk_version", "target_sdk_version", "uid",
        "apk_path", "internal_data_dir", "protected_internal_data_dir", "external_data_dir"
    )

    private val addPreferenceTitle = arrayOf(
        R.string.version_code, R.string.min_sdk_version, R.string.target_sdk_version, R.string.uid,
        R.string.apk_path, R.string.internal_data_dir, R.string.protected_internal_data_dir, R.string.external_data_dir
    )

    override fun onHookLoad() {
        appInformationFragment.afterHookedMethod("onCreatePreferences", Bundle::class.java, String::class.java) {
            addTextPreferences(it.nonNullThisObject)
        }

        appInformationFragment.beforeHookedMethodByName("onPreferenceTreeClick") { param ->
            val fragment = RePreferenceFragmentCompat(param.nonNullThisObject)
            val preference = ReTextPreference(param.argAs<Any>())

            val key = preference.key
            val text = preference.text
            if (key != null && text != null && addPreferenceKeys.contains(key)) {
                val context = fragment.requireContext()

                SystemUtils.addClipboardText(text, key)
                ToastUtils.showToast(context, context.getString("app_manager_copy_pkg_to_clip"))
                param.result = true
            }
        }
    }

    private fun addTextPreferences(obj: Any) {
        val packageInfo = obj.findFirstFieldByType(PackageInfo::class.java)
        val applicationInfo = packageInfo.applicationInfo ?: return

        val addPreferenceText = arrayOf(
            packageInfo.longVersionCode, applicationInfo.minSdkVersion, applicationInfo.targetSdkVersion,
            applicationInfo.uid, applicationInfo.sourceDir, applicationInfo.dataDir,
            applicationInfo.deviceProtectedDataDir, packageInfo.externalDataDir
        )

        val findField = appInformationFragment.findFirstFieldByType(PREFERENCE_CATEGORY).get(obj) ?: return
        val category = RePreferenceCategory(findField)
        addPreferenceKeys.forEachIndexed { i, key ->
            addTextPreferenceToCategory(category, key, getHostString(addPreferenceTitle[i]), addPreferenceText[i].toString())
        }
    }

    // 新版本已变更为添加不可点击首选项，故弃用
    private fun addTextPreference(obj: Any, key: String, title: String?, text: String) {
        addTextPreferenceMethod.invoke(obj, key, title, text)
    }

    private fun addTextPreferenceToCategory(category: RePreferenceCategory, key: String, title: String?, text: String) {
        category.addPreference(
            ReTextPreference(category.context).also {
                it.key = key
                it.title = title
                it.text = text
            }
        )
    }

    private val appInformationFragment by lazy {
        loadClass("com.miui.appmanager.fragment.AMAppInformationFragment")
    }

    private val addTextPreferenceMethod by lazy {
        appInformationFragment.methodFinder()
            .filterByModifiers(Modifier.PRIVATE)
            .filterByParamTypes(String::class.java, String::class.java, String::class.java)
            .single()
    }
}
