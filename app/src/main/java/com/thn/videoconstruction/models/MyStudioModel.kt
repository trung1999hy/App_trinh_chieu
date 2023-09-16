package com.thn.videoconstruction.models

import java.io.File

class MyStudioModel(val filePath:String, var dateAdded:Long = 0, val duration:Int):Comparable<MyStudioModel> {

    var checked = false
    init {
        if(filePath.isNotEmpty()) {
            dateAdded = File(filePath).lastModified()
        }
    }

    override fun compareTo(other: MyStudioModel): Int = other.dateAdded.compareTo(dateAdded)
}