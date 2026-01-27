package com.looperr.app.util

object TimeUtils {

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun parseSpotifyUrl(url: String): String? {
        // Handle various Spotify URL formats:
        // https://open.spotify.com/track/TRACK_ID
        // https://open.spotify.com/track/TRACK_ID?si=...
        // spotify:track:TRACK_ID

        val trackIdRegex = Regex("""(?:spotify:track:|open\.spotify\.com/track/)([a-zA-Z0-9]+)""")
        return trackIdRegex.find(url)?.groupValues?.get(1)
    }
}
