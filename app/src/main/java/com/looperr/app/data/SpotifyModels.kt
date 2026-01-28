package com.looperr.app.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val id: String,
    val name: String,
    @SerializedName("duration_ms") val durationMs: Long,
    val artists: List<Artist>,
    val album: Album
) : Parcelable {
    val artistNames: String
        get() = artists.joinToString(", ") { it.name }
}

@Parcelize
data class Artist(
    val id: String,
    val name: String
) : Parcelable

@Parcelize
data class Album(
    val id: String,
    val name: String,
    val images: List<AlbumImage>
) : Parcelable {
    val imageUrl: String?
        get() = images.firstOrNull()?.url
}

@Parcelize
data class AlbumImage(
    val url: String,
    val height: Int?,
    val width: Int?
) : Parcelable

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
