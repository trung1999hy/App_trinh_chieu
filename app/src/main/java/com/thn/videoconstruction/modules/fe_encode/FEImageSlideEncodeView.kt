package com.thn.videoconstruction.modules.fe_encode

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES20
import android.view.Surface
import com.thn.videoconstruction.models.StickerDrawer
import com.thn.videoconstruction.models.StickerForRender
import com.thn.videoconstruction.ffmpeg_fe.FFmpegFE
import com.thn.videoconstruction.ffmpeg_fe.FFmpegCmdFE
import com.thn.videoconstruction.view_customers.model_image_view.FESlideData
import com.thn.videoconstruction.view_customers.model_image_view.FEDataContainer
import com.thn.videoconstruction.view_customers.model_image_view.FEDrawer
import com.thn.videoconstruction.view_customers.model_image_view.FEThemeDrawer
import com.thn.videoconstruction.fe_slide_show.fe_slide_gl.FEDrawerStickerView
import com.thn.videoconstruction.models.Theme
import com.thn.videoconstruction.utils.*
import java.io.File

class FEImageSlideEncodeView(
    private val imageListDataList: ArrayList<FESlideData>,
    private val stickerAddedList: ArrayList<StickerForRender>,
    private val theme: Theme,
    private val delayTime: Int,
    private val musicPath: String,
    private val musicVolume: Float,
    private val videoQuality: Int,
    private val FETransition: com.thn.videoconstruction.transition.FETransition
) {

    private var mWidth = videoQuality
    private var mHeight = videoQuality
    private var mBitRare = 2500000

    var totalTimeForGetFrame = 0L

    private val MIME_TYPE = "video/avc"
    private val FRAME_RATE = 24
    private val IFRAME_INTERVAl = 1
    private val NUMBER_FRAMES = FRAME_RATE * 60


    private var mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
    private lateinit var mInputSurface: CodecInputSurface
    private lateinit var mMuxer: MediaMuxer

    private var mTrackIndex = 0
    private var mMuxerStarted = false

    private val mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    private var outputFilePath = ""
    private val mStickerDrawerList = ArrayList<StickerDrawer>()
    private val mFEDataContainer = FEDataContainer()
    private val mFEDrawer = FEDrawer()
    private var mFEThemeDrawer: FEThemeDrawer?=null
    private var mIsUseTheme = true
    private val totalVideoTime = (mFEDataContainer.transitionTimeMs + delayTime) * imageListDataList.size

    fun performEncodeVideo(onUpdateProgress: (Float) -> Unit, onComplete: (String) -> Unit) {
        mFEDataContainer.prepareForRender(imageListDataList, delayTime)
        if (mWidth == 1080) {
            mBitRare = 10000000
        } else if (mWidth == 720) {
            mBitRare = 5000000
        }
        try {
            val start = System.currentTimeMillis()
            prepareEncode()
            mInputSurface.makeCurrent()
            mFEDrawer.prepare(FETransition)
            if (theme.themeVideoFilePath != "none") {
                mIsUseTheme = true
                mFEThemeDrawer = FEThemeDrawer(theme)
                mFEThemeDrawer?.prepare(theme)
            } else {
                mIsUseTheme = false
            }

            for (item in stickerAddedList) {
                val drawer = FEDrawerStickerView().apply {
                    prepare(Utils.getStickerFromFilePath(item.stickerPath))
                }
                val stickerDrawer = StickerDrawer(item.startOffset, item.endOffset, drawer)
                mStickerDrawerList.add(stickerDrawer)
            }

            GLES20.glClearColor(0f, 0f, 0f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)



            if(mIsUseTheme) {
                Thread.sleep(1000)
                for (time in 0 until totalVideoTime step 40) {
                    drainEncoder(false)
                    mFEDrawer.changeFrameData(mFEDataContainer.getFrameByTimeForRender(time), true)
                    mFEDrawer.drawFrame()
                    if(mIsUseTheme) mFEThemeDrawer?.drawFrame()
                    for(item in mStickerDrawerList) {
                        if(time >= item.startOffset && time <= item.endOffset) item.drawerStickerView.drawFrame()
                    }
                    mInputSurface.setPresentationTime(time * 1000000L)
                    mInputSurface.swapBuffer()
                    onUpdateProgress.invoke(time.toFloat()*0.9f/totalVideoTime)
                }
            } else {

                if(mStickerDrawerList.size == 0) {
                    val delayTime = mFEDataContainer.delayTimeMs
                    val transitionTime = mFEDataContainer.transitionTimeMs
                    var currentTimeMs = 0
                    for(index in 0 until  imageListDataList.size) {
                        drainEncoder(false)

                        mFEDrawer.changeFrameData(mFEDataContainer.getFrameByTimeForRender(currentTimeMs), true)
                        mFEDrawer.drawFrame()
                        mInputSurface.setPresentationTime(currentTimeMs * 1000000L)
                        mInputSurface.swapBuffer()
                        onUpdateProgress.invoke(currentTimeMs.toFloat()*0.9f/totalVideoTime)
                        currentTimeMs+=delayTime



                        for(time in currentTimeMs until  currentTimeMs+transitionTime step 40) {
                            drainEncoder(false)
                            mFEDrawer.changeFrameData(mFEDataContainer.getFrameByTimeForRender(time), true)
                            mFEDrawer.drawFrame()
                            mInputSurface.setPresentationTime(time * 1000000L)
                            mInputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat()*0.9f/totalVideoTime)
                        }
                        currentTimeMs+=transitionTime
                    }
                } else {
                    val delayTime = mFEDataContainer.delayTimeMs
                    val transitionTime = mFEDataContainer.transitionTimeMs
                    var currentTimeMs = 0
                    for(index in 0 until  imageListDataList.size) {
                        Loggers.e("current time ms = $currentTimeMs")
                        for(time in currentTimeMs until currentTimeMs+delayTime step 1000) {
                            drainEncoder(false)
                            val slideData = mFEDataContainer.getFrameByTimeForRender(currentTimeMs)
                            mFEDrawer.changeFrameData(slideData, true)
                            mFEDrawer.drawFrame()
                            for(stickerDrawer in mStickerDrawerList) {
                                if(time >= stickerDrawer.startOffset && time <= stickerDrawer.endOffset) stickerDrawer.drawerStickerView.drawFrame()
                            }
                            mInputSurface.setPresentationTime(time * 1000000L)
                            mInputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat()*0.9f/totalVideoTime)
                        }
                        currentTimeMs+=delayTime
                        for(time in currentTimeMs until  currentTimeMs+transitionTime step 40) {
                            drainEncoder(false)
                            mFEDrawer.changeFrameData(mFEDataContainer.getFrameByTimeForRender(time), true)
                            mFEDrawer.drawFrame()
                            for(stickerDrawer in mStickerDrawerList) {
                                if(time >= stickerDrawer.startOffset && time <= stickerDrawer.endOffset) stickerDrawer.drawerStickerView.drawFrame()
                            }
                            mInputSurface.setPresentationTime(time * 1000000L)
                            mInputSurface.swapBuffer()
                            onUpdateProgress.invoke(time.toFloat()*0.9f/totalVideoTime)
                        }
                        currentTimeMs+=transitionTime
                    }
                }
            }




            drainEncoder(true)
            releaseEncoder()


            addMusic(onComplete,onUpdateProgress)

        } catch (e: Exception) {

        } finally {
        }

    }

    private var finalFilePath = ""
    private fun addMusic(onComplete: (String) -> Unit, onUpdateProgress: ((Float) -> Unit)?=null) {
        Loggers.e("music path = $musicPath")
        val mime = Utils.getVideoMimeType(musicPath)
        Loggers.e("mime type = $mime")

        if(musicPath.length < 5) {
            finalFilePath = Utils.getOutputVideoPath(mWidth)
            File(outputFilePath).renameTo(File(finalFilePath))
            onComplete.invoke(finalFilePath)
            return
        }

        if(musicPath.toLowerCase().contains(".m4a")) {
            finalFilePath = Utils.getOutputVideoPath(mWidth)
            val tempAudioPath = Utils.getTempM4aAudioPath()
            FFmpegFE(FFmpegCmdFE.trimAudio2(musicPath,0,totalVideoTime.toLong(), tempAudioPath)).apply {
                runCmd {
                    FFmpegFE(FFmpegCmdFE.mergeAudioToVideo(tempAudioPath, outputFilePath, finalFilePath,musicVolume )).apply {
                        runCmd({
                            onUpdateProgress?.invoke(0.9f+(it.toFloat()*0.1f/totalVideoTime))
                        },{
                            onComplete.invoke(finalFilePath)
                        })
                    }
                }
            }
        } else {
            finalFilePath = Utils.getOutputVideoPath(mWidth)

            if (musicPath.isNotEmpty()) {
                FFmpegFE(FFmpegCmdFE.mergeAudioToVideo(musicPath, outputFilePath, finalFilePath,musicVolume )).apply {
                    runCmd({
                        onUpdateProgress?.invoke(0.9f+(it.toFloat()*0.1f/totalVideoTime))
                    },{
                        onComplete.invoke(finalFilePath)
                    })
                }
            }
        }


    }

    fun releaseEncoder() {

        if (mEncoder != null) {
            mEncoder.stop()
            mEncoder.release()

        }
        if (mInputSurface != null) {
            mInputSurface.release()
        }
        if (mMuxer != null) {
            mMuxer.stop()
            mMuxer.release()
        }
    }

    private fun prepareEncode() {
        val mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_BIT_RATE, mBitRare)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAl)
        }
        mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = CodecInputSurface(mEncoder.createInputSurface())
        mEncoder.start()
        outputFilePath = Utils.getTempVideoPath()
        try {
            mMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: java.lang.Exception) {

        }
        mTrackIndex = -1
        mMuxerStarted = false
    }

    private fun drainEncoder(enOfStream: Boolean) {
        val timeOutUSec = 10000L
        if (enOfStream) {
            mEncoder.signalEndOfInputStream()
        }

        var encoderOutputBuffers = mEncoder.outputBuffers
        while (true) {
            val encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, timeOutUSec)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

                if (!enOfStream) {
                    break
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                encoderOutputBuffers = mEncoder.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if (mMuxerStarted) {
                    throw RuntimeException("format changed twice")
                }
                val mediaFormat = mEncoder.outputFormat
                mTrackIndex = mMuxer.addTrack(mediaFormat)
                mMuxer.start()
                mMuxerStarted = true
            } else if (encoderStatus < 0) {
                Loggers.e("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
            } else {

                val encodedData = encoderOutputBuffers[encoderStatus]

                if (encodedData == null) {
                    Loggers.e("encoderOutputBuffer $encoderStatus was null")
                    throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                }

                if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0

                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }

                    encodedData.position(mBufferInfo.offset)
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size)

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo)
                }
                mEncoder.releaseOutputBuffer(encoderStatus, false)
                if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!enOfStream) {
                        Loggers.e("reached end of stream unexpectedly")
                    } else {
                        Loggers.e("end of stream reached")
                    }
                    break
                }
            }
        }
    }


    private class CodecInputSurface(val mSurface: Surface) {
        private val EGL_RECORDABLE_ANDROID = 0x3142

        private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
        private var mEGLContext = EGL14.EGL_NO_CONTEXT
        private var mEGLSurface = EGL14.EGL_NO_SURFACE

        init {
            eglSetup()
        }

        fun eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 display")
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw RuntimeException("unable to initialize EGL14");
            }

            val attrList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
            )

            val configs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(mEGLDisplay, attrList, 0, configs, 0, configs.size, numConfigs, 0)
            val attr_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            mEGLContext =
                EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attr_list, 0)

            val surfaceAttrs = intArrayOf(EGL14.EGL_NONE)
            mEGLSurface =
                EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface, surfaceAttrs, 0)
        }

        fun makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        }

        fun swapBuffer(): Boolean {
            return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
        }

        fun setPresentationTime(nSecs: Long) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nSecs)
        }

        fun release() {
            if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(mEGLDisplay)
            }
            mSurface.release()
            mEGLDisplay = EGL14.EGL_NO_DISPLAY
            mEGLContext = EGL14.EGL_NO_CONTEXT
            mEGLSurface = EGL14.EGL_NO_SURFACE

        }

    }

}