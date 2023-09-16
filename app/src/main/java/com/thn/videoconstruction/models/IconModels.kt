package com.thn.videoconstruction.models

import androidx.annotation.DrawableRes

data class IconModels(
    var id: Int? = null,
    @DrawableRes
    var image: Int? = null,
    var textIcon: String? = null
)
