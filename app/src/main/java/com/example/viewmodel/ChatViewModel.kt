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

    private val _isThinkingMode = MutableStateFlow(false)
    val isThinkingMode: StateFlow<Boolean> = _isThinkingMode
    
    // Conversation history for context
    private val history = mutableListOf<Content>()

    init {
        // Welcome message
        _messages.value = listOf(
            ChatMessage("أهلاً بك! أنا المساعد الذكي الخاص بالنجار رامي. تفضل، كيف يمكنني مساعدتك في أعمال الخشب والديكور؟", false)
        )
    }

    fun toggleThinkingMode(enabled: Boolean) {
        _isThinkingMode.value = enabled
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
                
                var assistantText: String? = null
                
                if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.startsWith("MY_")) {
                    val isHighThinking = _isThinkingMode.value
                    val modelsToTry = if (isHighThinking) {
                        listOf("gemini-3.1-pro-preview", "gemini-2.5-pro", "gemini-1.5-pro")
                    } else {
                        listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-3.5-flash")
                    }
                    
                    for (modelName in modelsToTry) {
                        try {
                            val genConfig = if (isHighThinking) {
                                GenerationConfig(
                                    temperature = null,
                                    thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.HIGH.name)
                                )
                            } else {
                                GenerationConfig(temperature = 0.7f)
                            }

                            val request = GenerateContentRequest(
                                contents = history.toList(),
                                systemInstruction = sysInst,
                                generationConfig = genConfig
                            )
                            
                            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
                            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!text.isNullOrBlank()) {
                                assistantText = text
                                break
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                if (assistantText.isNullOrBlank()) {
                    // Fallback to local expert simulation:
                    val lastUserText = userText.lowercase()
                    assistantText = when {
                        lastUserText.contains("مطبخ") || lastUserText.contains("مطابخ") -> {
                            "يا مرحب بيك يا فندم! المطابخ لعبتنا وتخصصنا في الورشة هنا. بنعمل المطابخ الخشبية الكلاسيك من خشب الأرو الطبيعي أو الزان الأحمر الروماني، وبنعمل المطابخ المودرن بأحدث الخامات زي الـ HPL والـ Polylac والـ Acrylic على شاسيه كونتر ممتاز مقاوم للرطوبة والمياه. بنستخدم مفصلات باكم هيدروليك إيطالي صامتة وأدراج سحاب رمان بلي عشان النعومة والمتانة. قولي مساحة مطبخك كام في كام، ونعملك تصميم تحفة خصيصاً ليك يا باشا!"
                        }
                        lastUserText.contains("باب") || lastUserText.contains("أبواب") || lastUserText.contains("شباك") || lastUserText.contains("نوافذ") -> {
                            "منور يا باشا! الأبواب عندنا بتتصنع من أنضف أنواع الخشب زي خشب الموسكي الفنلندي المحشو السويدي، أو الزان الأحمر الثقيل للأبواب الخارجية عشان الأمان والمتانة. وبنكسيها قشرة أرو طبيعي شكلها يجنن مع دهان استر شفاف يظهر جمال ثمرة الخشب، أو دهان دوكو فرن مغسول ناصع البياض ومقاوم للرطوبة والخدش. قولي محتاج كام باب ومقاساتهم التقريبية، وهعملك عرض سعر يرضيك وزيادة يا فندم!"
                        }
                        lastUserText.contains("سعر") || lastUserText.contains("الأسعار") || lastUserText.contains("تكلفة") || lastUserText.contains("بكم") || lastUserText.contains("بكام") -> {
                            "على راسي يا غالي! الأسعار عندنا بتتحسب بالحب وبمنتهى الأمانة حسب نوع الخشب اللي بتختاره والتشطيب والإكسسوارات. مثلاً، شغل الأرو والزان الروماني ليه سعره عشان بيعيش العمر كله، والموسكي والكونتر المعالج بيكون اقتصادي وممتاز جداً. شغلنا كله بضمان حقيقي عشان إحنا بنصنع بضمير صنايعي قديم. عشان أديك تسعيرة مظبوطة تخدمك بجد، ابعتلي تفاصيل المقاسات أو اضغط على زر الواتساب وأنا هجيلك بنفسي أعمل معاينة مجانية كاملة ونظبط السعر سوا يا فندم!"
                        }
                        lastUserText.contains("غرفة") || lastUserText.contains("نوم") || lastUserText.contains("أثاث") || lastUserText.contains("سرير") || lastUserText.contains("دولاب") || lastUserText.contains("خزانة") -> {
                            "يا ست الهوانم ويا باشا مصر، غرف النوم والدواليب وتفصيل الأثاث عندنا عمولة على أبوه! بنستخدم شاسيهات كونتر ممتاز مدعم بالزان عشان تضمن إن الدولاب أو السرير ما يريحش ولا يقطم منك أبداً مهما عاش. الدواليب بنعملها بجرارات إيطالية ناعمة جداً، والسرير بملل خشبية متينة للغاية. ابعتيلي صورة الموديل اللي عاجبك من النت، والورشة هتنفهولك بالملي وبجودة أحسن من المستورد بكتير وبسعر أقل بكتير!"
                        }
                        else -> {
                            "يا فندم يا مرحب بيك في ورشة الأسطى رامي النجار! منورنا يا غالي. أنا هنا عشان أخدمك في أي حاجة تخص النجارة والديكورات الخشبية، المطابخ، الأبواب، الدواليب، أو تجليد الحوائط. شغلنا كله عمولة نضافة بضمير وصناعة تعيش العمر كله. اسألني عن أي حاجة حابب تعرفها في النجارة والأنواع المظبوطة وهجاوبك فوراً يا باشا!"
                        }
                    }
                }
                
                val finalReply = assistantText!!
                history.add(Content(role = "model", parts = listOf(Part(finalReply))))
                
                val updatedMsgs = _messages.value.toMutableList()
                updatedMsgs.add(ChatMessage(finalReply, false))
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
