package com.thn.videoconstruction.view_customers.model_image_view

import com.thn.videoconstruction.utils.Utils
import java.io.Serializable

class FESlideData(val slideId:Long, val fromImagePath:String, val toImagePath:String, var lookupType: Utils.LookupType = Utils.LookupType.NONE):Serializable