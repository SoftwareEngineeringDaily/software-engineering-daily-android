package com.koalatea.sedaily.feature.playbar

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.koalatea.sedaily.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_playback_controls.view.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PlaybarFragment : Fragment() {
    private var playbarViewModel: PlaybarViewModel? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null
    private val PROGRESS_UPDATE_INTERNAL: Long = 1000
    private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    private val mHandler = Handler()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mUpdateProgressTask = { updateProgress() }
    private var composeDispose: CompositeDisposable = CompositeDisposable()

    // @TODO: Change to binding
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false)

        initDisplay()

        return rootView
    }

    override fun onStart() {
        super.onStart()

        initListeners()
        if (rootView != null) {
            initDisplay()
        }
    }

    fun initDisplay() {
        playbarViewModel = ViewModelProviders
                .of(this)
                .get(PlaybarViewModel::class.java)

        rootView?.play_pause?.setOnClickListener {
            handlePlayClick()
        }

        rootView?.back15?.setOnClickListener {
            back15()
        }
        rootView?.skip15?.setOnClickListener {
            skip15()
        }

        rootView?.speed?.setOnClickListener {
            showSpeedDialog()
        }

        rootView?.seekBar1?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rootView?.startText?.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopSeekbarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val activity = this@PlaybarFragment.activity
                        ?: return // @TODO: can we use app context?
                MediaControllerCompat.getMediaController(activity)
                        .transportControls
                        .seekTo(seekBar.progress.toLong())
                scheduleSeekbarUpdate()
            }
        })

        setSpeedText()
        setUpSpeedSubscription()
        setUpMediaChangeSubscription()
        setupPlaybackStateSub()

        val metaData = PodcastSessionStateManager
                .getInstance().getMediaMetaData()
        if (metaData != null) {
            updateWithMeta(metaData)
        }
    }

    fun initListeners() {
        val currentPlayTime = PodcastSessionStateManager.getInstance().currentProgress
        mLastPlaybackState = PodcastSessionStateManager.getInstance().lastPlaybackState

        if (currentPlayTime > 0) {
            scheduleSeekbarUpdate()
        }
        rootView?.startText?.text = DateUtils.formatElapsedTime(currentPlayTime / 1000)
        setSpeedTextView()

        composeDispose.isDisposed.run {
            setUpSpeedSubscription()
            setUpMediaChangeSubscription()
            setupPlaybackStateSub()
        }
    }

    override fun onStop() {
        super.onStop()

        composeDispose.clear()

        stopSeekbarUpdate()
    }

    override fun onPause() {
        super.onPause()
        if (mLastPlaybackState == null) {
            return
        }

        var currentPosition = mLastPlaybackState!!.position
        if (mLastPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState!!.lastPositionUpdateTime
            currentPosition += (timeDelta.toInt() * mLastPlaybackState!!.playbackSpeed).toLong()
        }

        PodcastSessionStateManager.getInstance().currentProgress = currentPosition
        PodcastSessionStateManager.getInstance().lastPlaybackState = mLastPlaybackState
        stopSeekbarUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekbarUpdate()
    }

    private fun handlePlayClick() {
        val controller = MediaControllerCompat.getMediaController(this.activity as Activity)
                ?: return
        playbarViewModel?.playPause(controller)
    }

    /* Playback events */
    private fun updateWithMeta(metadata: MediaMetadataCompat?) {
        if (this.activity == null) {
            return
        }

        if (metadata == null) {
            return
        }

        updateDuration(metadata)

        rootView?.title?.text = metadata.description.title

        val postTile = metadata.description.title.toString()
        val controller = MediaControllerCompat.getMediaController(this.activity as Activity)

        val psm = PodcastSessionStateManager.getInstance()
        val activeTitle = psm.currentTitle
        if (controller != null && !postTile.isEmpty() && postTile != activeTitle) {
            psm.currentTitle = postTile
            val currentPlayPosition = psm.getProgressForEpisode(postTile)
            controller.transportControls.seekTo(currentPlayPosition)
            rootView?.startText?.text = DateUtils.formatElapsedTime(currentPlayPosition / 1000)
        }
    }

    private fun handlePlaybackStateChange(state: PlaybackStateCompat?) {
        if (this.activity == null) {
            return
        }

        if (state == null) {
            return
        }

        var enablePlay = false

        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> enablePlay = true
            PlaybackStateCompat.STATE_ERROR -> {
                // @TODO: Log error
            }
        }

        if (enablePlay) {
            rootView?.play_pause?.setImageDrawable(ContextCompat.getDrawable(this.activity as Activity, R.drawable.exo_controls_play))
        } else {
            rootView?.play_pause?.setImageDrawable(
                    ContextCompat.getDrawable(this.activity as Activity, R.drawable.exo_controls_pause)
            )
        }
    }

    /* Seek bar/Progress */
    private fun updateDuration(metadata: MediaMetadataCompat?) {
        if (metadata == null) {
            return
        }

        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        rootView?.seekBar1?.max = duration
        rootView?.endText?.text = DateUtils.formatElapsedTime((duration / 1000).toLong())
    }

    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()

        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate({ mHandler.post(mUpdateProgressTask) },
                    PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL,
                    TimeUnit.MILLISECONDS
            )
        }
    }

    private fun stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture!!.cancel(false)
        }
    }

    private fun updateProgress() {
        if (mLastPlaybackState == null) {
            return
        }

        // @TODO: Make reactive
        val currentPosition: Long? = playbarViewModel?.setListenedProgress(mLastPlaybackState!!) // @TODO: Null check
        rootView?.seekBar1?.progress = currentPosition?.toInt() as Int
    }

    /* Speed */
    private fun showSpeedDialog() {
        // @TODO: unwrap
        SpeedDialog().show(this.fragmentManager!!, "tag")
    }

    private fun setUpSpeedSubscription() {
        val speedSubscription = object : DisposableObserver<Int>() {
            override fun onError(e: Throwable) {}

            override fun onComplete() {}

            override fun onNext(integer: Int) {
                setSpeedText()
            }
        }

        composeDispose.addAll(speedSubscription)

        PodcastSessionStateManager
                .getInstance()
                .speedChanges
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(speedSubscription as DisposableObserver<Int>)
    }

    private fun setupPlaybackStateSub() {
        val playbackStateSub = object : DisposableObserver<PlaybackStateCompat>() {
            override fun onError(e: Throwable) {}

            override fun onComplete() {}

            override fun onNext(playbackState: PlaybackStateCompat) {
                handlePlaybackState(playbackState)
            }
        }

        composeDispose.addAll(playbackStateSub)

        PodcastSessionStateManager
                .getInstance()
                .playbackStateChanges
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(playbackStateSub as DisposableObserver<PlaybackStateCompat>)
    }

    private fun setUpMediaChangeSubscription() {
        val mediaItemSubscription = object : DisposableObserver<MediaMetadataCompat>() {
            override fun onNext(mediaMetadataCompat: MediaMetadataCompat) {
                updateDuration(mediaMetadataCompat)
                updateWithMeta(mediaMetadataCompat)
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        }

        composeDispose.addAll(mediaItemSubscription)

        PodcastSessionStateManager
                .getInstance()
                .metadataChanges
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mediaItemSubscription as DisposableObserver<MediaMetadataCompat>)
    }

    private fun setSpeedTextView(): Int {
        if (!isAdded) return 1
        val currentSpeed = PodcastSessionStateManager.getInstance().currentSpeed
        val speedArray = resources.getStringArray(R.array.speed_options)
        rootView?.speed?.text = speedArray[currentSpeed]
        return currentSpeed
    }

    private fun setSpeedText() {
        val currentSpeed = setSpeedTextView()
        Log.v("keithtest", currentSpeed.toString())
        if (this.activity == null) return
        // @TODO: Make reactive
        playbarViewModel?.sendSpeedChangeIntent(currentSpeed, this.activity as Activity)
    }

    fun handlePlaybackState(state: PlaybackStateCompat?) {
        if (state == null) return

        handlePlaybackStateChange(state)
        mLastPlaybackState = state

        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> scheduleSeekbarUpdate()
            PlaybackStateCompat.STATE_PAUSED -> stopSeekbarUpdate()
            PlaybackStateCompat.STATE_STOPPED -> stopSeekbarUpdate()
        }
    }

    fun back15() {
        playbarViewModel?.back15(this.activity as Activity)
    }

    fun skip15() {
        playbarViewModel?.skip15(this.activity as Activity)
    }
}