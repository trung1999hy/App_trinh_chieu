package com.thn.videoconstruction.models

class ImageModel {
    val filePath:String
    var count = 0
    val dateAdded:Long
    constructor(imageData: Image) {
        this.filePath = imageData.filePath
        this.dateAdded = imageData.dateAdded
    }

    constructor(dateAdded:Long) {
        this.dateAdded = dateAdded
        filePath = ""
    }
}