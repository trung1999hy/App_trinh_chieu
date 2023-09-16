package com.thn.videoconstruction.models

class MediaModel(private val mMedia: Media):Comparable<MediaModel> {

    val filePath = mMedia.filePath
    val dateAdded = mMedia.dateAdded
    var count = 0
    val kind = mMedia.typeMedia
    val duration = mMedia.duration
    override fun compareTo(other: MediaModel): Int = other.dateAdded.compareTo(dateAdded)
}