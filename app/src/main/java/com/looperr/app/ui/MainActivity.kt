package com.looperr.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.looperr.app.auth.SpotifyAuth
import com.looperr.app.auth.TokenManager
import com.looperr.app.data.SpotifyRepository
import com.looperr.app.data.Track
import com.looperr.app.databinding.ActivityMainBinding
import com.looperr.app.player.LoopService
import com.looperr.app.player.LoopState
import com.looperr.app.util.TimeUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var auth: SpotifyAuth
    private lateinit var repository: SpotifyRepository

    private var loopService: LoopService? = null
    private var serviceBound = false

    private var currentTrack: Track? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LoopService.LoopBinder
            loopService = binder.getService()
            serviceBound = true
            observeLoopState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            loopService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        auth = SpotifyAuth(this)
        repository = SpotifyRepository(this)

        setupUI()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        updateUIState()

        // Bind to loop service
        Intent(this, LoopService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            auth.startAuthFlow()
        }

        binding.loopButton.setOnClickListener {
            loopService?.loopController?.toggleLoop()
        }

        binding.rangeSlider.listener = object : RangeSliderView.OnRangeChangeListener {
            override fun onRangeChanged(startPercent: Float, endPercent: Float) {
                currentTrack?.let { track ->
                    val startMs = (track.durationMs * startPercent).toLong()
                    val endMs = (track.durationMs * endPercent).toLong()
                    binding.startTime.text = TimeUtils.formatTime(startMs)
                    binding.endTime.text = TimeUtils.formatTime(endMs)
                }
            }

            override fun onRangeChangeFinished(startPercent: Float, endPercent: Float) {
                currentTrack?.let { track ->
                    val startMs = (track.durationMs * startPercent).toLong()
                    val endMs = (track.durationMs * endPercent).toLong()
                    loopService?.loopController?.setLoopPoints(startMs, endMs)
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            val trackId = TimeUtils.parseSpotifyUrl(sharedText) ?: return

            if (tokenManager.isLoggedIn) {
                loadTrack(trackId)
            }
        }
    }

    private fun updateUIState() {
        when {
            !tokenManager.isLoggedIn -> showLoginState()
            currentTrack == null -> showWaitingState()
            else -> showPlayerState()
        }
    }

    private fun showLoginState() {
        binding.loginContainer.visibility = View.VISIBLE
        binding.waitingContainer.visibility = View.GONE
        binding.playerContainer.visibility = View.GONE
    }

    private fun showWaitingState() {
        binding.loginContainer.visibility = View.GONE
        binding.waitingContainer.visibility = View.VISIBLE
        binding.playerContainer.visibility = View.GONE
    }

    private fun showPlayerState() {
        binding.loginContainer.visibility = View.GONE
        binding.waitingContainer.visibility = View.GONE
        binding.playerContainer.visibility = View.VISIBLE
    }

    private fun loadTrack(trackId: String) {
        lifecycleScope.launch {
            repository.getTrack(trackId).onSuccess { track ->
                currentTrack = track
                displayTrack(track)

                // Start foreground service
                val serviceIntent = Intent(this@MainActivity, LoopService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                // Set track in loop controller
                loopService?.loopController?.setTrack(
                    uri = "spotify:track:${track.id}",
                    durationMs = track.durationMs
                )

                updateUIState()
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    private fun displayTrack(track: Track) {
        binding.trackName.text = track.name
        binding.artistName.text = track.artistNames

        track.album.imageUrl?.let { url ->
            binding.albumArt.load(url) {
                transformations(RoundedCornersTransformation(16f))
            }
        }

        binding.startTime.text = TimeUtils.formatTime(0)
        binding.endTime.text = TimeUtils.formatTime(track.durationMs)
        binding.currentTime.text = TimeUtils.formatTime(0)

        binding.rangeSlider.setRange(0f, 1f)
    }

    private fun observeLoopState() {
        lifecycleScope.launch {
            loopService?.loopController?.state?.collectLatest { state ->
                updateLoopUI(state)
            }
        }
    }

    private fun updateLoopUI(state: LoopState) {
        currentTrack?.let { track ->
            // Update button
            binding.loopButton.text = if (state.isLooping) "Stop Loop" else "Start Loop"

            // Update current position
            binding.currentTime.text = TimeUtils.formatTime(state.currentPositionMs)

            // Update slider position indicator
            val positionPercent = state.currentPositionMs.toFloat() / track.durationMs
            binding.rangeSlider.setCurrentPosition(positionPercent)
        }
    }
}
