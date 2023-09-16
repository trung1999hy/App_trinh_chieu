package com.thn.videoconstruction.ui_effect

import com.thn.videoconstruction.models.UIEffectData

object UIEffectUtils {

    enum class EffectType {
        NONE,
        SNOW,
        RAIN,
        WISP,
        WAVY,
        ZOOM_BLUR,
        CROSS_HATCHING,
        CROSS,
        GLITCH,
        TV_SHOW,
        MIRROR_H2,
        TILES,
        GRAY_SCALE,
        SPLIT_COLOR,
        POLYGON,
        DAWN,
        HALF_TONE
    }

    fun getEffectByType(effectType: EffectType) :FEEffect{
        return when(effectType) {
            EffectType.NONE -> FEEffect()
            EffectType.SNOW -> FEEffectSnow()
            EffectType.RAIN -> FEEffectRain()
            EffectType.WISP -> FEEffectWisp()
            EffectType.WAVY -> FEEffectWavy()
            EffectType.ZOOM_BLUR -> FEEffectZoomBlur()
            EffectType.CROSS_HATCHING -> FEEffectCrossHatching()
            EffectType.CROSS -> FEEffectCross()
            EffectType.GLITCH -> FEEffectGlitch()
            EffectType.TV_SHOW -> FEEffectTVShow()
            EffectType.MIRROR_H2 -> FEEffectMirrorH2()
            EffectType.TILES -> FEEffectTiles()
            EffectType.GRAY_SCALE -> FEEffectGrayScale()
            EffectType.SPLIT_COLOR -> FEEffectSplitColor()
            EffectType.POLYGON -> FEEffectPolygon()
            EffectType.DAWN -> FEEffectDawn()
            EffectType.HALF_TONE -> FEEffectHalfTone()
        }
    }

    fun getAllGSEffectData():ArrayList<UIEffectData> {
        val effectDataList = ArrayList<UIEffectData>()
        for(item in EffectType.values()) {
            effectDataList.add(UIEffectData(item))
        }
        return effectDataList
    }

}