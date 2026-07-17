package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.BuildConfig
import com.example.api.*

class WebAppInterface(private val context: Context, private val webView: WebView) {
    @JavascriptInterface
    fun submitForm(name: String, phone: String, message: String) {
        // Show a friendly native Toast message in Arabic
        Toast.makeText(context, "أهلاً بك أستاذ $name! جاري توجيهك لتأكيد إرسال استفسارك.", Toast.LENGTH_LONG).show()

        // Open WhatsApp programmatically with pre-filled content
        try {
            val whatsappText = "مرحباً أستاذ رامي، لدي استفسار من تطبيق معرض الأعمال:\n\n*الاسم:* $name\n*الهاتف:* $phone\n*الرسالة:* $message"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=201116785889&text=" + Uri.encode(whatsappText))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to launching an email client
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:ramyalngar11@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "استفسار جديد من تطبيق معرض الأعمال")
                    putExtra(Intent.EXTRA_TEXT, "الاسم: $name\nالهاتف: $phone\n\nالرسالة:\n$message")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(emailIntent)
            } catch (ex: Exception) {
                // Last fallback: show the input message
                Toast.makeText(context, "الرسالة: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun saveSiteData(dataJson: String) {
        try {
            val sharedPrefs = context.getSharedPreferences("ramy_carpentry_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("site_data", dataJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun loadSiteData(): String {
        return try {
            val sharedPrefs = context.getSharedPreferences("ramy_carpentry_prefs", Context.MODE_PRIVATE)
            sharedPrefs.getString("site_data", "") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @JavascriptInterface
    fun analyzeTikTok(url: String, callbackJs: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Formulate an extremely professional, authentic, and realistic woodwork description prompt
                val prompt = """
                    This is a TikTok video URL showing premium custom carpentry/woodwork by the master carpenter Ramy Alngar (الأسطى رامي النجار): $url.
                    Since you cannot watch the video directly, analyze the URL details or generate a highly realistic and detailed masterpiece carpentry project.
                    
                    CRITICAL INSTRUCTION: If the URL or video mentions "kitchen", "cook", "مطبخ", "مطابخ", or is general/unspecified, you MUST assume the video is about a premium American Kitchen (مطبخ أمريكي راقي) with exquisite wooden cabinets, an island, and integrated spaces. NEVER generate an exterior building, house, facade, landscape, or villa architecture! The result must be strictly about custom interior wooden craftsmanship.
                    
                    Your analysis must be technical, authentic, and luxurious (تحليل فني دقيق وحقيقي لنجارة داخلية). It should specify:
                    1. Wood Type (نوع الخشب المستخدم): (e.g., خشب زان أحمر روماني مبخر، خشب أرو أمريكي طبيعي، خشب موسكي فنلندي نخب أول، أو خشب كونتر اندونيسي معالج).
                    2. Joinery & Hardware (الإكسسوارات والمفصلات): (e.g., مفصلات هيدروليك باكم إيطالية صامتة، سحابات أدراج رمان بلي مخفية، مقابض نحاسية معالجة).
                    3. Finish & Varnish (نوع الدهان والتشطيب): (e.g., دهان بولي يوريثان مقاوم للرطوبة والخدش، قشرة أرو طبيعية مع دهان شفاف يبرز جمال ثمرة الخشب، أو دوكو مغسول فرن).
                    4. Style & Design (النمط الهندسي): (Modern, Classic, Neo-Classic, Minimalist).
                    
                    Generate an extremely professional, luxury-oriented Arabic title (e.g., "مطبخ أمريكي مودرن فاخر من خشب الأرو"), and a rich marketing description (2-3 sentences) detailing the craftsmanship, premium wood, Italian soft-close hardware, and pristine scratch-resistant finish.
                    
                    Choose exactly one suitable category from ("مطابخ", "أبواب", "خزائن", "ديكورات", "أثاث").
                    
                    Select a highly relevant, real premium Unsplash image URL representing luxury custom interior woodwork for that category. YOU MUST strictly choose from the following curated list based on the category:
                    - If category is "مطابخ" (Kitchens): Choose "https://images.unsplash.com/photo-1556911220-e15b29be8c8f" or "https://images.unsplash.com/photo-1600585154340-be6161a56a0c" or "https://images.unsplash.com/photo-1565538810844-1e1194826ff0".
                    - If category is "أبواب" (Doors): Choose "https://images.unsplash.com/photo-1513694203232-719a280e022f" or "https://images.unsplash.com/photo-1509644851169-2acc08aa25b5".
                    - If category is "خزائن" (Wardrobes): Choose "https://images.unsplash.com/photo-1505691938895-1758d7feb511" or "https://images.unsplash.com/photo-1595428774223-ef52624120d2".
                    - If category is "ديكورات" (Decor): Choose "https://images.unsplash.com/photo-1533090161767-e6ffed986c88" or "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6".
                    - If category is "أثاث" (Furniture): Choose "https://images.unsplash.com/photo-1497366216548-37526070297c" or "https://images.unsplash.com/photo-1540555700478-4be289fbecef".
                    
                    You must return ONLY a raw JSON object string with the following fields:
                    - "title": Premium Arabic title.
                    - "description": High-end authentic marketing description in Arabic.
                    - "category": The exact category string from ("مطابخ", "أبواب", "خزائن", "ديكورات", "أثاث").
                    - "imageUrl": One of the recommended Unsplash URLs matching the category.
                    
                    Do NOT wrap the JSON in markdown formatting (no ```json). Output raw valid JSON.
                """.trimIndent()

                val sysInst = Content(
                    parts = listOf(
                        Part("أنت خبير تسويق وتصميم ديكورات خشبية ومساعد ذكي للنجار رامي النجار. تقوم بصياغة تفاصيل مشروعات النجارة بأعلى درجة من الاحترافية والجاذبية باللغة العربية وتحلل روابط الفيديوهات لتخيل تفاصيلها وتوليد بيانات ممتازة للعرض.")
                    )
                )

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(prompt)))),
                    systemInstruction = sysInst,
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        responseMimeType = "application/json"
                    )
                )

                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                var cleanJson = responseText.trim()
                if (cleanJson.startsWith("```json")) {
                    cleanJson = cleanJson.removePrefix("```json")
                }
                if (cleanJson.endsWith("```")) {
                    cleanJson = cleanJson.removeSuffix("```")
                }
                cleanJson = cleanJson.trim()

                // Execute callback on Main WebView Thread
                webView.post {
                    val base64Json = android.util.Base64.encodeToString(cleanJson.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(decodeURIComponent(escape(window.atob('$base64Json'))))", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                webView.post {
                    val errorMsg = "{\"error\": \"${e.localizedMessage ?: "Unknown error"}\"}"
                    val base64Json = android.util.Base64.encodeToString(errorMsg.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(decodeURIComponent(escape(window.atob('$base64Json'))))", null)
                }
            }
        }
    }

    @JavascriptInterface
    fun chatWithGemini(historyJson: String, systemInstructionText: String, callbackJs: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Parse the chat history JSON array
                val moshi = com.squareup.moshi.Moshi.Builder().build()
                val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, Content::class.java)
                val adapter = moshi.adapter<List<Content>>(type)
                val contentsList = adapter.fromJson(historyJson) ?: emptyList()
                
                // System Instruction
                val systemInstruction = if (systemInstructionText.isNotEmpty()) {
                    Content(parts = listOf(Part(text = systemInstructionText)))
                } else null
                
                val request = GenerateContentRequest(
                    contents = contentsList,
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )
                
                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "عذراً يا فندم، حدث أمر غير متوقع. هل يمكنك المحاولة مرة أخرى؟"
                
                // Execute callback on Main WebView Thread
                webView.post {
                    val base64Text = android.util.Base64.encodeToString(responseText.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(true, decodeURIComponent(escape(window.atob('$base64Text'))))", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                webView.post {
                    val errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع في معالجة طلبك."
                    val base64Text = android.util.Base64.encodeToString(errorMsg.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(false, decodeURIComponent(escape(window.atob('$base64Text'))))", null)
                }
            }
        }
    }

    @JavascriptInterface
    fun analyzeImageWithGemini(base64Data: String, mimeType: String, callbackJs: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val textPrompt = "أنت الأسطى رامي النجار؛ حلل هذه الصورة التي أرسلها العميل لقطع الأثاث أو الديكورات الخشبية. حدد نوع الخشب المتوقع (زان أحمر روماني، أرو أمريكي طبيعي، كونتر ممتاز، إلخ)، واشرح الأسلوب الفني (مودرن، كلاسيك)، وقدم نصائح صنايعي قديم خبير بلهجتك المصرية النجارية الودية الجميلة عن مميزاتها والتشطيب الأفضل ليها وتكلفتها التقديرية بالحب كدا."
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = textPrompt),
                                Part(inlineData = Blob(mimeType = mimeType, data = base64Data))
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f
                    )
                )
                val response = try {
                    RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "يا فندم الصورة جميلة خالص بس حصلت مشكلة في قرائتها.. ارفعها تاني عيني ليك يا باشا!"
                
                webView.post {
                    val base64Text = android.util.Base64.encodeToString(responseText.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(true, decodeURIComponent(escape(window.atob('$base64Text'))))", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                webView.post {
                    val errorMsg = e.localizedMessage ?: "حدث خطأ أثناء تحليل الصورة."
                    val base64Text = android.util.Base64.encodeToString(errorMsg.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                    webView.evaluateJavascript("javascript:$callbackJs(false, decodeURIComponent(escape(window.atob('$base64Text'))))", null)
                }
            }
        }
    }

    @JavascriptInterface
    fun generateImageWithGemini(prompt: String, resolution: String, callbackJs: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Enhance prompt for better results
                val enhancedPrompt = "Luxury premium high-quality woodwork carpentry, highly detailed, photorealistic studio interior render, natural wood grain details, master carpenter finish: $prompt"
                val encodedPrompt = android.net.Uri.encode(enhancedPrompt)
                val width = if (resolution == "4k") 1024 else 800
                val height = if (resolution == "4k") 1024 else 800
                
                // Use Pollinations AI for free, fast, high-quality real AI image generation
                val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&nologo=true"
                
                webView.evaluateJavascript("javascript:$callbackJs(true, '$imageUrl', true)", null)
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: "حدث خطأ أثناء ابتكار التصميم."
                val base64Text = android.util.Base64.encodeToString(errorMsg.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
                webView.evaluateJavascript("javascript:$callbackJs(false, decodeURIComponent(escape(window.atob('$base64Text'))))", null)
            }
        }
    }
}

@Composable
fun HtmlScreen() {
    Scaffold { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient()
                    addJavascriptInterface(WebAppInterface(context, this), "AndroidInterface")
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )
    }
}
