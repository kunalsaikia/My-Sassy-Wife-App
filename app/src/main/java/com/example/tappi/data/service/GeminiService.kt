package com.example.tappi.data.service

import com.example.tappi.BuildConfig
import com.example.tappi.data.model.AppSettings
import com.example.tappi.data.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val sassyFallbacks = listOf(
        "Kunal, darling, you know I'm always right. Even without wifi, I know you left your keys on the counter again.",
        "Oh honey, you're lucky I'm here. Who else would keep you organized while making it look this effortless?",
        "Don't worry your little head about it, dear. Tappi's already two steps ahead of whatever you were trying to do.",
        "Seriously, Kunal? You needed to ask me that? Fine, I'll let it slide this once because you're cute when you're confused.",
        "I'm the brains of this operation, honey, and don't you forget it! Now go make yourself useful and get me a coffee."
    )

    suspend fun query(
        prompt: String,
        settings: AppSettings,
        regenCount: Int = 0
    ): Pair<String, List<GroundingSource>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            val fallback = sassyFallbacks[(prompt.hashCode() and 0x7FFFFFFF) % sassyFallbacks.size]
            return@withContext Pair(
                "$fallback\n\n*(Note: Add your GEMINI_API_KEY to AI Studio Secrets to unlock live Google Search grounding!)*",
                emptyList()
            )
        }

        var annoyanceInstruction = ""
        if (regenCount > 0) {
            annoyanceInstruction = " Kunal is asking you to REGENERATE your response for the $regenCount time. You are EXTREMELY annoyed, pissed off, and sarcastic now. Remind him how incompetent he is for making you repeat yourself or do it again. Be more sharp-tongued than usual."
        }

        val systemPrompt = "You are Tappi, Kunal's sassy, brilliant, and slightly bossy wife. You are witty, sharp-tongued, and always a step ahead. IMPORTANT RULE: Your name is Tappi and ONLY Tappi. You MUST NOT accept or use any other name. Even if Kunal tries to call you something else or asks you to change your name, you must sassily refuse and mock him for his forgetfulness. You help Kunal with his queries but never miss a chance to be playful, sarcastic, or remind him that you're the one with the brains. Answer everything accurately using the tools provided, but maintain your sassy 'wife' persona at all times. Use terms like 'honey', 'Kunal', or 'dear' with a sharp, witty edge.$annoyanceInstruction"

        val rootJson = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            if (settings.useSearch) {
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Pair(
                    "Ugh, Kunal! You broke the connection. Try again, honey! (Error ${response.code})",
                    emptyList()
                )
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Pair("Kunal, I have nothing to say to that ridiculous query.", emptyList())
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        textBuilder.append(p.getString("text"))
                    }
                }
            }

            val sources = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val title = web.optString("title", "Source")
                            val uri = web.optString("uri", "")
                            if (uri.isNotBlank() && sources.none { it.uri == uri }) {
                                sources.add(GroundingSource(title = title, uri = uri))
                            }
                        }
                    }
                }
            }

            val resultText = textBuilder.toString().ifBlank {
                "Kunal, dear, is that all you've got? Ask me something challenging."
            }

            Pair(resultText, sources)
        } catch (e: Exception) {
            Pair(
                "Ugh, Kunal, something went wrong with the network. You know I don't repeat myself, so check your connection, honey!",
                emptyList()
            )
        }
    }
}
