package com.thn.videoconstruction.modules.music_player

import com.thn.videoconstruction.models.MusicReturn

interface FEMusicPlayers {

    fun play()
    fun pause()
    fun changeState()
    fun changeMusic(audioFilePath:String)
    fun changeMusic(audioFilePath:String, startOffset: Int, length: Int)
    fun seekTo(offset:Int)
    fun changeStartOffset(startOffset:Int)
    fun changeLength(length:Int)
    fun release()
    fun getOutMusic(): MusicReturn
}