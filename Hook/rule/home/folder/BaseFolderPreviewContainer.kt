package com.xzakota.oshape.startup.rule.home.folder

import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.createInstance
import com.xzakota.code.extension.getFloatField
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.setIntField
import com.xzakota.oshape.startup.rule.home.api.DeviceConfig
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIconPreviewInfo

abstract class BaseFolderPreviewContainer(instance: Any) : BaseReflectObject(instance) {
    override val instance = instance as View

    protected val lItemEdgePercent = instance.getFloatField("m2x2LargeItemMergeEdgePercent")
    protected val lItemInnerPercent = instance.getFloatField("m2x2LargeItemMergeInnerPercent")
    protected val sItemInnerPercent = instance.getFloatField("m2x2SmallItemMergeInnerPercent")

    protected var lItemEdgeHor: Int
        get() = instance.getIntField("mLarge2x2ItemMergeEdgeHor")
        set(value) = instance.setIntField("mLarge2x2ItemMergeEdgeHor", value)

    protected var lItemEdgeVer: Int
        get() = instance.getIntField("mLarge2x2ItemMergeEdgeVer")
        set(value) = instance.setIntField("mLarge2x2ItemMergeEdgeVer", value)

    protected var lItemInnerHor: Int
        get() = instance.getIntField("mLarge2x2ItemMergeInnerHor")
        set(value) = instance.setIntField("mLarge2x2ItemMergeInnerHor", value)

    protected var lItemInnerVer: Int
        get() = instance.getIntField("mLarge2x2ItemMergeInnerVer")
        set(value) = instance.setIntField("mLarge2x2ItemMergeInnerVer", value)

    protected var lItemHeight: Int
        get() = instance.getIntField("mLargeItemHeight")
        set(value) = instance.setIntField("mLargeItemHeight", value)

    protected var lItemWith: Int
        get() = instance.getIntField("mLargeItemWith")
        set(value) = instance.setIntField("mLargeItemWith", value)

    protected var sItemInner: Int
        get() = instance.getIntField("mSmall2x2ItemMergeInner")
        set(value) = instance.setIntField("mSmall2x2ItemMergeInner", value)

    protected var sItemHeight: Int
        get() = instance.getIntField("mSmallItemHeight")
        set(value) = instance.setIntField("mSmallItemHeight", value)

    protected var sItemWith: Int
        get() = instance.getIntField("mSmallItemWith")
        set(value) = instance.setIntField("mSmallItemWith", value)

    protected abstract val layout: FolderLayout

    fun preMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec) - instance.paddingStart - instance.paddingEnd
        val height = View.MeasureSpec.getSize(heightMeasureSpec) - instance.paddingTop - instance.paddingBottom

        onPreMeasure(width, height)
    }

    protected abstract fun onPreMeasure(width: Int, height: Int)

    open fun onPreSetup() {
        val start: Int
        val edge: Int
        if (DeviceConfig.isLayoutRtl()) {
            start = instance.paddingStart + lItemEdgeHor + lItemInnerHor * (layout.x - 1)
            edge = lItemWith * (layout.x - 1)
        } else {
            start = instance.paddingStart
            edge = lItemEdgeHor
        }

        val x0 = start + edge
        val y0 = instance.paddingTop + lItemEdgeVer

        var x = x0
        var y = y0
        var t = 0

        val lIconNum = getMLargeIconNum()
        for (i in 0 until getMItemsMaxCount()) {
            if (i < lIconNum) {
                val (x1, y1) = largeViewPreSetup(x, y, x0, y0, lItemWith, lItemHeight, i)
                t = x1
                y = y1
                if (DeviceConfig.isLayoutRtl() && i == layout.l - 1) {
                    t += sItemWith + sItemInner
                }
                x = t
            } else {
                val (x1, y1) = smallViewPreSetup(x, y, t, sItemWith, sItemHeight, i)
                x = x1
                y = y1
            }
        }
    }

    open fun getSmallItemsRectF(): RectF {
        val edge = lItemEdgeHor
        val start = if (DeviceConfig.isLayoutRtl()) {
            instance.paddingStart
        } else {
            instance.paddingStart + (lItemWith + edge) * (layout.x - 1)
        }
        val x0 = (start + edge).toFloat()
        val y0 = instance.paddingTop + (lItemEdgeVer + lItemHeight) * (layout.y - 1) + lItemInnerVer * 1F
        val itemInner = sItemInner
        return RectF(x0, y0, (sItemWith * 2) + x0 + itemInner, (sItemHeight * 2) + y0 + itemInner)
    }

    protected open fun largeViewPreSetup(x: Int, y: Int, x0: Int, y0: Int, w: Int, h: Int, i: Int): Point {
        getMPvItemLocationInfoList().add(
            folderIconPreviewInfo.createInstance(0, 0, w, h, Rect(0, 0, w, h), Rect(x, y, x + w, y + h))
        )
        val offset = lItemInnerHor + w
        val x1 = if (i % layout.x != layout.x - 1) {
            x + if (DeviceConfig.isLayoutRtl()) {
                -offset
            } else {
                offset
            }
        } else {
            x0
        }
        val y1 = y0 + (i + 1) / layout.x * (lItemInnerVer + h)
        return Point(x1, y1)
    }

    protected open fun smallViewPreSetup(x: Int, y: Int, x0: Int, w: Int, h: Int, i: Int): Point {
        getMPvItemLocationInfoList().add(
            folderIconPreviewInfo.createInstance(0, 0, w, h, Rect(0, 0, w, h), Rect(x, y, x + w, y + h))
        )
        val offset = sItemInner + w
        val x1 = if ((i - getMLargeIconNum()) % 2 == 0) {
            x + if (DeviceConfig.isLayoutRtl()) {
                -offset
            } else {
                offset
            }
        } else {
            x0
        }
        val y1 = if (i - layout.l == 1) {
            sItemInner + h + y
        } else {
            y
        }
        return Point(x1, y1)
    }

    private fun getMItemsMaxCount(): Int = instance.callMethodAs("getMItemsMaxCount")

    private fun getMLargeIconNum(): Int = instance.callMethodAs("getMLargeIconNum")

    private fun getMPvItemLocationInfoList(): ArrayList<Any> = instance.callMethodAs("getMPvItemLocationInfoList")

    class FolderLayout(val x: Int, val y: Int, val l: Int, val s: Int)
}
