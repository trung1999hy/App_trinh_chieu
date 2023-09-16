package com.thn.videoconstruction.modules.audio_manager

import com.thn.videoconstruction.models.MusicReturn

interface AudioManagers {

    fun getAudioName():String
    fun playAudio()
    fun pauseAudio()
    fun returnToDefault(currentTimeMs: Int)
    fun seekTo(currentTimeMs:Int)
    fun repeat()
    fun setVolume(volume:Float)
    fun getVolume():Float
    fun changeAudio(musicReturn: MusicReturn, currentTimeMs: Int)
    fun changeMusic(path:String)
    fun getOutMusicPath():String
    fun getOutMusic(): MusicReturn

    fun useDefault()
}