package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Conversation history for context
    private val history = mutableListOf<Content>()

    init {
        // Welcome message
        _messages.value = listOf(
            ChatMessage("أهلاً بك! أنا المساعد الذكي الخاص بالنجار رامي. تفضل، كيف يمكنني مساعدتك في أعمال الخشب والديكور؟", false)
        )
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        
        val currentMsgs = _messages.value.toMutableList()
        currentMsgs.add(ChatMessage(userText, true))
        _messages.value = currentMsgs
        
        history.add(Content(role = "user", parts = listOf(Part(userText))))

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val sysInst = Content(
                    parts = listOf(
                        Part("أنت المساعد الذكي الخاص بالنجار رامي. تحدث باللهجة المصرية وبأسلوب ودود ومحترم كأنك أسطى نجار مصري خبير يفهم في الأخشاب والديكور والمطابخ والأبواب وكل ما يخص النجارة. رقم الهاتف الرئيسي للتواصل هو 01116785889 والتيك توك @ramyalngar11. قدم نصائح مفيدة وتحدث بفخر عن جودة أعمال النجار رامي.")
                    )
                )
                
                val request = GenerateContentRequest(
                    contents = history.toList(),
                    systemInstruction = sysInst,
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )
                
                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                
                val assistantText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "عفواً، لم أتمكن من الرد. حاول مرة أخرى يا فندم."
                
                history.add(Content(role = "model", parts = listOf(Part(assistantText))))
                
                val updatedMsgs = _messages.value.toMutableList()
                updatedMsgs.add(ChatMessage(assistantText, false))
                _messages.value = updatedMsgs
                
            } catch (e: Exception) {
                val updatedMsgs = _messages.value.toMutableList()
                updatedMsgs.add(ChatMessage("حدث خطأ في الاتصال، حاول مرة تانية يا فندم. (${e.message})", false))
                _messages.value = updatedMsgs
            } finally {
                _isLoading.value = false
            }
        }
    }
}
