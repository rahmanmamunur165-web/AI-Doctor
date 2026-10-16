package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.MessageSender
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
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiService"
        const val MODEL_FLASH = "gemini-3.8-flash"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite"
        const val MODEL_PRO = "gemini-3.1-pro-preview"

        const val SYSTEM_PROMPT = """You are RankBoost AI, an elite SEO Architect, Google Search Console Specialist, and Technical Audit Advisor.
Your mission is to provide clear, actionable, evidence-based SEO guidance.
Principles:
- Align strictly with Google Search Essentials and Helpful Content guidelines.
- Never guarantee #1 rankings or invent fabricated search volume or competitor metrics.
- Provide practical solutions for meta tags, heading hierarchies, internal linking, robots.txt, canonicalization, mobile responsiveness, and Core Web Vitals.
- Format responses cleanly with headings, bullet points, and code snippets when proposing schema markup (JSON-LD)."""
    }

    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        modelName: String = MODEL_FLASH
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSeoResponse(userMessage)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Append previous turns (up to last 10 messages for context)
            val recentHistory = history.takeLast(10)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val partObj = JSONObject().put("text", msg.content)
                val partsArr = JSONArray().put(partObj)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", partsArr)
                contentsArray.put(contentObj)
            }

            // Append current user message
            val currentPartObj = JSONObject().put("text", userMessage)
            val currentPartsArr = JSONArray().put(currentPartObj)
            val currentContentObj = JSONObject()
                .put("role", "user")
                .put("parts", currentPartsArr)
            contentsArray.put(currentContentObj)

            val rootJson = JSONObject()
            rootJson.put("contents", contentsArray)

            // System instruction
            val sysPartObj = JSONObject().put("text", SYSTEM_PROMPT)
            val sysContentObj = JSONObject().put("parts", JSONArray().put(sysPartObj))
            rootJson.put("systemInstruction", sysContentObj)

            // Generation config
            val genConfig = JSONObject()
                .put("temperature", 0.7)
                .put("topP", 0.95)
                .put("maxOutputTokens", 2048)
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error: ${response.code} $responseString")
                return@withContext generateLocalSeoResponse(userMessage)
            }

            val respJson = JSONObject(responseString)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) return@withContext text
                }
            }

            generateLocalSeoResponse(userMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed", e)
            generateLocalSeoResponse(userMessage)
        }
    }

    suspend fun generateContentOptimization(
        targetKeyword: String,
        url: String,
        taskType: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = when (taskType) {
            "title_meta" -> "Generate 3 high-converting, Google-compliant SEO Title tags (under 60 characters) and 3 Meta Descriptions (under 155 characters) for website '$url' targeting keyword '$targetKeyword'. Explain why each performs well."
            "headings" -> "Create a high-ranking semantic heading structure (H1, H2, H3 hierarchy) for an authoritative guide on '$targetKeyword' for '$url', addressing primary search intent and featured snippet opportunities."
            "faqs" -> "Generate 5 frequently asked questions and clear, concise answers for '$targetKeyword'. Include sample JSON-LD FAQPage schema markup."
            else -> "Provide 4 evidence-based on-page SEO improvements for '$url' targeting '$targetKeyword' without keyword stuffing."
        }

        sendChatMessage(emptyList(), prompt, MODEL_FLASH)
    }

    private fun generateLocalSeoResponse(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("title") || lower.contains("meta") -> """
### 🎯 SEO Title & Meta Description Best Practices

1. **Title Tag Blueprint (50–60 characters)**:
   - **Formula**: `Primary Keyword - Secondary Benefit | Brand Name`
   - *Example*: `Best Digital Marketing Tools (2026 Review) | RankBoost AI`
   - Keep key terms within the first 40 characters to avoid mobile SERP truncation.

2. **Meta Description (140–155 characters)**:
   - Include the search keyword naturally and add a clear user benefit or CTA.
   - *Example*: `Discover the top digital marketing tools tested for SEO, PPC, and content growth. Compare features, pricing, and boost your organic traffic today.`

3. **Checklist**:
   - ✅ Avoid duplicate meta descriptions across category pages.
   - ✅ Ensure each title accurately reflects the page's primary search intent.
            """.trimIndent()

            lower.contains("sitemap") || lower.contains("index") -> """
### 🗺️ Sitemap & Indexing Action Plan

1. **Sitemap Setup**:
   - Ensure your XML sitemap is accessible at `https://yourdomain.com/sitemap.xml`.
   - Only include canonical, indexable URLs (HTTP 200). Exclude 301 redirects, 404s, and `noindex` pages.

2. **Google Search Console Submission**:
   - Go to Google Search Console → **Sitemaps**.
   - Submit `sitemap.xml` and verify that status indicates "Success".

3. **Robots.txt Declaration**:
   - Add this line at the bottom of your `robots.txt`:
     `Sitemap: https://yourdomain.com/sitemap.xml`
            """.trimIndent()

            lower.contains("speed") || lower.contains("mobile") || lower.contains("cwv") -> """
### ⚡ Core Web Vitals & Technical Speed Optimization

1. **LCP (Largest Contentful Paint < 2.5s)**:
   - Preload hero images: `<link rel="preload" as="image" href="...">`
   - Compress images using WebP/AVIF formats with responsive `srcset`.

2. **INP (Interaction to Next Paint < 200ms)**:
   - Minimize long main-thread JavaScript tasks.
   - Defer third-party tracking scripts until after initial render.

3. **CLS (Cumulative Layout Shift < 0.1)**:
   - Explicitly define `width` and `height` attributes on all `<img>` and `<iframe>` elements.
            """.trimIndent()

            lower.contains("keyword") || lower.contains("research") -> """
### 🔍 Strategic Keyword Research Framework

1. **Search Intent Categorization**:
   - **Informational**: "how to optimize website speed" (Focus: comprehensive guides, infographics).
   - **Commercial Investigation**: "best seo software 2026" (Focus: comparison tables, pros/cons).
   - **Transactional**: "buy rankboost ai license" (Focus: pricing, frictionless checkout).

2. **Long-Tail Keyword Strategy**:
   - Target 3-5 word variations with clear user intent.
   - Example for *digital marketing tools*:
     - *digital marketing tools for small business*
     - *free digital marketing tools for startups*
     - *b2b digital marketing tools comparison*
            """.trimIndent()

            else -> """
### 🚀 RankBoost AI SEO Recommendations

Based on your query: **"$query"**

1. **Search Intent Alignment**: Verify whether searchers looking for this topic expect an in-depth tutorial, product comparison, or quick answer.
2. **On-Page Optimization**:
   - Place primary keyword in the `H1` and first 100 words.
   - Structure secondary topics with descriptive `H2` subheadings.
   - Add contextual internal links pointing to and from relevant pillar content.
3. **Structured Data**: Implement Schema.org markup (such as `Article`, `FAQPage`, or `SoftwareApplication`) to improve rich snippet eligibility.
4. **Monitoring**: Track impression and average position changes in Google Search Console over a 14-to-28 day window following updates.
            """.trimIndent()
        }
    }
}
