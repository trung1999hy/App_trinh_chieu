package com.thn.videoconstruction.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Environment
import android.os.StatFs
import android.os.StrictMode
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.util.Size
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.thn.videoconstruction.R
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.models.Lookup
import com.thn.videoconstruction.models.Theme
import com.thn.videoconstruction.models.ThemeLink
import com.thn.videoconstruction.transition.FETransition
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import org.kodein.di.android.BuildConfig
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.InvalidParameterException
import kotlin.math.max
import kotlin.math.min

object Utils {
    enum class TransitionType {
        NONE,
        ANGULAR,
        BOUNCE,
        BOW_TIE_HORIZONTAL,
        BOW_TIE_VERTICAL,
        BUTTERFLY_WAVE,
        CANNABIS_LEAF,
        CIRCLE_CROP,
        CIRCLE,
        CIRCLE_OPEN,
        COLOR_PHASE,
        COLOR_DISTANCE
    }

    fun getTransitionByType(transitionType: TransitionType): com.thn.videoconstruction.transition.FETransition {
        return when (transitionType) {
            TransitionType.NONE -> com.thn.videoconstruction.transition.FETransition()

            TransitionType.ANGULAR -> com.thn.videoconstruction.transition.FEAngularTransition()
            TransitionType.BOUNCE -> com.thn.videoconstruction.transition.FEBounce()
            TransitionType.BOW_TIE_HORIZONTAL -> com.thn.videoconstruction.transition.FEBowTieHorizontal()
            TransitionType.BOW_TIE_VERTICAL -> com.thn.videoconstruction.transition.FeBowTieVertical()
            TransitionType.BUTTERFLY_WAVE -> com.thn.videoconstruction.transition.FEButterflyWave()
            TransitionType.CANNABIS_LEAF -> com.thn.videoconstruction.transition.FeCannabisLeaf()
            TransitionType.CIRCLE_CROP -> com.thn.videoconstruction.transition.FeCircleCrop()
            TransitionType.CIRCLE -> com.thn.videoconstruction.transition.FECircle()
            TransitionType.CIRCLE_OPEN -> com.thn.videoconstruction.transition.FECircleOpenTransition()
            TransitionType.COLOR_PHASE -> com.thn.videoconstruction.transition.FEColorPhase()
            TransitionType.COLOR_DISTANCE -> com.thn.videoconstruction.transition.FEColorDistance()

        }
    }

    fun createFragmentShaderCode(transitionCodeId: Int): String {
        return readTextFileFromRawResource(FEMainApp.getContext(), transitionCodeId)
    }

    fun getGSTransitionList(): ArrayList<FETransition> {
        var number: Int = 0
        val FETransitionList = ArrayList<FETransition>()

        for (value in TransitionType.values()) {
            if (number > 5) {
                FEMainApp.instance
                    .getPreference()?.run {
                        var itemSet: MutableSet<String> = getListKeyBy() ?: mutableSetOf()
                        FETransitionList.add(getTransitionByType(value).apply {
                            this.lock = !itemSet.contains(number.toString())
                        })
                    }
            } else {
                FETransitionList.add(getTransitionByType(value))
            }
            number++
        }
        return FETransitionList
    }

    fun getThemeDataList(): ArrayList<Theme> {
        val themeList = ArrayList<Theme>()
        themeList.add(Theme("none", Theme.ThemType.NOT_REPEAT, "none"))
        val themeFolder = File(themeFolderPath)
        if (themeFolder.exists() && themeFolder.isDirectory) {
            for (file in themeFolder.listFiles()) {
                themeList.add(
                    Theme(
                        file.absolutePath,
                        Theme.ThemType.NOT_REPEAT,
                        file.name
                    )
                )
            }
        }
        return themeList
    }

    fun convertSecToTimeString(sec: Int): String {
        return if (sec >= 3600) {
            val h = zeroPrefix((sec / 3600).toString())
            val m = zeroPrefix(((sec % 3600) / 60).toString())
            val s = zeroPrefix(((sec % 3600) % 60).toString())
            "$h:$m:$s"
        } else {
            val m = zeroPrefix(((sec % 3600) / 60).toString())
            val s = zeroPrefix(((sec % 3600) % 60).toString())
            "$m:$s"
        }
    }

    private fun zeroPrefix(string: String): String {
        if (string.length < 2) return "0$string"
        return string
    }

    fun getTextWidth(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }

    fun getTextHeight(text: String, paint: Paint): Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    fun getAvailableSpaceInMB(): Long {
        val SIZE_KB = 1024L
        val SIZE_MB = SIZE_KB * SIZE_KB
        val availableSpace: Long
        try {
            val stat = StatFs(getVideoAppDirectory())
            availableSpace = stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: java.lang.Exception) {
            return 120
        }
        return availableSpace / SIZE_MB
    }

    fun getVideoAppDirectory(): String? {
        val folder =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                FEMainApp.getContext().getString(R.string.app_name)
            )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder.absolutePath
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = if (connectivityManager != null) {
            connectivityManager.activeNetworkInfo
        } else null
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun isInternetAvailable(): Boolean {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            val urlc: HttpURLConnection =
                URL("https://www.google.com/").openConnection() as HttpURLConnection
            urlc.setRequestProperty("User-Agent", "Test")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 500 //choose your own timeframe
            urlc.readTimeout = 500 //choose your own timeframe
            urlc.connect()
            return urlc.responseCode == 200
        } catch (e: IOException) {
            return false //connectivity exists, but no internet.
        }

    }

    val linkThemeList = ArrayList<ThemeLink>().apply {
        add(ThemeLink("PUT_THEME_FILE_URL_HERE", "theme_boom_shape", "Boom Shape"))
        add(ThemeLink("PUT_THEME_FILE_URL_HERE", "theme_balloon", "Ballon"))
        add(ThemeLink("PUT_THEME_FILE_URL_HERE", "green_chrismas", "Green christmas"))
        add(ThemeLink("PUT_THEME_FILE_URL_HERE", "theme_birthday", "Birthday"))
    }


    private const val mSharedPreferenceName = "ratio_data"
    private const val mRatioKey = "ratio_key"


    fun checkStorageSpace(listFilePath: ArrayList<String>): Boolean {
        var totalFileLength = 0L
        for (index in 0 until listFilePath.size) {
            val file = File(listFilePath[index])
            if (file.exists()) {
                val fileLength = file.length()
                totalFileLength += fileLength
            }
        }

        val currentFreeSpace = getAvailableSpaceInMB() * 1024 * 1024

        Loggers.e("currentFreeSpace = $currentFreeSpace   totalFileLength = $totalFileLength")
        if (currentFreeSpace > (totalFileLength * 2)) return true
        return false
    }

    fun convertSecondsToTime(seconds: Int): String {
        val timeStr: String
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) {
            return "00:00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr =
                    "00:" + unitFormat(minute) + ":" + unitFormat(
                        second
                    )
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr =
                    unitFormat(hour) + ":" + unitFormat(
                        minute
                    ) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }

    private fun unitFormat(i: Int): String? {
        return if (i in 0..9) {
            "0$i"
        } else {
            "" + i
        }
    }

    fun getBitmapFromFilePath(filePath: String): Bitmap {
        try {
            val file = File(filePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            return modifyOrientation(bitmap, filePath)
        } catch (e: Exception) {
            return getBlackBitmap()
        }

    }

    fun resizeBitmap(path: String?, maxSize: Int): Bitmap? {
        val bitmap: Bitmap?
        val file = File(path)
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        val fis = FileInputStream(file)
        BitmapFactory.decodeStream(fis, null, bmOptions)
        fis.close()
        var scale = 1f
        if (bmOptions.outWidth > maxSize || bmOptions.outHeight > maxSize) {
            val ratioW = bmOptions.outWidth.toFloat() / maxSize
            val ratioH = bmOptions.outHeight.toFloat() / maxSize
            val ratio = max(ratioW, ratioH)
            scale = ratio
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = scale.toInt()
        val fs = FileInputStream(file)
        bitmap = BitmapFactory.decodeStream(fs, null, options)
        fs.close()
        return bitmap
    }

    fun getStickerFromFilePath(filePath: String): Bitmap {
        val file = File(filePath)
        return BitmapFactory.decodeFile(file.absolutePath)

    }

    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotate(bitmap, 90f)
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotate(bitmap, 180f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotate(bitmap, 270f)
            }

            else -> {
                bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeWrapBitmap(bitmapInput: Bitmap, size: Float): Bitmap {
        if (bitmapInput.width < size && bitmapInput.height < size) return bitmapInput
        val scale = min((size / bitmapInput.width), (size / bitmapInput.height))
        return Bitmap.createScaledBitmap(
            bitmapInput,
            (bitmapInput.width * scale).toInt(),
            (bitmapInput.height * scale).toInt(),
            true
        )
    }

    fun resizeMatchBitmap(bitmapInput: Bitmap, size: Float): Bitmap {
        val scale = max((size / bitmapInput.width), (size / bitmapInput.height))
        return Bitmap.createScaledBitmap(
            bitmapInput,
            (bitmapInput.width * scale).toInt(),
            (bitmapInput.height * scale).toInt(),
            true
        )
    }


    fun blurBitmapV2(bm: Bitmap?, r: Int): Bitmap? {
        if (bm == null) return null
        val radius = 25f
        Loggers.e("size = ${bm.width} x ${bm.height}")
        val rsScript: RenderScript =
            RenderScript.create(FEMainApp.getContext())
        val alloc: Allocation = Allocation.createFromBitmap(rsScript, bm)
        val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
        blur.setRadius(radius)
        blur.setInput(alloc)
        val result =
            Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createTyped(rsScript, alloc.type)
        blur.forEach(outAlloc)
        outAlloc.copyTo(result)

        rsScript.destroy()
        return result
    }

    fun getBlackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                bitmap.setPixel(i, j, Color.BLACK)
            }
        }
        return bitmap
    }

    fun loadBitmapFromUri(path: String?, callBack: (Bitmap?) -> Unit) {
        Glide.with(FEMainApp.getContext()).asBitmap().load(path).apply(
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        ).addListener(object :
            RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                callBack.invoke(null)
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                callBack.invoke(resource)
                return true
            }
        }).submit()
    }

    fun loadBitmapFromXML(id: String, callBack: (Bitmap?) -> Unit) {
        val bitmap = FEMainApp.getContext().getDrawable(id.toInt())?.toBitmap(512, 512)
        callBack.invoke(bitmap)
    }

    fun getBitmapFromAsset(path: String): Bitmap {
        val inputStream = FEMainApp.getContext().assets.open(path)
        return BitmapFactory.decodeStream(inputStream)
    }

    fun loadTexture(context: Context, resourceId: Int): Int {

        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture name.")
        }
        val options = BitmapFactory.Options().apply { inScaled = false }
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureHandle[0]
    }


    fun loadTexture(bitmap: Bitmap): Int {

        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture name.")
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        return textureHandle[0]
    }

    private val TAG = "ShaderHelper"

    fun compileShader(shaderType: Int, shaderSource: String): Int {
        var shaderHandle = GLES20.glCreateShader(shaderType)

        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource)

            GLES20.glCompileShader(shaderHandle)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }

        if (shaderHandle == 0) {
            throw RuntimeException("Error creating shader.")
        }

        return shaderHandle
    }

    fun createAndLinkProgram(
        vertexShaderHandle: Int,
        fragmentShaderHandle: Int,
        attributes: Array<String>?
    ): Int {
        var programHandle = GLES20.glCreateProgram()

        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            if (attributes != null) {
                val size = attributes.size
                for (i in 0 until size) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                }
            }

            GLES20.glLinkProgram(programHandle)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle))
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }

        if (programHandle == 0) {
            throw RuntimeException("Error creating program.")
        }

        return programHandle
    }

    fun getAudioDuration(path: String): Long {

        return try {
            val media = MediaMetadataRetriever()
            media.setDataSource(path)
            val duration = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?: "-1").toLong()
            duration
        } catch (e: Exception) {
            -1
        }
    }

    fun getVideoSize(videoPath: String): Size {
        try {
            val media = MediaMetadataRetriever()
            media.setDataSource(videoPath)
            val rotation =
                (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?: "-1").toInt()
            val videoW: Int
            val videoH: Int
            if (rotation == 90) {
                videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?: "-1").toInt()
                videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?: "-1").toInt()
            } else {
                videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?: "-1").toInt()
                videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?: "-1").toInt()
            }
            return Size(videoW, videoH)
        } catch (e: Exception) {
            return Size(1, 1)
        }


    }

    fun getVideoBitRare(videoPath: String): Int {
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
        val bitRare = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        return (bitRare ?: "-1").toInt()
    }


    fun getVideoDuration(videoPath: String): Int {
        return try {
            val media = MediaMetadataRetriever()
            media.setDataSource(videoPath)
            val duration = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?: "-1").toInt()
            duration
        } catch (e: Exception) {
            0
        }
    }

    fun getVideoMimeType(videoPath: String): String {
        return try {
            val media = MediaMetadataRetriever()
            media.setDataSource(videoPath)
            val mimeType =
                media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
            mimeType
        } catch (e: Exception) {
            ""
        }
    }

    fun selectVideoTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if ((format.getString(MediaFormat.KEY_MIME) ?: "-1").startsWith("video/")) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no video track")
    }

    fun selectAudioTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if ((format.getString(MediaFormat.KEY_MIME) ?: "-1").startsWith("audio/")) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no audio track")
    }

    @SuppressLint("DefaultLocale")
    fun videoHasAudio(videoPath: String): Boolean {
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
        try {
            val hasAudio =
                media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) ?: ""
            Loggers.e("videoHasAudio = $hasAudio")
            if (hasAudio.toLowerCase() == "yes") return true
            return false

        } catch (e: Exception) {
            return false
        }
    }

    fun readTextFileFromRawResource(context: Context, resourceId: Int): String {

        val inputStream = context.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var nextLine = ""
        val body = StringBuilder()
        try {

            while (bufferedReader.readLine().also { nextLine = it } != null) {
                body.append(nextLine)
                body.append("\n")
            }

        } catch (e: Exception) {

        }
        return body.toString()
    }

    fun readTextColorFile(): ArrayList<String> {
        val textColorList = ArrayList<String>()
        val inputStream = FEMainApp.getContext().resources.openRawResource(R.raw.color_list2)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var nextLine = ""
        try {

            while (bufferedReader.readLine().also { nextLine = it } != null) {
                textColorList.add(nextLine.trim())
            }

        } catch (e: Exception) {

        }
        return textColorList
    }

    fun getLookupDataList(): ArrayList<Lookup> {
        var number: Int = 0
        val lockData = ArrayList<Lookup>()
        for (type in LookupType.values()) {
            if (number > 28) {
                FEMainApp.instance
                    .getPreference()?.run {
                        var itemSet: MutableSet<String> = getListKeyBy() ?: mutableSetOf()
                        lockData.add(Lookup(type, type.toString(), !itemSet.any {
                            it == type.toString()
                        }))
                    }
            } else {
                lockData.add(Lookup(type, type.toString()))
            }
            number++


        }
        return lockData
    }

    enum class LookupType {
        NONE,
        A1, A2, A3, A4, A5, A6, A7, A8, A9,
        B1, B2, B3, B4, B5, B6
    }

    fun screenWidth(context: Context): Int = context.resources.displayMetrics.widthPixels

    fun screenHeight(context: Context): Int = context.resources.displayMetrics.heightPixels

    fun density(context: Context): Float = context.resources.displayMetrics.density

    fun density(): Float = FEMainApp.getContext().resources.displayMetrics.density

    fun videoPreviewScale(): Float {
        val context = FEMainApp.getContext()
        val screenH = screenHeight(context)
        val screenW = screenWidth(context)
        val toolAreaHeightMin = 356 * density(context)

        return if ((screenH - toolAreaHeightMin) < screenW) {
            (screenH - toolAreaHeightMin) / screenW
        } else {
            1f
        }
    }


    fun videoScaleInTrim(): Float {
        val context = FEMainApp.getContext()
        val screenH = screenHeight(context)
        val screenW = screenWidth(context)
        val toolAreaHeightMin = 236 * density(context)

        return if ((screenH - toolAreaHeightMin) < screenW) {
            (screenH - toolAreaHeightMin) / screenW
        } else {
            1f
        }
    }

    val internalPath = FEMainApp.getContext().getExternalFilesDir(null)
    private val txtTempFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempText"
    private val videoTempFolder =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempvideo"
    private val tempRecordAudioFolder =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempRecordAudio"
    val themeFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/theme"
    val audioDefaultFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/audio"
    val defaultAudio = "$audioDefaultFolderPath/default_sound.mp3"
    private val musicTempDataFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/musicTempData"
    private val stickerTempFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/stickerTemp"
    val outputFolderPath =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/${
            FEMainApp.getContext().getString(R.string.app_name)
        }"//"$internalPath/DCIM/${VideoMakerApplication.getContext().getString(R.string.app_name)}"
    val myStuioFolderPath get() = outputFolderPath


    val tempImageFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempImage"

    fun saveBitmapToTempData(bitmap: Bitmap?): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
        if (!tempDataFolder.exists()) {
            tempDataFolder.mkdirs()
        }
        val outFile =
            File("$tempDataFolderPath/${System.currentTimeMillis()}${View.generateViewId()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun saveBitmapToTempData(bitmap: Bitmap?, outFileName: String): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
        tempDataFolder.mkdirs()
        val outFile = File("$tempDataFolderPath/$outFileName")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun deleteTempFolder() {
        Thread {
            val internalPath = FEMainApp.getContext().getExternalFilesDir(null)
            val tempDataFolderPath =
                "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempdata"
            val tempDataFolder = File(tempDataFolderPath)
            if (tempDataFolder.exists() && tempDataFolder.isDirectory) {
                for (file in tempDataFolder.listFiles()) {
                    file.delete()
                }
            }
        }.start()
    }

    fun getTempMp3OutPutFile(): String {
        File(musicTempDataFolderPath).mkdirs()
        return "$musicTempDataFolderPath/audio_${System.currentTimeMillis()}.mp4"
    }

    fun getTempAudioOutPutFile(fileType: String): String {
        File(musicTempDataFolderPath).mkdirs()
        return "$musicTempDataFolderPath/audio_${System.currentTimeMillis()}.$fileType"
    }

    fun saveStickerToTemp(bitmap: Bitmap): String {
        File(stickerTempFolderPath).mkdirs()

        val outFile = File("$stickerTempFolderPath/sticker_${System.currentTimeMillis()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun getTempVideoPath(): String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp4"
    }

    fun getTempM4aAudioPath(): String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp3"
    }


    fun getOutputVideoPath(): String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}.mp4"
    }

    fun getOutputVideoPath(size: Int): String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}-$size.mp4"
    }


    fun clearTemp() {
        val tempMusicFolder = File(musicTempDataFolderPath)
        try {
            if (tempMusicFolder.exists() && tempMusicFolder.isDirectory && tempMusicFolder.listFiles() != null) {
                for (file in tempMusicFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempVideoFolder = File(videoTempFolder)
        try {
            if (tempVideoFolder.exists() && tempVideoFolder.isDirectory && tempVideoFolder.listFiles() != null) {
                for (file in tempVideoFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempStickerFolder = File(stickerTempFolderPath)
        try {
            if (tempStickerFolder.exists() && tempStickerFolder.isDirectory && tempStickerFolder.listFiles() != null) {
                for (file in tempStickerFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempImageFolder = File(tempImageFolderPath)
        try {
            if (tempImageFolder.exists() && tempImageFolder.isDirectory && tempImageFolder.listFiles() != null) {
                for (file in tempImageFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

    }

    fun getTextTempOutFile(): String {
        File(txtTempFolderPath).mkdirs()
        return "$txtTempFolderPath/${System.currentTimeMillis()}.txt"
    }


    fun writeTextListFile(filePathList: ArrayList<String>): String {
        val txtOutFilePath = getTextTempOutFile()
        val outFile = File(txtOutFilePath)
        try {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(outFile)))
            for (path in filePathList) {
                writer.write("file '${path}'\n")
            }
            writer.close()
        } catch (e: java.lang.Exception) {

        }

        return outFile.absolutePath

    }

    fun getAudioRecordTempFilePath(): String {
        File(tempRecordAudioFolder).mkdirs()
        return "${tempRecordAudioFolder}/record_${System.currentTimeMillis()}.3gp"
    }

    fun copyFileTo(inPath: String, outPath: String) {
        val inputStream = FileInputStream(File(inPath))
        val outputStream = FileOutputStream(File(outPath))
        copyFile(inputStream, outputStream)
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    fun deleteFile(pathList: ArrayList<String>) {
        for (path in pathList) {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }


}