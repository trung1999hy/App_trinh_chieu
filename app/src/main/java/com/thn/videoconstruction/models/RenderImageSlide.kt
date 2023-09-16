package com.thn.videoconstruction.models

import java.io.Serializable

data class RenderImageSlide(
    val imageList: ArrayList<String>,
    val bitmapHashMap:HashMap<String, String>,
    val videoQuality: Int,
    val delayTimeSec: Int,
    val theme: Theme,
    val musicReturn: MusicReturn?,
    val FETransition: com.thn.videoconstruction.transition.FETransition,
    val stickerAddedForRender : ArrayList<StickerForRender>
) :Serializable