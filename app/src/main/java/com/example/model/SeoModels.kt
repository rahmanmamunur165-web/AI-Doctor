package com.example.model

enum class ScreenDestination(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "dashboard"),
    KEYWORD_RESEARCH("Keyword Research", "search"),
    SEO_ANALYZER("SEO Analyzer", "analytics"),
    CONTENT_OPTIMIZER("Content Optimizer", "edit_note"),
    RANK_TRACKER("Rank Tracker", "trending_up"),
    SITE_AUDIT("Site Audit", "fact_check"),
    SITEMAP_INDEXING("Sitemap & Indexing", "account_tree"),
    REPORTS("Reports", "assessment"),
    SETTINGS("Settings", "settings"),
    AI_CHATBOT("AI Assistant", "smart_toy")
}

data class CountryOption(
    val code: String,
    val name: String,
    val flagEmoji: String
)

data class KpiMetric(
    val title: String,
    val value: String,
    val subtext: String,
    val changeText: String,
    val isPositive: Boolean,
    val iconKey: String
)

data class AuditCheckItem(
    val title: String,
    val isPassed: Boolean,
    val details: String = ""
)

data class AuditResult(
    val url: String,
    val targetKeyword: String,
    val country: String,
    val overallScore: Int,
    val statusMessage: String,
    val checklist: List<AuditCheckItem>,
    val metaTitle: String,
    val metaDescription: String,
    val h1Heading: String,
    val h2Headings: List<String>,
    val canonicalUrl: String?,
    val isHttps: Boolean,
    val hasRobotsTxt: Boolean,
    val hasSitemap: Boolean,
    val brokenLinksCount: Int,
    val missingAltImagesCount: Int,
    val loadTimeMs: Long,
    val issues: List<AuditIssue>,
    val auditedAt: Long = System.currentTimeMillis()
)

enum class IssueSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class AuditIssue(
    val id: String,
    val category: String,
    val severity: IssueSeverity,
    val title: String,
    val description: String,
    val evidence: String,
    val recommendation: String,
    val requiresHumanReview: Boolean = true
)

data class KeywordSuggestion(
    val id: String,
    val keyword: String,
    val intent: String, // "Informational", "Commercial", "Transactional", "Navigational"
    val volumeEst: String,
    val difficulty: Int, // 1-100
    val cpcEst: String,
    val ideasCount: Int
)

data class RankTrendPoint(
    val label: String,
    val position: Float
)

data class AuditScoreTrendPoint(
    val label: String,
    val score: Int,
    val dateFull: String = "",
    val changeText: String = "",
    val notes: String = ""
)

data class CompetitorItem(
    val rank: Int,
    val domain: String,
    val domainRating: Int,
    val isUserSite: Boolean = false
)

data class OptimizationTip(
    val id: String,
    val text: String,
    val isCompleted: Boolean,
    val priority: String = "Medium"
)

data class SitemapSummary(
    val sitemapUrl: String,
    val isValidXml: Boolean,
    val urlCount: Int,
    val lastModified: String,
    val indexingStatus: String,
    val sampleUrls: List<String>
)

data class SearchConsoleQueryItem(
    val query: String,
    val clicks: Int,
    val impressions: Int,
    val ctr: Float,
    val position: Float
)

data class SearchConsoleData(
    val isConnected: Boolean,
    val propertyUrl: String,
    val totalClicks: String,
    val totalImpressions: String,
    val avgCtr: String,
    val avgPosition: String,
    val queries: List<SearchConsoleQueryItem>
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class PricingPlan(
    val id: String,
    val name: String,
    val price: String,
    val period: String,
    val description: String,
    val isPopular: Boolean,
    val features: List<String>
)
