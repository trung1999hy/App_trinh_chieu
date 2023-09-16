package com.thn.videoconstruction.fe_ui.fe_select_music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thn.videoconstruction.modules.local_storage.FELocalStorage

class FEMusicSelectViewModelFactory (private val FELocalStorage: FELocalStorage) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FEMusicSelectViewModel(FELocalStorage) as T
    }

}