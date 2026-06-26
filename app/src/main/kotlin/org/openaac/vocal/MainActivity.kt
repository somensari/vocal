package org.openaac.vocal

import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.feature.board.BoardRoute
import org.openaac.vocal.feature.settings.SettingsRoute
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocalTheme {
                VocalApp()
            }
        }
    }
}

private object VocalDestination {
    const val Board = "board"
    const val Settings = "settings"
}

@Composable
private fun VocalApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
