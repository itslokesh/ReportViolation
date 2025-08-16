package com.example.reportviolation.data.remote.auth

import com.example.reportviolation.data.remote.ApiResponse
import com.example.reportviolation.data.remote.AuthApi
import com.example.reportviolation.data.remote.RefreshBody
import com.example.reportviolation.data.remote.Tokens
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

object TokenStore {
    @Volatile var accessToken: String? = null
    @Volatile var refreshToken: String? = null

    fun update(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }
}

class TokenAuthenticator(
    private val refreshApi: AuthApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val current = TokenStore.accessToken
            if (response.request.header("Authorization") == "Bearer $current") {
                val refresh = TokenStore.refreshToken ?: return null
                val result = refreshApi.refresh(RefreshBody(refresh)).execute()
                val body: ApiResponse<Tokens>? = result.body()
                val tokens = body?.data ?: return null
                TokenStore.update(tokens.accessToken, tokens.refreshToken)
            }
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${TokenStore.accessToken}")
                .build()
        }
    }
}


