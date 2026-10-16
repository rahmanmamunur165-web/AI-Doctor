package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.SeoRepository
import com.example.model.ScreenDestination
import com.example.ui.components.AppHeader
import com.example.ui.components.AppSidebar
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {

    private val repository = SeoRepository()
    private val geminiService = GeminiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RankBoostApp(repository = repository, geminiService = geminiService)
            }
        }
    }
}

@Composable
fun RankBoostApp(
    repository: SeoRepository,
    geminiService: GeminiService
) {
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 800

    var currentScreen by remember { mutableStateOf(ScreenDestination.DASHBOARD) }
    val selectedCountry by repository.selectedCountry.collectAsState()

    // Handle Android system back button: return to dashboard if on another tab
    BackHandler(enabled = currentScreen != ScreenDestination.DASHBOARD) {
        currentScreen = ScreenDestination.DASHBOARD
    }

    if (isExpandedScreen) {
        // Desktop / Tablet Landscape Layout: Sidebar on left + Content on right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            AppSidebar(
                currentDestination = currentScreen,
                onDestinationSelected = { currentScreen = it },
                selectedCountry = selectedCountry,
                onUpgradeClick = { currentScreen = ScreenDestination.SETTINGS }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AppHeader(
                    selectedCountry = selectedCountry,
                    availableCountries = repository.availableCountries,
                    onCountrySelected = { repository.setCountry(it) },
                    onSearchClick = { currentScreen = ScreenDestination.KEYWORD_RESEARCH },
                    onProfileClick = { currentScreen = ScreenDestination.SETTINGS }
                )

                Box(modifier = Modifier.weight(1f)) {
                    ScreenContent(
                        destination = currentScreen,
                        repository = repository,
                        geminiService = geminiService,
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        }
    } else {
        // Handheld Portrait Layout: Top Header + Bottom Navigation + Content
        Scaffold(
            topBar = {
                AppHeader(
                    selectedCountry = selectedCountry,
                    availableCountries = repository.availableCountries,
                    onCountrySelected = { repository.setCountry(it) },
                    onSearchClick = { currentScreen = ScreenDestination.KEYWORD_RESEARCH },
                    onProfileClick = { currentScreen = ScreenDestination.SETTINGS },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val bottomNavDestinations = listOf(
                        ScreenDestination.DASHBOARD to Icons.Default.Home,
                        ScreenDestination.SITE_AUDIT to Icons.Default.Analytics,
                        ScreenDestination.KEYWORD_RESEARCH to Icons.Default.Search,
                        ScreenDestination.AI_CHATBOT to Icons.Default.SmartToy,
                        ScreenDestination.RANK_TRACKER to Icons.AutoMirrored.Filled.ShowChart,
                        ScreenDestination.SETTINGS to Icons.Default.Settings
                    )

                    bottomNavDestinations.forEach { (dest, icon) ->
                        val isSelected = currentScreen == dest
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = dest },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = dest.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (dest == ScreenDestination.SITE_AUDIT) "Audit"
                                    else if (dest == ScreenDestination.KEYWORD_RESEARCH) "Keywords"
                                    else if (dest == ScreenDestination.AI_CHATBOT) "AI Chat"
                                    else if (dest == ScreenDestination.RANK_TRACKER) "Ranking"
                                    else dest.title,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = Color(0xFFEFF6FF),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen != ScreenDestination.AI_CHATBOT) {
                    ExtendedFloatingActionButton(
                        onClick = { currentScreen = ScreenDestination.AI_CHATBOT },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
                        text = { Text("Ask AI", fontSize = 12.sp) },
                        modifier = Modifier.testTag("floating_ai_assistant_button")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScreenContent(
                    destination = currentScreen,
                    repository = repository,
                    geminiService = geminiService,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: ScreenDestination,
    repository: SeoRepository,
    geminiService: GeminiService,
    onNavigate: (ScreenDestination) -> Unit
) {
    Crossfade(targetState = destination, label = "screen_transition") { screen ->
        when (screen) {
            ScreenDestination.DASHBOARD -> DashboardScreen(
                repository = repository,
                onNavigate = onNavigate
            )
            ScreenDestination.SEO_ANALYZER -> SeoAuditResultsScreen(
                repository = repository,
                geminiService = geminiService,
                onNavigate = onNavigate
            )
            ScreenDestination.SITE_AUDIT -> SiteAuditScreen(
                repository = repository
            )
            ScreenDestination.KEYWORD_RESEARCH -> KeywordResearchScreen(
                repository = repository
            )
            ScreenDestination.CONTENT_OPTIMIZER -> ContentOptimizerScreen(
                repository = repository,
                geminiService = geminiService
            )
            ScreenDestination.RANK_TRACKER -> RankTrackerScreen(
                repository = repository
            )
            ScreenDestination.SITEMAP_INDEXING -> SitemapScreen(
                repository = repository
            )
            ScreenDestination.REPORTS -> ReportsScreen(
                repository = repository
            )
            ScreenDestination.AI_CHATBOT -> GeminiChatScreen(
                repository = repository,
                geminiService = geminiService
            )
            ScreenDestination.SETTINGS -> SettingsScreen(
                repository = repository
            )
        }
    }
}
