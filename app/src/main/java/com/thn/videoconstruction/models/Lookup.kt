package com.thn.videoconstruction.models

import com.thn.videoconstruction.utils.Utils

data class Lookup(val lookupType: Utils.LookupType, val name:String, var lock: Boolean = false )