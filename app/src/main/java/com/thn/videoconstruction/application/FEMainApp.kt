package com.thn.videoconstruction.application

import android.app.Application
import android.provider.Settings

import com.thn.videoconstruction.modules.audio_manager.AudioManagers
import com.thn.videoconstruction.modules.audio_manager.AudioManagerIP
import com.thn.videoconstruction.modules.local_storage.FELocalStorage
import com.thn.videoconstruction.modules.local_storage.FELocalStorageImpl
import com.thn.videoconstruction.modules.music_player.FEMusicPlayers
import com.thn.videoconstruction.modules.music_player.FEMusicPlayersImpls
import com.thn.videoconstruction.fe_ui.pick_media.FeMediaPickViewModelFactory
import com.thn.videoconstruction.fe_ui.fe_select_music.FEMusicSelectViewModelFactory
import com.thn.videoconstruction.fe_ui.slide_show.ShowViewModelFactory
import com.thn.videoconstruction.utils.Preference
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class FEMainApp : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@FEMainApp))

        bind<FELocalStorage>() with singleton { FELocalStorageImpl() }
        bind() from provider { FeMediaPickViewModelFactory(instance()) }
        bind() from provider { FEMusicSelectViewModelFactory(instance()) }
        bind() from provider { ShowViewModelFactory() }
        bind<AudioManagers>() with provider { AudioManagerIP() }
        bind<FEMusicPlayers>() with provider { FEMusicPlayersImpls() }
    }


    companion object {
        lateinit var instance: FEMainApp
        fun getContext() = instance.applicationContext!!
    }

    private var preference: Preference? = null


    override fun onCreate() {
        super.onCreate()
        instance = this
        preference = Preference.buildInstance(this)
        if (preference?.firstInstall == false) {
            preference?.firstInstall = true
            preference?.setValueCoin(100)
        }

    }


    fun getPreference(): Preference? {
        return preference
    }

    val deviceId: String
        get() = Settings.Secure.getString(instance.contentResolver, Settings.Secure.ANDROID_ID)

}