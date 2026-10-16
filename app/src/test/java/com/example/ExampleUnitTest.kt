package com.example

import com.example.data.SeoRepository
import com.example.model.MessageSender
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: SeoRepository

    @Before
    fun setUp() {
        repository = SeoRepository()
    }

    @Test
    fun testInitialAuditData() {
        val audit = repository.currentAudit.value
        assertEquals("https://example.com", audit.url)
        assertEquals(92, audit.overallScore)
        assertTrue(audit.isHttps)
        assertTrue(audit.checklist.all { it.isPassed })
        assertEquals(5, audit.checklist.size)
    }

    @Test
    fun testSsrfBlockingForInternalIps() = runBlocking {
        val blockedResult = repository.runAudit("http://127.0.0.1:8080", "test keyword")
        assertTrue("Localhost must be blocked for SSRF safety", blockedResult.overallScore < 20)
        assertTrue(blockedResult.statusMessage.contains("Blocked", ignoreCase = true))

        val privateIpResult = repository.runAudit("http://192.168.1.1/admin", "test keyword")
        assertTrue("Private IP must be blocked for SSRF safety", privateIpResult.overallScore < 20)
    }

    @Test
    fun testAuditCsvExport() {
        val audit = repository.currentAudit.value
        val csv = repository.exportAuditCsv(audit)
        assertTrue(csv.contains("Category,Item,Status,Details"))
        assertTrue(csv.contains("https://example.com"))
        assertTrue(csv.contains("92/100"))
    }

    @Test
    fun testChatStateManagement() {
        val initialCount = repository.chatMessages.value.size
        repository.addChatMessage(MessageSender.USER, "How do I improve my CTR?")
        assertEquals(initialCount + 1, repository.chatMessages.value.size)
        assertEquals("How do I improve my CTR?", repository.chatMessages.value.last().content)
    }

    @Test
    fun testTipToggle() {
        val tip = repository.optimizationTips.value.first()
        val initialStatus = tip.isCompleted
        repository.toggleOptimizationTip(tip.id)
        val updatedTip = repository.optimizationTips.value.first()
        assertEquals(!initialStatus, updatedTip.isCompleted)
    }

    @Test
    fun testIssuesCategorizedBySeverity() {
        val audit = repository.currentAudit.value
        val criticals = audit.issues.filter { it.severity == com.example.model.IssueSeverity.CRITICAL }
        val warnings = audit.issues.filter { it.severity == com.example.model.IssueSeverity.WARNING }
        val infos = audit.issues.filter { it.severity == com.example.model.IssueSeverity.INFO }

        assertTrue("Must have at least one critical issue", criticals.isNotEmpty())
        assertTrue("Must have warning issues", warnings.isNotEmpty())
        assertTrue("Must have optimization opportunity info issues", infos.isNotEmpty())
        assertEquals(5, audit.issues.size)
    }
}
