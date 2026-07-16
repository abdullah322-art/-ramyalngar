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

class WebAppInterface(private val context: Context) {
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
                    addJavascriptInterface(WebAppInterface(context), "AndroidInterface")
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )
    }
}
