package com.example.projet.network

import android.util.Log
import com.example.projet.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class OpenAIInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .build()
        Log.d("OpenAI", "API key = '${BuildConfig.OPENAI_API_KEY}'")

        return chain.proceed(request)
    }
}
