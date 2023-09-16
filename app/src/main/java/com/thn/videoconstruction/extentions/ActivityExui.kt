package com.thn.videoconstruction.extentions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.thn.videoconstruction.utils.Loggers

fun Activity.openAppInStore() {
    try {
        val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
        if(intent != null) {
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName");
            startActivity(intent)
        }
    }catch (e:java.lang.Exception) {
        Loggers.e(e.toString())
    }
}

fun Activity.openDevPageInGooglePlay() {
    try {
        val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
        if(intent != null) {
            intent.action = Intent.ACTION_VIEW;
            intent.data = Uri.parse("https://play.google.com/store/apps/developer?id=Ani+App+Studio");
            startActivity(intent);
        }
    }catch (e:java.lang.Exception) {
        Loggers.e(e.toString())
    }
}