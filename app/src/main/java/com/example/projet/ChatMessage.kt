package com.example.projet

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: String = "text" // text, story, quiz, help
) 