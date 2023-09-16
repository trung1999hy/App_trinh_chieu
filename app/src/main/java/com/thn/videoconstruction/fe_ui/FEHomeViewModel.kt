package com.thn.videoconstruction.fe_ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thn.videoconstruction.models.VideoSaves
import com.datnt.slideshowmaker.data_local.MyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FEHomeViewModel(var context: Context) : ViewModel() {
    private val database = MyDatabase.getInstance(context).videoSaveDao()

    fun update(videoSaves: VideoSaves) = viewModelScope.launch(Dispatchers.IO) {
        database.update(videoSaves)
    }.invokeOnCompletion {
        Log.d("error", it?.message.toString())
    }

    fun add(videoSaves: VideoSaves) = viewModelScope.launch(Dispatchers.IO) {
        database.insert(videoSaves)
    }.invokeOnCompletion {
        Log.d("error", it?.message.toString())
    }

    fun getAll() = database.getAll()
    fun delete(videoSaves: VideoSaves) = viewModelScope.launch(Dispatchers.IO) {
        database.delete(videoSaves)
    }


    class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FEHomeViewModel::class.java)) {
                return FEHomeViewModel(context) as T
            }
            throw IllegalArgumentException("loiN")
        }

    }
}