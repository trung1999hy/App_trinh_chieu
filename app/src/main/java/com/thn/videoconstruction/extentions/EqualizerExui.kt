package com.thn.videoconstruction.extentions

import android.media.audiofx.Equalizer

fun Equalizer.setEffect(dBList:IntArray) {
    dBList.forEachIndexed { index, dB ->
        setBandLevel(index.toShort(), dB.toShort())
    }
}