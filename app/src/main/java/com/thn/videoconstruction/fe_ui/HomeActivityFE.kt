package com.thn.videoconstruction.fe_ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FEMyStudioInHomeAdapterFE
import com.thn.videoconstruction.adapter.FEThemeHomeAdapterFE
import com.thn.videoconstruction.base.FEBaseActivity
import com.thn.videoconstruction.models.MyStudioModel
import com.thn.videoconstruction.fe_ui.pick_media.MediaPickActivityFE
import com.thn.videoconstruction.fe_ui.share_video.VideoShareActivityFE
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.TypeMedia
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.activity_home.bgButtonSlideShow
import kotlinx.android.synthetic.main.activity_home.icNoProject
import kotlinx.android.synthetic.main.activity_home.iv_share_app
import kotlinx.android.synthetic.main.activity_home.myStudioListView
import kotlinx.android.synthetic.main.activity_home.nestedScrollView
import kotlinx.android.synthetic.main.activity_home.tvVersion
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class HomeActivityFE : FEBaseActivity() {

    companion object {
        const val TAKE_PICTURE = 1001
        const val RECORD_CAMERA = 1991
        const val CAMERA_PERMISSION_REQUEST = 1002
        const val STORAGE_PERMISSION_REQUEST = 1003
    }

    private val mFEThemeHomeAdapter = FEThemeHomeAdapterFE()
    private val viewModel: FEHomeViewModel by viewModels {
        FEHomeViewModel.HomeViewModelFactory(this.applicationContext)
    }
    private val mMyStudioAdapter = FEMyStudioInHomeAdapterFE()

    override fun getContentResId(): Int = R.layout.activity_home

    private var onSplashComplete = false
    override fun initViews() {
        setUpTextViewVerSion()
        comebackStatus = getString(R.string.do_you_want_to_leave)
        hideHeader()
        myStudioListView.apply {
            adapter = mMyStudioAdapter
            val layoutManager =
                GridLayoutManager(this@HomeActivityFE, 2, GridLayoutManager.VERTICAL, false)
            this.layoutManager = layoutManager
        }

        Utils.linkThemeList.forEach {
            mFEThemeHomeAdapter.addItem(it)
        }

        mFEThemeHomeAdapter.onItemClick = { linkData ->

            val themFilePath = Utils.themeFolderPath + "/${linkData.fileName}.mp4"

            if (linkData.link == "none") {

            } else {
                if (File(themFilePath).exists()) {
                    gotoPickMedia(TypeMedia.PHOTO, linkData.fileName)
                } else {

                    if (!checkSettingAutoUpdateTime()) {
                        showToast(getString(R.string.please_set_auto_update_time))
                    } else {
                        if (Utils.isInternetAvailable()) {
                            showDownloadThemeDialog(linkData, {
                                mFEThemeHomeAdapter.notifyDataSetChanged()
                            }, {
                                mFEThemeHomeAdapter.notifyDataSetChanged()
                            })

                        } else {
                            showToast(getString(R.string.no_internet_connection_please_connect_to_the_internet_and_try_again))
                        }
                    }
                }
            }

        }


        Loggers.e("check storage permission in on create = ${checkStoragePermission()}")
        if (!checkStoragePermission()) {
            requestStoragePermission()
        }
        iv_share_app.setOnClickListener { shareApp() }
    }

    private fun shareApp() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appName = packageInfo.applicationInfo.labelRes
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "\uD83D\uDD90 Create professional-looking slideshows and videos without any design experience using ${getString(appName)}. \n" +
                        "https://play.google.com/store/apps/details?id=com.thn.videoconstruction&hl=vi&gl=US")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun setUpTextViewVerSion() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appName = packageInfo.applicationInfo.labelRes
        val versionCode = packageInfo.versionCode
        tvVersion.text = "App Name: ${getString(appName)}\nVersion Code: $versionCode"
    }

    private fun onInit() {
        onSplashComplete = true
        needShowDialog = true
        if (checkStoragePermission()) {

            Thread {
                try {
                    initThemeData()
                    initDefaultAudio()
                    getAllMyStudioItem()
                    Utils.clearTemp()
                } catch (e: Exception) {

                }

            }.start()

        } else {

        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO),
            STORAGE_PERMISSION_REQUEST
        ) else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST
            )
    }


    override fun initActions() {
        bgButtonSlideShow.setOnClickListener {
            gotoPickMedia(TypeMedia.PHOTO)
        }

        mMyStudioAdapter.onClickItem = {
            VideoShareActivityFE.gotoActivity(this, it.filePath)
        }


    }

    private fun countDownAvailable() {
        object : CountDownTimer(1000, 1000) {
            override fun onFinish() {
                pickMediaAvailable = true
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }

    private var pickMediaAvailable = true
    private fun gotoPickMedia(typeMedia: TypeMedia) {

        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        if (Utils.getAvailableSpaceInMB() < 200) {
            showToast(getString(R.string.free_space_too_low))
            return
        }
        if (pickMediaAvailable) {
            pickMediaAvailable = false
            MediaPickActivityFE.gotoActivity(this@HomeActivityFE, typeMedia)
            countDownAvailable()
        }

    }

    private fun gotoPickMedia(typeMedia: TypeMedia, themePath: String) {

        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        if (Utils.getAvailableSpaceInMB() < 200) {
            showToast(getString(R.string.free_space_too_low))
            return
        }
        if (pickMediaAvailable) {
            pickMediaAvailable = false
            MediaPickActivityFE.gotoActivity(this, typeMedia, themePath)
            countDownAvailable()

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            return
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty()) {
                val checkPermission: Boolean
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPermission =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                } else {
                    checkPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                }
                if (checkPermission) {
                    onInit()
                } else {
                    checkShouldShowRequestPermissionRationale()
                }
            } else {
                checkShouldShowRequestPermissionRationale()
            }
        } else {
            openActSetting()
        }
        return
    }

    fun checkShouldShowRequestPermissionRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            ) {
                requestStoragePermission()
            } else {
                openActSetting()
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            requestStoragePermission()
        } else {
            openActSetting()
        }
    }


    private var showSetting = false
    protected fun openActSetting() {

        val view = showYesNoDialogForOpenSetting(
            getString(R.string.anser_grant_permission) + "\n" + getString(R.string.goto_setting_and_grant_permission),
            {
                Loggers.e("click Yes")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                showToast(getString(R.string.please_grant_read_external_storage))
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                showSetting = true
            },
            { finishAfterTransition(); },
            { finishAfterTransition(); })

    }

    private fun initThemeData() {
        val themeFolder = File(Utils.themeFolderPath)
        if (!themeFolder.exists()) {
            themeFolder.mkdirs()
        }
        copyDefaultTheme()
    }

    private fun getData() {

    }

    private fun copyDefaultTheme() {
        val fileInAsset = assets.list("theme-default")
        fileInAsset?.let {
            for (fileName in fileInAsset) {
                val fileOut = File("${Utils.themeFolderPath}/$fileName")
                if (!fileOut.exists()) {
                    val inputStream = assets.open("theme-default/$fileName")
                    val outputStream = FileOutputStream(fileOut)
                    copyFile(inputStream, outputStream)
                }
            }
        }
    }

    fun initDefaultAudio() {
        val audioFolder = File(Utils.audioDefaultFolderPath)
        if (!audioFolder.exists()) {
            audioFolder.mkdirs()
        }
        copyDefaultAudio()
    }

    private fun copyDefaultAudio() {
        val fileInAsset = assets.list("audio")
        fileInAsset?.let {
            for (fileName in fileInAsset) {
                val fileOut = File("${Utils.audioDefaultFolderPath}/$fileName")
                if (!fileOut.exists()) {
                    val inputStream = assets.open("audio/$fileName")
                    val outputStream = FileOutputStream(fileOut)
                    copyFile(inputStream, outputStream)
                }
            }
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.close()
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAllMyStudioItem() {
        Thread {
            val folder = File(Utils.myStuioFolderPath)
            val myStudioDataList = ArrayList<MyStudioModel>()
            if (folder.exists() && folder.isDirectory) {
                for (item in folder.listFiles()) {
                    try {
                        val duration = Utils.getVideoDuration(item.absolutePath)
                        myStudioDataList.add(
                            MyStudioModel(
                                item.absolutePath,
                                item.lastModified(),
                                duration
                            )
                        )
                    } catch (e: Exception) {
                        item.delete()
                        doSendBroadcast(item.absolutePath)
                        continue
                    }

                }
            }

            runOnUiThread {
                mMyStudioAdapter.setItemList(myStudioDataList)
                if (mMyStudioAdapter.itemCount < 1) {
                    icNoProject.visibility = View.VISIBLE
                } else {
                    icNoProject.visibility = View.GONE
                }
            }

        }.start()
    }

    private var mOnPause = false
    override fun onPause() {
        super.onPause()
        mOnPause = true
    }

    override fun onBackPressed() {

        if (mRateDialogShowing) return


        if (!checkStoragePermission()) {
            return
        }
        isHome = true
        super.onBackPressed()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        if (onSplashComplete == false && mOnPause) {
        }

        mFEThemeHomeAdapter.notifyDataSetChanged()
        if (checkStoragePermission()) {
            getAllMyStudioItem()
            onInit()
        } else {

        }
        if (mOnPause) {
        }
        if (showSetting && !checkStoragePermission()) {
            showSetting = false
            openActSetting()
        }
    }


}
