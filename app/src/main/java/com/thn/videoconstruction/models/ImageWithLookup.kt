package com.thn.videoconstruction.models

import com.thn.videoconstruction.utils.Utils

class ImageWithLookup(val id:Int, val imagePath:String, var lookupType: Utils.LookupType=Utils.LookupType.NONE) {
}