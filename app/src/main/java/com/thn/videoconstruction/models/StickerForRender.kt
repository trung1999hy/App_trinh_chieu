package com.thn.videoconstruction.models

import java.io.Serializable

data class StickerForRender(val stickerPath: String, val startOffset: Int, val endOffset: Int) : Serializable
