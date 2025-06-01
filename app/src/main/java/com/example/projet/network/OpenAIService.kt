package com.example.projet.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class Message(val role: String, val content: String)
data class CompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)
data class Choice(val message: Message)
data class CompletionResponse(val choices: List<Choice>)

interface OpenAIService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun createCompletion(
        @Body request: CompletionRequest
    ): CompletionResponse
}
