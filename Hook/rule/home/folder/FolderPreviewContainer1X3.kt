package com.xzakota.oshape.startup.rule.home.folder

class FolderPreviewContainer1X3(instance: Any) : BaseFolderPreviewContainer(instance) {
    override val layout = FolderLayout(1, 3, 2, 4)

    override fun onPreMeasure(width: Int, height: Int) {
        lItemEdgeVer = (height * lItemEdgePercent).toInt()

        val lItemInner = (height * lItemInnerPercent).toInt()
        lItemInnerHor = lItemInner
        lItemInnerVer = lItemInner

        val lItemSize = (height - lItemEdgeVer * 2 - lItemInnerVer * (layout.y - 1)) / layout.y
        lItemWith = lItemSize
        lItemHeight = lItemSize

        lItemEdgeHor = (width - lItemSize) / 2

        sItemInner = (height * sItemInnerPercent).toInt()

        val sItemSize = (lItemSize - sItemInner) / 2
        sItemWith = sItemSize
        sItemHeight = sItemSize
    }
}
