package com.thn.videoconstruction.models

class MediaAlbumModel {
    val mediaItemPaths = arrayListOf<Media>()
    val folderId:String
    val albumName:String

    constructor(albumName:String, folderId:String) {
        this.folderId = folderId
        this.albumName = albumName
    }

}