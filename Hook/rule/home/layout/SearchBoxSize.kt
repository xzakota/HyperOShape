package com.xzakota.oshape.startup.rule.home.layout

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import com.xzakota.android.extension.view.findViewByIdName
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.extension.view.setPadding
import com.xzakota.android.extension.view.setPaddingLeft
import com.xzakota.android.extension.view.setRoundRect
import com.xzakota.android.extension.view.updateLayoutParams
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.reflect.createBeforeHook
import com.xzakota.android.hook.extension.replaceMethod
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.android.util.DensityUtils.px2sp
import com.xzakota.code.extension.findFirstField
import com.xzakota.code.extension.setBooleanField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.WidgetBlur.addHomeWidgetBlur
import com.xzakota.oshape.startup.rule.home.api.Utilities
import com.xzakota.oshape.startup.rule.home.shared.Blur.isWidgetBlurFollowHookTheme
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isChildElementSize
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isLayoutOptimization
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isSearchBoxHeight
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.isSearchBoxWidth
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.searchBarDesktopLayout
import com.xzakota.oshape.startup.rule.home.shared.SearchBox.searchBarDesktopLayoutUpdateStyle
import com.xzakota.oshape.util.DexKit
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Method

object SearchBoxSize : BaseHook() {
    override fun onHookLoad() {
        searchBarDesktopLayout.afterHookedMethod("onFinishInflate") { param ->
            val layout = param.thisObjectAs<RelativeLayout>()

            if (isWidgetBlurFollowHookTheme) {
                layout.background = null
                layout.setRoundRect(dp2px(50).toFloat())
            }

            val xiaoai = layout.findViewByIdNameAs<ImageView?>("xiaoai_button")
            xiaoai?.setPadding(0)

            if (isLayoutOptimization) {
                if (searchBoxWidthDp >= 80) {
                    layout.addView(
                        TextView(layout.context).apply {
                            tag = "search_text"
                            text = getHostString("search_menu_title")
                            textSize = px2sp(childElementSize - dp2px(6))
                            layoutParams = RelativeLayout.LayoutParams(-2, -2).apply {
                                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                            }
                            setTextColor(Color.argb(0xEE, 0xFF, 0xFF, 0xFF))
                            if (searchBoxWidthDp < 150) {
                                setPaddingLeft(dp2px(24))
                            }
                        }
                    )
                }

                layout.findViewByIdName("search_bar_content_icon_layout")?.isVisible = false
                layout.findViewByIdName("search_bar_xiaoai_layout")?.updateLayoutParams<FrameLayout.LayoutParams> {
                    gravity = Gravity.START
                }

                if (isChildElementSize) {
                    xiaoai?.resetSize()
                    layout.findViewByIdNameAs<ImageView?>("search_bar_content_icon")?.resetSize()
                }
            }

            layout.doOnPreDraw {
                layout.updateLayoutParams<FrameLayout.LayoutParams> {
                    if (isSearchBoxWidth) {
                        width = dp2px(searchBoxWidthDp)
                    }

                    if (isSearchBoxHeight) {
                        height = dp2px(searchBoxHeightDp)
                    }

                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        }

        if (isLayoutOptimization) {
            xiaoaiDrawable.createBeforeHook {
                val xiaoai = getThemeIcon(
                    if (Utilities.isSupportSuperXiaoai()) {
                        "super_xiaoai"
                    } else {
                        "xiaoai"
                    }
                )

                if (xiaoai != null) {
                    it.nonNullThisObject.findFirstField()?.setBooleanField("mIsThemesShow", true)
                    it.result = xiaoai
                }
            }
        }

        if (isWidgetBlurFollowHookTheme) {
            searchBarDesktopLayout.afterHookedMethod("refreshStyle") { param ->
                val layout = param.thisObjectAs<RelativeLayout>()
                layout.addHomeWidgetBlur()
            }

            searchBarDesktopLayoutUpdateStyle.replaceMethod("doInBackground", null)
        }
    }

    private fun ImageView.resetSize() {
        updateLayoutParams<FrameLayout.LayoutParams> {
            width = childElementSize
        }
    }

    private val searchBoxWidthDp by lazy {
        prefs.getInt("home_layout_search_box_width", 150)
    }

    private val searchBoxHeightDp by lazy {
        prefs.getInt("home_layout_search_box_height", 32)
    }

    private val childElementSize by lazy {
        dp2px(prefs.getInt("home_layout_search_box_child_element_size", 24))
    }

    private val xiaoaiDrawable by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(xiaoaiDrawable)") { bridge ->
            bridge.findClass {
                matcher {
                    className("com.miui.home.launcher.SearchBarXiaoaiLayout", StringMatchType.StartsWith)
                }
            }.findMethod {
                matcher {
                    returnType(Drawable::class.java)
                }
            }.singleOrNull()
        }
    }
}
