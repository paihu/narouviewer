package dev.paihu.narou_viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.ui.ContentScreen
import dev.paihu.narou_viewer.ui.NovelScreen
import dev.paihu.narou_viewer.ui.NovelViewModel
import dev.paihu.narou_viewer.ui.PageScreen

enum class AppScreen {
    NovelList,
    PageList,
    ContentView
}


@Composable
fun NovelApp(
    viewModel: NovelViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: AppScreen.NovelList.name
    )

    Scaffold { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        NavHost(
            navController = navController,
            startDestination = AppScreen.NovelList.name,
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = AppScreen.NovelList.name) {
                NovelScreen(uiState.novels) { id ->
                    viewModel.selectNovel(id)
                    navController.navigate(AppScreen.PageList.name)
                }
            }
            composable(route = AppScreen.PageList.name) {
                PageScreen(Datasource.loadPages(uiState.selectedNovel)) { id ->
                    viewModel.selectPage(id)
                    navController.navigate(AppScreen.ContentView.name)
                }
            }
            composable(route = AppScreen.ContentView.name) {
                ContentScreen(page = Datasource.loadPage(uiState.selectedPage))
            }
        }
    }
}