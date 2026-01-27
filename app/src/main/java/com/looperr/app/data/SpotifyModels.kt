package com.looperr.app.data

import com.google.gson.annotations.SerializedName

data class Track(
    val id: String,
    val name: String,
    @SerializedName("duration_ms") val durationMs: Long,
    val artists: List<Artist>,
    val album: Album
) {
    val artistNames: String
        get() = artists.joinToString(", ") { it.name }
}

data class Artist(
    val id: String,
    val name: String
)

data class Album(
    val id: String,
    val name: String,
    val images: List<AlbumImage>
) {
    val imageUrl: String?
        get() = images.firstOrNull()?.url
}

data class AlbumImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

data class PlaybackState(
    @SerializedName("is_playing") val isPlaying: Boolean,
    @SerializedName("progress_ms") val progressMs: Long?,
    val item: Track?,
    val device: Device?
)

data class Device(
    val id: String,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    val type: String
)

data class DevicesResponse(
    val devices: List<Device>
)

data class PlayRequest(
    val uris: List<String>? = null,
    @SerializedName("position_ms") val positionMs: Long? = null
)
