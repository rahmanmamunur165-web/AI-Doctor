package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.AuditResult
import com.example.model.IssueSeverity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun exportToPdf(context: Context, audit: AuditResult, effectiveScore: Int): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(reportsDir, "rankboost_seo_audit_$timeStamp.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header Background Banner
        paint.color = AndroidColor.parseColor("#0F172A") // NavySidebar
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Brand Title
        paint.color = AndroidColor.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RankBoost AI — SEO Audit Report", 30f, 40f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = AndroidColor.parseColor("#94A3B8")
        canvas.drawText("Analyze. Optimize. Rank Higher. • Generated on ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}", 30f, 62f, paint)

        var y = 120f

        // Overview Box
        paint.color = AndroidColor.parseColor("#F8FAFC")
        canvas.drawRoundRect(30f, y, 565f, y + 90f, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.parseColor("#E2E8F0")
        canvas.drawRoundRect(30f, y, 565f, y + 90f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Score display inside overview
        val scoreColor = if (effectiveScore >= 80) "#059669" else "#DC2626"
        paint.color = AndroidColor.parseColor(scoreColor)
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("$effectiveScore/100", 45f, y + 45f, paint)

        paint.textSize = 10f
        paint.color = AndroidColor.parseColor("#64748B")
        canvas.drawText("OVERALL SEO SCORE", 45f, y + 65f, paint)

        // Metadata details
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Target URL: ${audit.url}", 170f, y + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = AndroidColor.parseColor("#475569")
        canvas.drawText("Keyword: \"${audit.targetKeyword}\" • Country: ${audit.country}", 170f, y + 48f, paint)
        canvas.drawText("Protocol: ${if (audit.isHttps) "HTTPS (Secure)" else "HTTP (Insecure)"} • TTFB: ${audit.loadTimeMs}ms • Checklist: ${audit.checklist.count { it.isPassed }}/${audit.checklist.size} Passed", 170f, y + 66f, paint)

        y += 115f

        // Section Title: Recommendations
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Prioritized Audit Recommendations by Severity", 30f, y, paint)

        y += 20f

        // Iterate through categorized issues
        for (issue in audit.issues) {
            if (y > 760f) break // Avoid overflowing page bounds

            val (severityBadgeColor, severityLabel) = when (issue.severity) {
                IssueSeverity.CRITICAL -> Pair("#DC2626", "[CRITICAL]")
                IssueSeverity.WARNING -> Pair("#D97706", "[WARNING]")
                IssueSeverity.INFO -> Pair("#2563EB", "[OPPORTUNITY]")
            }

            paint.color = AndroidColor.parseColor(severityBadgeColor)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("$severityLabel ${issue.category.uppercase()}", 30f, y, paint)

            y += 14f

            paint.color = AndroidColor.parseColor("#0F172A")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(issue.title.take(75), 30f, y, paint)

            y += 14f

            paint.color = AndroidColor.parseColor("#475569")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Fix: ${issue.recommendation.take(90)}", 30f, y, paint)

            y += 18f
        }

        // Footer Policy Disclaimer
        paint.color = AndroidColor.parseColor("#94A3B8")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("RankBoost AI conforms to Google Search Essentials. Deterministic technical observations with zero fabricated metrics.", 30f, 810f, paint)

        pdfDocument.finishPage(page)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun exportToCsv(context: Context, audit: AuditResult, csvContent: String): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(reportsDir, "rankboost_seo_audit_$timeStamp.csv")

        csvFile.writeText(csvContent)
        return csvFile
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
