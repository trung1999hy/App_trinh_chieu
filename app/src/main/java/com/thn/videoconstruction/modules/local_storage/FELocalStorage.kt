package com.thn.videoconstruction.modules.local_storage

import androidx.lifecycle.MutableLiveData
import com.thn.videoconstruction.models.AudioData
import com.thn.videoconstruction.models.Media
import com.thn.videoconstruction.utils.TypeMedia

interface FELocalStorage {
    val audioDataResponse:MutableLiveData<ArrayList<AudioData>>
    val mediaResponse:MutableLiveData<ArrayList<Media>>

    fun getAllAudio()
    fun getAllMedia(typeMedia: TypeMedia)
}