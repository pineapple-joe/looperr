package com.looperr.app.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.looperr.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class SpotifyAuth(private val context: Context) {

    private val client = OkHttpClient()
    private val tokenManager = TokenManager(context)

    private var codeVerifier: String? = null

    fun startAuthFlow() {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)

        val authUrl = Uri.parse(Constants.SPOTIFY_AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", Constants.SPOTIFY_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", Constants.SPOTIFY_REDIRECT_URI)
            .appendQueryParameter("scope", Constants.SPOTIFY_SCOPES.joinToString(" "))
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .build()

        // Save code verifier for later
        context.getSharedPreferences("auth_temp", Context.MODE_PRIVATE)
            .edit()
            .putString("code_verifier", codeVerifier)
            .apply()

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, authUrl)
    }

    suspend fun handleCallback(code: String): Boolean = withContext(Dispatchers.IO) {
        val savedVerifier = context.getSharedPreferences("auth_temp", Context.MODE_PRIVATE)
            .getString("code_verifier", null) ?: return@withContext false

        try {
            val formBody = FormBody.Builder()
                .add("client_id", Constants.SPOTIFY_CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", Constants.SPOTIFY_REDIRECT_URI)
                .add("code_verifier", savedVerifier)
                .build()

            val request = Request.Builder()
                .url(Constants.SPOTIFY_TOKEN_URL)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false

            val json = JSONObject(response.body?.string() ?: return@withContext false)
            tokenManager.saveTokens(
                accessToken = json.getString("access_token"),
                refreshToken = json.optString("refresh_token", null),
                expiresIn = json.getInt("expires_in")
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun refreshAccessToken(): Boolean = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.refreshToken ?: return@withContext false

        try {
            val formBody = FormBody.Builder()
                .add("client_id", Constants.SPOTIFY_CLIENT_ID)
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build()

            val request = Request.Builder()
                .url(Constants.SPOTIFY_TOKEN_URL)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false

            val json = JSONObject(response.body?.string() ?: return@withContext false)
            tokenManager.saveTokens(
                accessToken = json.getString("access_token"),
                refreshToken = json.optString("refresh_token", tokenManager.refreshToken),
                expiresIn = json.getInt("expires_in")
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
