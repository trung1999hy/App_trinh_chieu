package com.thn.videoconstruction.fe_ui.pick_media

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FEItemTouchCallback
import com.thn.videoconstruction.adapter.FEPickPagerAdapter
import com.thn.videoconstruction.adapter.FEPickedMediaAdapterFE
import com.thn.videoconstruction.base.FEBaseActivity
import com.thn.videoconstruction.utils.TypeMedia
import com.thn.videoconstruction.utils.TypeVideoAction
import com.thn.videoconstruction.models.MediaPickedModel
import com.thn.videoconstruction.fe_ui.slide_show.SlideShowActivityFE
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.activity_pick_media.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import kotlin.math.roundToInt

class MediaPickActivityFE : FEBaseActivity(), KodeinAware {
    override fun getContentResId(): Int = R.layout.activity_pick_media
    private var mTypeMedia = TypeMedia.PHOTO

    private val mFeMediaPickViewModelFactory: FeMediaPickViewModelFactory by instance<FeMediaPickViewModelFactory>()
    private lateinit var mFEMediaPickViewModel: FE_MediaPickViewModel

    private var mTypeVideoAction = TypeVideoAction.SLIDE

    private val mMediaPickedAdapter = FEPickedMediaAdapterFE {
        performDeleteItemPicked(it)
        updateNumberImageSelected()
    }
    var flag = false

    companion object {
        const val TAKE_PICTURE = 1001
        const val RECORD_CAMERA = 1991
        const val COLS_IMAGE_LIST_SIZE = 90
        const val COLS_ALBUM_LIST_SIZE = 120
        const val CAMERA_PERMISSION_REQUEST = 1002

        const val ADD_MORE_PHOTO = 1003
        const val ADD_MORE_PHOTO_REQUEST_CODE = 1004

        const val ADD_MORE_VIDEO = 1005
        const val ADD_MORE_VIDEO_REQUEST_CODE = 1006

        fun gotoActivity(activity: Activity, typeMedia: TypeMedia) {
            val intent = Intent(activity, MediaPickActivityFE::class.java).apply {
                putExtra("MediaType", typeMedia.toString())
            }
            activity.startActivity(intent)
        }

        fun gotoActivity(activity: Activity, typeMedia: TypeMedia, themePath: String) {
            val intent = Intent(activity, MediaPickActivityFE::class.java).apply {
                putExtra("MediaType", typeMedia.toString())
                putExtra("themePath", themePath)
            }
            activity.startActivity(intent)
        }

    }


    private var mActionCode = -1
    private var mFlag = false
    override val kodein by closestKodein()

    private val mListPhotoPath = ArrayList<String>()
    private var startAvailable = true
    private var mThemeFileName = ""
    override fun initViews() {

        mFEMediaPickViewModel =
            ViewModelProvider(this, mFeMediaPickViewModelFactory).get(FE_MediaPickViewModel::class.java)
        listen()

        val action = intent.getIntExtra("action", -1)
        mActionCode = action
        Loggers.e("action = $action")
        if (action == ADD_MORE_PHOTO) {
            setScreenTitle(getString(R.string.photo))
            intent.getStringArrayListExtra("list-photo")?.let {
                for (path in it) {
                    mListPhotoPath.add(path)
                }
            }
            mTypeMedia = TypeMedia.PHOTO
        } else if (action == ADD_MORE_VIDEO) {
            setScreenTitle(getString(R.string.video))
            intent.getStringArrayListExtra("list-video")?.let {
                mListPhotoPath.addAll(it)
            }
        } else {
            when (mTypeMedia) {

                TypeMedia.PHOTO -> {
                    setScreenTitle(getString(R.string.photo))
                }
            }
        }
        mThemeFileName = intent.getStringExtra("themePath") ?: ""
        tabLayout.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = FEPickPagerAdapter(this, supportFragmentManager)

        val col = (Utils.screenWidth(this) / (96 * Utils.density(this))).roundToInt()
        mediaPickedListView.adapter = mMediaPickedAdapter
        mediaPickedListView.layoutManager = GridLayoutManager(this, col.toInt())
        addItemTouchCallback(mediaPickedListView)
        imagePickedArea.visibility = View.GONE
        mFEMediaPickViewModel.FELocalStorage.getAllMedia(mTypeMedia)

        for (path in mListPhotoPath) {
            mMediaPickedAdapter.addItem(MediaPickedModel(path))
            imagePickedArea.visibility = View.VISIBLE
            mediaPickedListView.scrollToPosition(mMediaPickedAdapter.itemCount - 1)
            updateNumberImageSelected()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    private fun addItemTouchCallback(recyclerView: RecyclerView) {
        val callback = FEItemTouchCallback(object : FEPickedMediaAdapterFE.ItemTouchListenner {
            override fun onItemMove(fromPosition: Int, toPosition: Int) {
                mMediaPickedAdapter.onMove(fromPosition, toPosition)
            }
        })
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        mMediaPickedAdapter.registerItemTouch(itemTouchHelper)
    }

    override fun initActions() {


        expandViewButton.setOnClickListener {
            if (isExpanded) collapseView()
            else expandView()
        }

        startButton.setClick {
            if (startAvailable) {
                startAvailable = false
                if (mMediaPickedAdapter.itemCount < 2) {
                    if (mTypeMedia == TypeMedia.PHOTO)
                        Toast.makeText(
                            this,
                            getString(R.string.select_at_least_2_image),
                            Toast.LENGTH_LONG
                        ).show()
                    else {
                        if (mTypeVideoAction == TypeVideoAction.SLIDE) {
                            if (mMediaPickedAdapter.itemCount > 0) {
                                val items = arrayListOf<String>()
                                for (item in mMediaPickedAdapter.itemList) {
                                    items.add(item.path)
                                }
                                if (mActionCode == ADD_MORE_VIDEO) {
                                    val intent = Intent().apply {
                                        putStringArrayListExtra("Video picked list", items)
                                    }
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                } else {

                                }

                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.select_at_least_1_video),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.select_at_least_2_videos),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } else {
                    val items = arrayListOf<String>()
                    for (item in mMediaPickedAdapter.itemList) {
                        items.add(item.path)
                    }
                    Loggers.e("items size = ${items.size}")
                    if (mTypeMedia == TypeMedia.PHOTO) {
                        if (mActionCode == ADD_MORE_PHOTO) {
                            val intent = Intent().apply {
                                putStringArrayListExtra(
                                    SlideShowActivityFE.imagePickedListKey,
                                    items
                                )
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            val intent = Intent(this, SlideShowActivityFE::class.java)
                            intent.putStringArrayListExtra(
                                SlideShowActivityFE.imagePickedListKey,
                                items
                            )
                            if (mThemeFileName.isNotEmpty()) intent.putExtra(
                                "themeFileName",
                                mThemeFileName
                            )
                            startActivity(intent)
                        }

                    } else {

                    }

                }

                object : CountDownTimer(1000, 3000) {
                    override fun onFinish() {
                        startAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
    }


    private var mMediaCapturePath = ""



    private fun listen() {
        mFEMediaPickViewModel.itemJustPicked.observe(this, Observer {
            mMediaPickedAdapter.addItem(MediaPickedModel(it.filePath))
            imagePickedArea.visibility = View.VISIBLE
            mediaPickedListView.scrollToPosition(mMediaPickedAdapter.itemCount - 1)
            updateNumberImageSelected()
        })
    }

    @SuppressLint("SetTextI18n")
    fun updateNumberImageSelected() {
        val firstText = getString(R.string.selected) + " ("
        val numberText = mMediaPickedAdapter.itemCount.toString()
        val endText =
            ") " + getString(R.string.photos)
        val spannable = SpannableString(firstText + numberText + endText)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.orangeA01)),
            firstText.length,
            firstText.length + numberText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        numberMediaPicked.text = spannable
    }

    private fun performDeleteItemPicked(position: Int) {
        mFEMediaPickViewModel.onDelete(mMediaPickedAdapter.itemList[position])
        mMediaPickedAdapter.itemList.removeAt(position)
        mMediaPickedAdapter.notifyDataSetChanged()

        if (mMediaPickedAdapter.itemCount < 1) {
            imagePickedArea.visibility = View.GONE
        }

    }

    private var isExpanded = false

    override fun onResume() {
        super.onResume()
        mFEMediaPickViewModel.FELocalStorage.getAllMedia(mTypeMedia)
        mMediaPickedAdapter.checkFile()
        if (mMediaPickedAdapter.itemCount <= 0) {
            imagePickedArea.visibility = View.GONE
        } else {
            updateNumberImageSelected()
        }

    }

    private fun expandView() {
        if (isExpanded) return
        expandViewButton.rotation = 180f
        val target = Utils.screenHeight(this) * 2 / 3 - 220 * Utils.density(this)
        imagePickedArea.layoutParams.height += target.toInt()
        imagePickedArea.requestLayout()
        isExpanded = true
    }

    private fun collapseView() {
        if (!isExpanded) return
        expandViewButton.rotation = 0f
        val target = Utils.screenHeight(this) * 2 / 3 - 220 * Utils.density(this)
        imagePickedArea.layoutParams.height -= target.toInt()
        imagePickedArea.requestLayout()
        isExpanded = false
    }

    private fun checkCameraPermission(): Boolean { //true if GRANTED
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onBackPressed() {
        if (mFEMediaPickViewModel.folderIsShowing) {
            mFEMediaPickViewModel.hideFolder()
        } else {
            super.onBackPressed()
        }
    }

}
