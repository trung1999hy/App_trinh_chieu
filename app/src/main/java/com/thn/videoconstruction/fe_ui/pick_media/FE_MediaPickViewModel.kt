package com.thn.videoconstruction.fe_ui.pick_media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thn.videoconstruction.models.Media
import com.thn.videoconstruction.models.MediaModel
import com.thn.videoconstruction.models.MediaPickedModel
import com.thn.videoconstruction.modules.local_storage.FELocalStorage

class FE_MediaPickViewModel (val FELocalStorage: FELocalStorage) : ViewModel() {

    private val mItemJustPicked = MutableLiveData<MediaModel>()
    val itemJustPicked get() = mItemJustPicked

    private val mItemJustDeleted = MutableLiveData<MediaPickedModel>()
    val itemJustDeleted get() = mItemJustDeleted

    private val mActiveCounter = MutableLiveData<Boolean>(true)
    val acctiveCounter get() = mActiveCounter

    private val mNewMediaItem = MutableLiveData<Media>()
    val newMediaItem get() = mNewMediaItem

    private val mFolderIsShowing = MutableLiveData<Boolean>(false)
    val folderIsShowingLiveData get() = mFolderIsShowing

    var folderIsShowing = false

    private val mMediaPickedCount = HashMap<String, Int>()
    val mediaPickedCount get() = mMediaPickedCount


    fun onShowFolder() {
        folderIsShowing = true
    }

    fun hideFolder() {
        folderIsShowing = false
        mFolderIsShowing.postValue(false)
    }

    fun onPickImage(mediaModel: MediaModel) {
        var count = mMediaPickedCount[mediaModel.filePath] ?: 0
        mMediaPickedCount[mediaModel.filePath]=count+1
        mItemJustPicked.postValue(mediaModel)
    }
    fun updateCount(pathList:ArrayList<String>) {
        for(path in pathList) {
            var count = mMediaPickedCount[path] ?: 0
            mMediaPickedCount[path]=count+1
        }

    }


    fun onDelete(mediaDataModel: MediaPickedModel) {
        var count = mMediaPickedCount[mediaDataModel.path] ?: 0
        mMediaPickedCount[mediaDataModel.path]=count-1
        mItemJustDeleted.postValue(mediaDataModel)
    }

    fun disableCounter() {
        mActiveCounter.postValue(false)
    }

    fun addNewMediaData(media: Media) {
        mNewMediaItem.postValue(media)
    }



}