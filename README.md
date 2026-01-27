# Looperr Android

A Spotify music player app that lets you create infinite loops of specific sections within songs.

## Features

- **Spotify Authentication** - Secure OAuth 2.0 login with PKCE flow
- **Track Sharing** - Share Spotify track links directly to the app
- **Custom Loop Range** - Interactive dual-handle range slider to set precise loop points
- **Background Playback** - Foreground service maintains loop playback when app is backgrounded
- **Secure Storage** - Encrypted storage of Spotify access/refresh tokens

## Tech Stack

- Kotlin with Coroutines
- Android SDK (minSdk 26, targetSdk 34)
- Retrofit + OkHttp for networking
- AndroidX Material Design
- Coil for image loading
- EncryptedSharedPreferences for secure token storage

## Setup

1. Clone the repository
2. Create a Spotify Developer application at [developer.spotify.com](https://developer.spotify.com/dashboard)
3. Add your credentials to `app/src/main/java/com/looperr/app/util/Constants.kt`:
   ```kotlin
   const val SPOTIFY_CLIENT_ID = "your_client_id"
   const val SPOTIFY_REDIRECT_URI = "looperr://callback"
   ```
4. Add the redirect URI to your Spotify app settings
5. Build and run the app

## Project Structure

```
app/src/main/java/com/looperr/app/
├── ui/
│   ├── MainActivity.kt          # Main UI activity
│   └── RangeSliderView.kt       # Custom range slider component
├── auth/
│   ├── SpotifyAuth.kt           # OAuth implementation
│   ├── TokenManager.kt          # Secure token storage
│   └── AuthCallbackActivity.kt  # OAuth callback handler
├── player/
│   ├── LoopService.kt           # Foreground service for playback
│   ├── LoopController.kt        # Loop logic and state management
│   └── LoopState.kt             # Loop state data class
├── data/
│   ├── SpotifyApi.kt            # Retrofit interface
│   ├── SpotifyRepository.kt     # Data layer
│   └── SpotifyModels.kt         # Data classes
└── util/
    ├── Constants.kt             # Config values
    └── TimeUtils.kt             # Time formatting
```

## Usage

1. Open the app and log in with your Spotify account
2. Share a Spotify track link to Looperr (or paste it in the app)
3. Use the range slider to select your loop section
4. The app will continuously play the selected section

## Requirements

- Android 8.0 (API 26) or higher
- Spotify Premium account (required for playback control)
- Active Spotify session on the device

## License

MIT
