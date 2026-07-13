package org.openaac.vocal

import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.feature.board.BoardRoute
import org.openaac.vocal.feature.settings.SettingsRoute
import org.openaac.vocal.monitoring.NewRelicMonitoring
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var monitoringRepository: MonitoringRepository

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        // New Relic: must be the first line of the launcher Activity onCreate — not Application.
        // https://docs.newrelic.com/docs/mobile-monitoring/new-relic-mobile-android/troubleshoot/no-data-appears-android/
        NewRelicMonitoring.start(application)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val boardThemePreset by userPreferencesRepository.boardThemePreset.collectAsStateWithLifecycle(
                initialValue = BoardThemePreset.Default,
            )

            VocalTheme(boardThemePreset = boardThemePreset) {
                VocalApp(monitoringRepository = monitoringRepository)
            }
        }
    }
}

private object VocalDestination {
    const val Board = "board"
    const val Settings = "settings"
}

@Composable
private fun VocalApp(monitoringRepository: MonitoringRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    LaunchedEffect(currentRoute) {
        val screen = when (currentRoute) {
            VocalDestination.Board -> MonitoringEvents.Screen.Board
            VocalDestination.Settings -> MonitoringEvents.Screen.Settings
            else -> return@LaunchedEffect
        }
        // Name the default Activity interaction for Compose destinations.
        monitoringRepository.setInteractionName("Display $screen")
        monitoringRepository.recordBreadcrumb(
            name = "screen_view",
            attributes = mapOf(MonitoringEvents.Attr.Screen to screen),
        )
        monitoringRepository.recordCustomEvent(
            eventName = MonitoringEvents.Name.ScreenView,
            attributes = mapOf(MonitoringEvents.Attr.Screen to screen),
        )
        monitoringRepository.setSessionAttribute("vocal.current_screen", screen)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any {
                        it.route == VocalDestination.Board
                    } == true,
                    onClick = {
                        navController.navigate(VocalDestination.Board) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ViewModule,
                            contentDescription = stringResource(R.string.nav_board),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_board)) },
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any {
                        it.route == VocalDestination.Settings
                    } == true,
                    onClick = {
                        navController.navigate(VocalDestination.Settings) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = VocalDestination.Board,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(VocalDestination.Board) {
                BoardRoute()
            }
            composable(VocalDestination.Settings) {
                SettingsRoute()
            }
        }
    }
}
