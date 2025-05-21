package com.xzakota.oshape.startup.rule.home.folder

class FolderPreviewContainer3X1(instance: Any) : BaseFolderPreviewContainer(instance) {
    override val layout = FolderLayout(3, 1, 2, 4)

    override fun onPreMeasure(width: Int, height: Int) {
        lItemEdgeHor = (width * lItemEdgePercent).toInt()

        val lItemInner = (width * lItemInnerPercent).toInt()
        lItemInnerHor = lItemInner
        lItemInnerVer = lItemInner

        val lItemSize = (width - lItemEdgeHor * 2 - lItemInnerHor * (layout.x - 1)) / layout.x
        lItemWith = lItemSize
        lItemHeight = lItemSize

        lItemEdgeVer = (height - lItemSize) / 2

        sItemInner = (width * sItemInnerPercent).toInt()

        val sItemSize = (lItemSize - sItemInner) / 2
        sItemWith = sItemSize
        sItemHeight = sItemSize
    }
}
