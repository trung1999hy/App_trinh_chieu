package com.thn.videoconstruction.models

import java.io.Serializable

class Theme(val themeVideoFilePath:String="none", val themeType: ThemType = ThemType.NOT_REPEAT, val themeName:String="None") :Serializable{

    enum class ThemType {
        REPEAT,NOT_REPEAT
    }
}