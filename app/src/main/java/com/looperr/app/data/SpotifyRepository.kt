package com.looperr.app.data

import android.content.Context
import com.looperr.app.auth.SpotifyAuth
import com.looperr.app.auth.TokenManager
import com.looperr.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val auth = SpotifyAuth(context)

    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager.accessToken
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val api: SpotifyApi = Retrofit.Builder()
        .baseUrl(Constants.SPOTIFY_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyApi::class.java)

    suspend fun getTrack(trackId: String): Result<Track> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            api.getTrack(trackId)
        }
    }

    suspend fun getPlaybackState(): Result<PlaybackState?> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            val response = api.getPlaybackState()
            if (response.code() == 204) null else response.body()
        }
    }

    suspend fun getDevices(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            api.getDevices().devices
        }
    }

    suspend fun playTrack(trackUri: String, positionMs: Long = 0): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            api.play(request = PlayRequest(uris = listOf(trackUri), positionMs = positionMs))
            Unit
        }
    }

    suspend fun pause(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            api.pause()
            Unit
        }
    }

    suspend fun seek(positionMs: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            ensureValidToken()
            api.seek(positionMs)
            Unit
        }
    }

    private suspend fun ensureValidToken() {
        if (tokenManager.isTokenExpired && tokenManager.refreshToken != null) {
            auth.refreshAccessToken()
        }
    }
}
