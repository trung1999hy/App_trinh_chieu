package com.thn.videoconstruction.fe_ui.slide_show

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ShowViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ShowViewModel() as T
    }

}