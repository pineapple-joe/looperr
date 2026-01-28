package com.looperr.app.player

import com.looperr.app.data.SpotifyRepository
import com.looperr.app.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoopState(
    val isLooping: Boolean = false,
    val currentPositionMs: Long = 0,
    val startMs: Long = 0,
    val endMs: Long = 0
)

class LoopController(private val repository: SpotifyRepository) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var loopJob: Job? = null

    private val _state = MutableStateFlow(LoopState())
    val state: StateFlow<LoopState> = _state.asStateFlow()

    private var trackUri: String? = null

    fun setTrack(uri: String, durationMs: Long) {
        trackUri = uri
        _state.value = LoopState(
            isLooping = false,
            currentPositionMs = 0,
            startMs = 0,
            endMs = durationMs
        )
    }

    fun setLoopPoints(startMs: Long, endMs: Long) {
        _state.value = _state.value.copy(startMs = startMs, endMs = endMs)
    }

    fun startLoop() {
        if (trackUri == null) return

        _state.value = _state.value.copy(isLooping = true)

        loopJob?.cancel()
        loopJob = scope.launch {
            // Start playback at loop start position
            repository.playTrack(trackUri!!, _state.value.startMs)

            while (isActive && _state.value.isLooping) {
                delay(Constants.LOOP_POLL_INTERVAL_MS)

                val playbackResult = repository.getPlaybackState()
                playbackResult.onSuccess { playbackState ->
                    playbackState?.let { state ->
                        val currentPos = state.progressMs ?: 0
                        _state.value = _state.value.copy(currentPositionMs = currentPos)

                        // Check if we've reached the end of the loop
                        if (currentPos >= _state.value.endMs) {
                            repository.seek(_state.value.startMs)
                        }
                    }
                }
            }
        }
    }

    fun stopLoop() {
        _state.value = _state.value.copy(isLooping = false)
        loopJob?.cancel()
        loopJob = null
    }

    fun clearTrack() {
        stopLoop()
        trackUri = null
        _state.value = LoopState()
    }

    fun toggleLoop() {
        if (_state.value.isLooping) {
            stopLoop()
        } else {
            startLoop()
        }
    }

    fun seekTo(positionMs: Long) {
        scope.launch {
            repository.seek(positionMs)
            _state.value = _state.value.copy(currentPositionMs = positionMs)
        }
    }

    fun destroy() {
        stopLoop()
        scope.cancel()
    }
}
