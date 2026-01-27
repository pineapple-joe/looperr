package com.looperr.app.data

import retrofit2.Response
import retrofit2.http.*

interface SpotifyApi {

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String): Track

    @GET("me/player")
    suspend fun getPlaybackState(): Response<PlaybackState?>

    @GET("me/player/devices")
    suspend fun getDevices(): DevicesResponse

    @PUT("me/player/play")
    suspend fun play(
        @Query("device_id") deviceId: String? = null,
        @Body request: PlayRequest? = null
    ): Response<Unit>

    @PUT("me/player/pause")
    suspend fun pause(): Response<Unit>

    @PUT("me/player/seek")
    suspend fun seek(@Query("position_ms") positionMs: Long): Response<Unit>

    @PUT("me/player")
    suspend fun transferPlayback(@Body body: Map<String, Any>): Response<Unit>
}
