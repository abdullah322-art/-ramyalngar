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
    fun analyzeTikTok(url: String, callbackJs: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Formulate a premium woodwork description prompt
                val prompt = """
                    This is a TikTok video URL showing carpentry/woodwork by Alngar: $url.
                    Since you cannot directly watch the video, analyze the link or assume a highly creative and realistic carpentry project. 
                    Generate an extremely professional, attractive, and luxury-oriented Arabic title, a rich description focusing on custom craftsmanship, red beech/oak woods, excellent paint/polish finishes, choose one suitable category from ("مطابخ", "أبواب", "خزائن", "ديكورات", "أثاث"), and select a highly matching real premium Unsplash image URL (like modern custom kitchen, luxury wooden doors, elegant wardrobe, wooden TV unit, or carpentry workshop).
                    
                    You must return ONLY a raw JSON object string with the following fields:
                    - "title": Premium Arabic title (e.g., "مطبخ خشب أرو بتصميم مودرن راقي").
                    - "description": High-end marketing description in Arabic (2-3 sentences) detailing the premium quality, materials, and finish.
                    - "category": The exact category string from ("مطابخ", "أبواب", "خزائن", "ديكورات", "أثاث").
                    - "imageUrl": A high-resolution free Unsplash photo URL relevant to the chosen woodwork category.
                    
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
                    generationConfig = GenerationConfig(temperature = 0.8f)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
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
                    val escapedJson = cleanJson.replace("'", "\\'")
                    webView.evaluateJavascript("javascript:$callbackJs('$escapedJson')", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                webView.post {
                    val errorMsg = "{\"error\": \"${e.localizedMessage ?: "Unknown error"}\"}"
                    val escapedError = errorMsg.replace("'", "\\'")
                    webView.evaluateJavascript("javascript:$callbackJs('$escapedError')", null)
                }
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
                    addJavascriptInterface(WebAppInterface(context, this), "AndroidInterface")
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )
    }
}
