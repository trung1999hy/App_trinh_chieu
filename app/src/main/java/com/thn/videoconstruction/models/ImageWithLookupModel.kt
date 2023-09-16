package com.thn.videoconstruction.models

class ImageWithLookupModel(private val mImageWithLookup: ImageWithLookup) {

    val imagePath
    get() = mImageWithLookup.imagePath

    val id
    get() = mImageWithLookup.id

    var lookupType = mImageWithLookup.lookupType
}