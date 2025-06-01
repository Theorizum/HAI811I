package com.example.projet.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LlmApi {
    private const val BASE_URL = "https://api.openai.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(OpenAIInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(OpenAIService::class.java)

    suspend fun generateText(prompt: String): String {
        val messages = listOf(
            Message("system", "Vous êtes un assistant pour enfants."),
            Message("user", prompt)
        )
        val request = CompletionRequest(messages = messages)
        val response = service.createCompletion(request)
        return response.choices.firstOrNull()?.message?.content
            ?: "Aucune réponse."
    }
}
