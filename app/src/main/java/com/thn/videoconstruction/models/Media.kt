package com.thn.videoconstruction.models

import com.thn.videoconstruction.utils.TypeMedia

data class Media(val dateAdded:Long, val filePath:String = "", val fileName:String="", val typeMedia: TypeMedia = TypeMedia.PHOTO, val folderId:String="", val folderName:String="", val duration:Long=0)