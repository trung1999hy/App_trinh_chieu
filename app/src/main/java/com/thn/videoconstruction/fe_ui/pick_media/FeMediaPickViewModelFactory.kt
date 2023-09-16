package com.thn.videoconstruction.fe_ui.pick_media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thn.videoconstruction.modules.local_storage.FELocalStorage

class FeMediaPickViewModelFactory (private val FELocalStorage: FELocalStorage) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FE_MediaPickViewModel(FELocalStorage) as T
    }



}