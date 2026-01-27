package com.looperr.app.util

object Constants {
    // TODO: Replace with your Spotify Developer Dashboard credentials
    const val SPOTIFY_CLIENT_ID = "958c198ddb084b67ae314b6148a581f2"
    const val SPOTIFY_REDIRECT_URI = "looperr://callback"

    const val SPOTIFY_AUTH_URL = "https://accounts.spotify.com/authorize"
    const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"
    const val SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1/"

    val SPOTIFY_SCOPES = listOf(
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing"
    )

    const val LOOP_POLL_INTERVAL_MS = 300L
}
