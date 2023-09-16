package com.thn.videoconstruction.models

class ThemeModel(val theme: Theme) {

    val name = theme.themeName
    val videoPath = theme.themeVideoFilePath
    var selected = false

}