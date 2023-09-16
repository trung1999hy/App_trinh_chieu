package com.thn.videoconstruction.transition

import com.thn.videoconstruction.R
import java.io.Serializable

open class FETransition (val transitionCodeId:Int= R.raw.none_transition_code, val transitionName:String="None", var lock: Boolean = false) :Serializable {

}