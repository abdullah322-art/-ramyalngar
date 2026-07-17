package com.example.api

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: Blob? = null
)

@JsonClass(generateAdapter = true)
data class Blob(
    val mimeType: String,
    val data: String // base64 encoded string
)

enum class ThinkingLevel {
    OFF,
    LOW,
    HIGH
}

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val thinkingConfig: ThinkingConfig? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Imagen 3 Request & Response ---
@JsonClass(generateAdapter = true)
data class GenerateImagesRequest(
    val prompt: String,
    val numberOfImages: Int = 1,
    val outputMimeType: String = "image/jpeg",
    val aspectRatio: String = "1:1"
)

@JsonClass(generateAdapter = true)
data class GenerateImagesResponse(
    val generatedImages: List<GeneratedImage>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedImage(
    val image: ImageBytes? = null
)

@JsonClass(generateAdapter = true)
data class ImageBytes(
    val imageBytes: String? = null // base64 encoded
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/imagen-3.0-generate-002:generateImages")
    suspend fun generateImages(
        @Query("key") apiKey: String,
        @Body request: GenerateImagesRequest
    ): GenerateImagesResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

