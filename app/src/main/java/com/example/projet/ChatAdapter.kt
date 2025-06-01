package com.example.projet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    
    private val messages = mutableListOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val messageContainer: View = itemView.findViewById(R.id.messageContainer)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutResId = if (viewType == VIEW_TYPE_USER) {
            R.layout.message_item_user
        } else {
            R.layout.message_item_ai
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ChatViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        
        holder.messageTextView.text = message.content
        holder.timestampTextView.text = dateFormat.format(Date(message.timestamp))
        
        // Add message type emoji prefix for AI messages
        if (!message.isFromUser) {
            val prefix = when (message.messageType) {
                "story" -> "📚 "
                "quiz" -> "🧩 "
                "help" -> "❓ "
                else -> "🤖 "
            }
            holder.messageTextView.text = prefix + message.content
        }
        
        // Animation fade-in
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }
    
    override fun getItemCount(): Int = messages.size
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
    
    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }
    
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }
} 