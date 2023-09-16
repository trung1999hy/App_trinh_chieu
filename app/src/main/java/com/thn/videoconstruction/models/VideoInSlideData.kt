package com.thn.videoconstruction.models

import android.view.View
import com.thn.videoconstruction.ui_effect.UIEffectUtils
import java.io.Serializable

class VideoInSlideData(val path:String, val id:Int=View.generateViewId(), var gsEffectType: UIEffectUtils.EffectType=UIEffectUtils.EffectType.NONE):Serializable {
}