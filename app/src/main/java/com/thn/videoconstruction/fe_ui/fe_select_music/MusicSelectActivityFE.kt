package com.thn.videoconstruction.fe_ui.fe_select_music

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FEMusicListAdapterFE
import com.thn.videoconstruction.base.FEBaseActivity
import com.thn.videoconstruction.models.AudioData
import com.thn.videoconstruction.models.MusicReturn
import com.thn.videoconstruction.ffmpeg_fe.FFmpegFE
import com.thn.videoconstruction.ffmpeg_fe.FFmpegCmdFE
import com.thn.videoconstruction.models.AudioModel
import com.thn.videoconstruction.modules.music_player.FEMusicPlayers
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.activity_select_music.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class MusicSelectActivityFE : FEBaseActivity(), KodeinAware {

    companion object{
        const val SELECT_MUSIC_REQUEST_CODE = 1001
        const val MUSIC_RETURN_DATA_KEY = "MusicSelectActivity.MUSIC_RETURN_DATA_KEY"
    }

    override fun getContentResId(): Int = R.layout.activity_select_music

    override val kodein by closestKodein()

    private val mMusicSelectViewModelFactory:FEMusicSelectViewModelFactory by instance()
    private val mFEMusicPlayers:FEMusicPlayers by instance()

    private lateinit var mFEMusicSelectViewModel: FEMusicSelectViewModel
    private var useAvailable = true
    private val mFEMusicListAdapter = FEMusicListAdapterFE(object :FEMusicListAdapterFE.MusicCallback{
        override fun onClickItem(audioModel: AudioModel) {
            mFEMusicPlayers.changeMusic(audioModel.audioFilePath)
        }

        override fun onClickUse(audioModel: AudioModel) {
            val out = mFEMusicPlayers.getOutMusic()
            if(out.length < 10000) {
                showToast(getString(R.string.minimum_time_is_10_s))
            } else {
                if(!useAvailable) {
                    return
                }
                useAvailable = false
                performUseMusic(out.audioFilePath, out.startOffset.toLong(), out.startOffset+out.length.toLong(), audioModel.fileType)
            }
        }

        override fun onClickPlay(isPlaying:Boolean) {
            mFEMusicPlayers.changeState()
        }

        override fun onChangeStart(
            startOffsetMilSec: Int,
            lengthMilSec: Int
        ) {
            mFEMusicPlayers.changeStartOffset(startOffsetMilSec)
            mFEMusicPlayers.changeLength(lengthMilSec)
        }

        override fun onChangeEnd(lengthMilSec: Int) {
            mFEMusicPlayers.changeLength(lengthMilSec)
        }

    })

    private var mCurrentMusic: MusicReturn? = null

    override fun initViews() {
        intent.getBundleExtra("bundle")?.let {
            it.getSerializable("CurrentMusic")?.let { serializable ->
                val musicData = serializable as MusicReturn
                mCurrentMusic = musicData
            }
        }
        musicListView.adapter = mFEMusicListAdapter
        musicListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mFEMusicSelectViewModel = ViewModelProvider(this, mMusicSelectViewModelFactory).get(FEMusicSelectViewModel::class.java)

        listen()

        mFEMusicSelectViewModel.FELocalStorage.getAllAudio()

        setRightButton(R.drawable.ic_search_white_24dp) {
            showSearchInput()
        }

        setSearchInputListener {
           onSearch(it)
        }
    }

    private fun onSearch(query:String) {
        onPauseMusic()
        mFEMusicListAdapter.setOffAll()
        val result = ArrayList<AudioData>()
        for(item in mAllMusicList) {
            Loggers.e("music name = ${item.musicName}")
            if(item.musicName.toLowerCase().contains(query.toLowerCase())) {
                result.add(item)
            }
        }
        mFEMusicListAdapter.setAudioDataList(result)
    }

    private val mAllMusicList = ArrayList<AudioData>()

    private fun listen() {
        mFEMusicSelectViewModel.FELocalStorage.audioDataResponse.observe(this, Observer {
            mFEMusicListAdapter.setAudioDataList(it)
            mAllMusicList.addAll(it)
            mCurrentMusic?.let {musicReturnData ->
                val index = mFEMusicListAdapter.restoreBeforeMusic(musicReturnData)
                if(index >= 0) {
                    musicListView.scrollToPosition(index)
                    mFEMusicPlayers.changeMusic(musicReturnData.audioFilePath, musicReturnData.startOffset, musicReturnData.length)
                }

            }

        })
    }

    override fun initActions() {

    }

    private fun performUseMusic(inputAudioPath:String, startOffset:Long, endOffset:Long, fileType:String) {
            mFEMusicPlayers.pause()
        showProgressDialog()
            Thread{
            val outMusicPath:String
                val ex  = inputAudioPath.substring(inputAudioPath.lastIndexOf(".")+1, inputAudioPath.length)
                Loggers.e("ex = $ex")
            if(ex != "m4a") {
                outMusicPath = Utils.getTempAudioOutPutFile(ex)
            } else {
                outMusicPath = Utils.getTempAudioOutPutFile("mp4")
            }

            Loggers.e("out mp3 = $outMusicPath")
            val ffmpeg = FFmpegFE(FFmpegCmdFE.trimAudio(inputAudioPath, startOffset, endOffset, outMusicPath))
            ffmpeg.runCmd {
                try {

                    MediaPlayer().apply {
                        setDataSource(outMusicPath)
                        prepare()
                        setOnPreparedListener {

                        }
                    }

                    val musicReturn = MusicReturn(inputAudioPath, outMusicPath, startOffset.toInt(), endOffset.toInt()-startOffset.toInt())
                    runOnUiThread {
                        val returnIntent = Intent()
                        Bundle().apply {
                            putSerializable(MUSIC_RETURN_DATA_KEY, musicReturn)
                            returnIntent.putExtra("bundle", this)
                        }
                        setResult(Activity.RESULT_OK, returnIntent)
                        dismissProgressDialog()
                        finish()
                    }
                } catch (e :Exception) {
                    runOnUiThread {
                        dismissProgressDialog()
                        useAvailable = true
                        showToast(getString(R.string.have_an_error_try_another_music_file))
                    }

                }

            }
        }.start()


    }

    override fun onBackPressed() {
        if(searchMode) {
            hideSearchInput()
        } else {
            super.onBackPressed()

        }

    }

    override fun screenTitle(): String = getString(R.string.select_music)

    private fun onPauseMusic() {
        mFEMusicPlayers.pause()
        mFEMusicListAdapter.onPause()
    }

    override fun onPause() {
        super.onPause()
        onPauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        mFEMusicPlayers.release()
    }

}
