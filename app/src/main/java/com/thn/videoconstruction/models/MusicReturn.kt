package com.thn.videoconstruction.models

import java.io.Serializable

class MusicReturn(val audioFilePath:String, var outFilePath:String="", val startOffset:Int, val length:Int):Serializable