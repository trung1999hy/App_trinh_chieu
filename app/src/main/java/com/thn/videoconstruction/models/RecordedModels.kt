package com.thn.videoconstruction.models

class RecordedModels(private val mRecorded: Recorded) {
    val path = mRecorded.recordFilePath
    var isSelect = false
    val startOffset = mRecorded.startMs
    val endOffset = mRecorded.endMs
    fun checkTime(timeMs:Int):Boolean {
        if(timeMs >= mRecorded.startMs && timeMs <= mRecorded.endMs) return true
        return false
    }
}