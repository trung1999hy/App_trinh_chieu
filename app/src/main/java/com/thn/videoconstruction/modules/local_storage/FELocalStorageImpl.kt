package com.thn.videoconstruction.modules.local_storage

import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.models.AudioData
import com.thn.videoconstruction.models.Media
import com.thn.videoconstruction.utils.TypeMedia
import com.thn.videoconstruction.utils.Loggers
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

class FELocalStorageImpl :
    FELocalStorage {

    override val audioDataResponse = MutableLiveData<ArrayList<AudioData>>()

    override val mediaResponse = MutableLiveData<ArrayList<Media>>()


    override fun getAllAudio() {
        val audioList = arrayListOf<AudioData>()
        Observable.fromCallable<ArrayList<AudioData>> {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val orderBy = MediaStore.Audio.Media.DATE_ADDED
            val selectionMusic = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val cursor: Cursor =
                FEMainApp.getContext().contentResolver.query(
                    uri,
                    null,
                    selectionMusic, null, "$orderBy DESC"
                )!!
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val audioName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                        ?: ""
                val mineType =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)) ?: ""
                val duration = try {
                    cursor.getLong(cursor.getColumnIndex("duration"))
                } catch (e: Exception) {
                    continue
                }
                if (filePath.toLowerCase().contains(".m4a") || filePath.toLowerCase()
                        .contains(".mp3")
                ) {
                    if (duration > 10000) {
                        audioList.add(AudioData(filePath, audioName, mineType, duration))
                    }
                }
            }
            cursor.close()
            return@fromCallable audioList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<AudioData>> {
                override fun onNext(t: ArrayList<AudioData>) {
                    audioDataResponse.postValue(t)
                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }


    override fun getAllMedia(typeMedia: TypeMedia) {
        when (typeMedia) {
            TypeMedia.PHOTO -> getAllPhoto()
        }
    }


    private fun getAllPhoto() {
        val mediaList = arrayListOf<Media>()
        Observable.fromCallable<ArrayList<Media>> {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val orderBy = MediaStore.Images.Media.DATE_ADDED
            val cursor: Cursor =
                FEMainApp.getContext().contentResolver.query(
                    uri,
                    null,
                    null, null, "$orderBy DESC"
                )!!
            var file: File
            while (cursor.moveToNext()) {


                val dateAdded =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)) ?: ""
                val folderContainName = File(path)?.parentFile?.name ?: ""
                if (path.toLowerCase().contains(".tif") || path.toLowerCase()
                        .contains(".psd") || path.toLowerCase().contains(".ai")
                ) continue
                Loggers.e("image length = ${File(path).length()}")
                if (File(path).length() > 100)
                    if (!path.toLowerCase().contains(".gif") && !path.toLowerCase()
                            .contains("!\$&welcome@#image")
                    ) {
                        file = File(path)

                        if (file.exists()) {
                            val folderContainId = file.parentFile?.absolutePath ?: ""
                            mediaList.add(
                                Media(
                                    dateAdded * 1000,
                                    path,
                                    file.name,
                                    TypeMedia.PHOTO,
                                    folderContainId,
                                    folderContainName
                                )
                            )
                        }
                    }

            }
            cursor.close()
            return@fromCallable mediaList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<Media>> {
                override fun onNext(t: ArrayList<Media>) {
                    mediaResponse.postValue(t)
                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }



}