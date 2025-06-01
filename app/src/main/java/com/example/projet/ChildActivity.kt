package com.example.projet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projet.network.LlmApi
import kotlinx.coroutines.launch
import java.util.Locale

data class QuizData(
    val question: String,
    val choices: Map<String, String>,
    val correct_choice: String,
    val explanation: String
)


class ChildActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var storyButton: Button
    private lateinit var quizButton: Button
    private lateinit var helpButton: Button
    private lateinit var backButton: Button
    
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var databaseHelper: DatabaseHelper
    
    private var childAge: Int = 8 // Default age
    private var difficultyLevel: Int = 1 // Default difficulty (1-3)
    private var childName: String = "Mon enfant"
    // Quand quiz en attente, on stocke la question et le type
    private var isQuizPending = false
    private var pendingCorrectChoice: String? = null
    private var pendingExplanation: String? = null
    private var pendingQuizPrompt: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)
        
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadChildSettings()
        
        // Welcome message
        addWelcomeMessage()
    }
    
    private fun initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        storyButton = findViewById(R.id.storyButton)
        quizButton = findViewById(R.id.quizButton)
        helpButton = findViewById(R.id.helpButton)
        backButton = findViewById(R.id.backButton)
        
        databaseHelper = DatabaseHelper(this)
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChildActivity)
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageEditText.text.clear()
            }
        }
        
        storyButton.setOnClickListener {
            generateStory()
        }
        
        quizButton.setOnClickListener {
            generateQuiz()
        }
        
        helpButton.setOnClickListener {
            showHelp()
        }
        
        backButton.setOnClickListener {
            finish() // Return to previous activity
        }
    }
    
    private fun loadChildSettings() {
        // Load from SessionManager
        childAge = SessionManager.getChildAge(this)
        difficultyLevel = SessionManager.getDifficultyLevel(this)
        childName = SessionManager.getChildName(this)
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = "Salut $childName ! 🎉\nJe suis ton assistant intelligent 🤖\nComment puis-je t'aider aujourd'hui ?"
        chatAdapter.addMessage(ChatMessage(welcomeMessage, false, System.currentTimeMillis(), "welcome"))
        scrollToBottom()
    }

    private fun sendMessage(userMessage: String) {
        // Afficher le message de l’enfant dans le chat
        chatAdapter.addMessage(ChatMessage(userMessage, true, System.currentTimeMillis()))
        scrollToBottom()

        if (isQuizPending) {
            isQuizPending = false
            val childAnswer = userMessage.trim().uppercase(Locale.getDefault())
            val correctLetter = pendingCorrectChoice?.uppercase(Locale.getDefault())

            val feedback: String = if (childAnswer == correctLetter) {
                "C'est correct 🎉\nExplication : ${pendingExplanation.orEmpty()}"
            } else {
                "Ce n'est pas la bonne réponse. La bonne réponse est $correctLetter.\nExplication : ${pendingExplanation.orEmpty()}"
            }

            chatAdapter.addMessage(
                ChatMessage(feedback, false, System.currentTimeMillis(), "quiz_correction")
            )
            scrollToBottom()

            // Enregistrer la réponse de l’enfant, le feedback et le score
            val score = if (childAnswer == correctLetter) 10 else 0
            databaseHelper.addChildMessage(userMessage, feedback, "quiz_correction")
            databaseHelper.addChildScore("quiz", score, difficultyLevel, childAge)
            return
        }


        // Cas normal (pas de quiz en attente) : on envoie directement au LLM
        lifecycleScope.launch {
            try {
                val response = sendPromptToLLM(userMessage, difficultyLevel)

                // Afficher la réponse de l’IA dans le chat
                chatAdapter.addMessage(ChatMessage(response, false, System.currentTimeMillis()))
                scrollToBottom()

                databaseHelper.addChildMessage(userMessage, response)

            } catch (e: Exception) {
                val errorMessage = "Désolé, je n'ai pas pu répondre. Essaie encore ! 😊"
                chatAdapter.addMessage(ChatMessage(errorMessage, false, System.currentTimeMillis()))
                scrollToBottom()
            }
        }
    }

    
    private fun generateStory() {
        val storyPrompt = createStoryPrompt()
        chatAdapter.addMessage(ChatMessage("📚 Raconte-moi une histoire !", true, System.currentTimeMillis()))
        sendLLMRequest(storyPrompt, "story")
    }

    private fun generateQuiz() {
        askQuizSubjectAndGenerate()
    }

    
    private fun showHelp() {
        val helpPrompt = createHelpPrompt()
        chatAdapter.addMessage(ChatMessage("❓ J'ai besoin d'aide !", true, System.currentTimeMillis()))
        sendLLMRequest(helpPrompt, "help")
    }

    private fun askQuizSubjectAndGenerate() {
        val subjects = listOf("Mathématiques", "Français", "Sciences", "Culture générale", "Nature", "Histoire")
        val subjectsArray = subjects.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Choisis un thème pour ton quiz 🧠")
            .setItems(subjectsArray) { _, which ->
                val selectedSubject = subjectsArray[which]
                generateQuizWithSubject(selectedSubject)
            }
            .show()
    }

    private fun generateQuizWithSubject(subject: String) {
        chatAdapter.addMessage(
            ChatMessage("🧩 Je veux un quiz sur $subject !", true, System.currentTimeMillis())
        )
        scrollToBottom()

        val prompt = """
        Tu es un assistant éducatif pour enfants. Génère un quiz sur le thème "$subject" adapté à un enfant de $childAge ans. 
        Format de sortie EXACT (JSON) :
        {
          "question": "<QUESTION>",
          "choices": {
            "A": "<CHOIX_A>",
            "B": "<CHOIX_B>",
            "C": "<CHOIX_C>"
          },
          "correct_choice": "<LETTRE_CORRECTE>",
          "explanation": "<EXPLICATION_BRÈVE>"
        }
        Ne réponds pas autrement. N’inclus aucun texte libre hors de ce format JSON.
        Utilise un langage simple, ajoute un emoji dans le texte si tu veux.
        Difficulté : niveau $difficultyLevel sur 3.
    """.trimIndent()

        lifecycleScope.launch {
            try {
                val jsonResponse = sendPromptToLLM(prompt, difficultyLevel)
                val quizData = com.google.gson.Gson().fromJson(jsonResponse, QuizData::class.java)

                val questionText = buildString {
                    append("🧩 Question : ${quizData.question}\n")
                    append("A) ${quizData.choices["A"]}\n")
                    append("B) ${quizData.choices["B"]}\n")
                    append("C) ${quizData.choices["C"]}")
                }
                chatAdapter.addMessage(
                    ChatMessage(questionText, false, System.currentTimeMillis(), "quiz_question")
                )
                scrollToBottom()

                pendingQuizPrompt = jsonResponse
                isQuizPending = true
                pendingCorrectChoice = quizData.correct_choice
                pendingExplanation = quizData.explanation

                databaseHelper.addChildMessage(prompt, jsonResponse, "quiz_json")

            } catch (e: Exception) {
                chatAdapter.addMessage(
                    ChatMessage("Désolé, impossible de générer le quiz. Essaie plus tard.", false, System.currentTimeMillis(), "quiz_error")
                )
                scrollToBottom()
            }
        }
    }

    private fun sendLLMRequest(prompt: String, gameType: String) {
        lifecycleScope.launch {
            try {
                val response = sendPromptToLLM(prompt, difficultyLevel)

                // Si c'est un quiz qui vient d'être généré, on passe en "pending"
                if (gameType == "quiz_pending") {
                    isQuizPending = true
                    pendingQuizPrompt = prompt
                    // L’IA répondra “J’attends ta réponse…”
                    chatAdapter.addMessage(ChatMessage(response, false, System.currentTimeMillis(), "quiz_pending"))
                } else {
                    // Pour les autres (story, help, quiz_correction, etc.)
                    chatAdapter.addMessage(ChatMessage(response, false, System.currentTimeMillis(), gameType))
                }
                scrollToBottom()

                // Enregistrer le prompt et la réponse dans la base
                databaseHelper.addChildMessage(prompt, response, gameType)

            } catch (e: Exception) {
                Toast.makeText(this@ChildActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    
    private suspend fun sendPromptToLLM(prompt: String, difficulty: Int): String {
        val difficultyText = when (difficulty) {
            1 -> "très facile, pour un enfant de $childAge ans"
            2 -> "moyen, pour un enfant de $childAge ans"
            3 -> "un peu difficile, pour un enfant de $childAge ans"
            else -> "adapté à un enfant de $childAge ans"
        }
        
        val enhancedPrompt = """
            Tu es un assistant éducatif pour enfants. Réponds de façon $difficultyText.
            Utilise un langage simple, bienveillant et encourageant.
            Ajoute des emojis pour rendre ta réponse plus amusante.
            L'enfant s'appelle $childName.
            
            Question de l'enfant : $prompt
        """.trimIndent()
        
        return LlmApi.generateText(enhancedPrompt)
    }
    
    private fun createStoryPrompt(): String {
        val themes = listOf("animaux", "espace", "magie", "aventure", "océan", "forêt")
        val randomTheme = themes.random()
        
        return """
            Crée une courte histoire interactive sur le thème "$randomTheme" adaptée à un enfant de $childAge ans nommé $childName.
            L'histoire doit être engageante, éducative et se terminer par une question 
            pour encourager l'interaction. Utilise des emojis et un langage simple.
            Difficulté : niveau $difficultyLevel sur 3.
            Limite : maximum 150 mots.
        """.trimIndent()
    }

    
    private fun createHelpPrompt(): String {
        return """
            L'enfant $childName demande de l'aide. Propose-lui gentiment :
            - Des suggestions d'activités éducatives adaptées à son âge ($childAge ans)
            - Des encouragements personnalisés
            - Des conseils pour bien apprendre
            - Des idées de jeux éducatifs
            Sois bienveillant et utilise des emojis.
            Limite : maximum 100 mots.
        """.trimIndent()
    }
    
    private fun saveQuizScore(score: Int) {
        val success = databaseHelper.addChildScore("quiz", score, difficultyLevel, childAge)
        if (success) {
            // Show congratulations
            lifecycleScope.launch {
                val congratsMessage = "🎉 Bravo $childName ! Tu as gagné $score points ! Continue comme ça ! 🌟"
                chatAdapter.addMessage(ChatMessage(congratsMessage, false, System.currentTimeMillis(), "score"))
                scrollToBottom()
            }
        }
    }
    
    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }
} 