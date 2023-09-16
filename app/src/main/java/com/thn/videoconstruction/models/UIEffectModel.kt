package com.thn.videoconstruction.models

import com.thn.videoconstruction.ui_effect.UIEffectUtils

class UIEffectModel (val UIEffectData: UIEffectData) {
    val gsEffect = UIEffectUtils.getEffectByType(UIEffectData.effectType)
    val name = gsEffect.name
    var isSelect = false
}