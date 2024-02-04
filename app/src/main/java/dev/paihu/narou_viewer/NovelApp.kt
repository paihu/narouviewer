package dev.paihu.narou_viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.data.NovelRepository
import dev.paihu.narou_viewer.data.PageRepository
import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.ui.ContentScreen
import dev.paihu.narou_viewer.ui.ContentViewModel
import dev.paihu.narou_viewer.ui.NovelScreen
import dev.paihu.narou_viewer.ui.PageScreen
import dev.paihu.narou_viewer.ui.PageViewModel

enum class AppScreen {
    NovelList,
    PageList,
    ContentView
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: AppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    currentNovel: Novel?,
    modifier: Modifier = Modifier
) {
    if(currentScreen!= AppScreen.ContentView){
    TopAppBar(
        title = { Text(if (currentScreen == AppScreen.PageList && currentNovel != null) currentNovel.title else currentScreen.name) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "modoru"
                    )
                }
            }
        }
    )}
}

@Composable
fun NovelApp(
    viewModel: NovelViewModel = NovelViewModel(NovelRepository()),
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: AppScreen.NovelList.name
    )
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                currentNovel = viewModel.currentNovel,
            )
        }
    ) { innerPadding ->

    NavHost(
            navController = navController,
            startDestination = AppScreen.NovelList.name,
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = AppScreen.NovelList.name) {
                NovelScreen(viewModel.novels.collectAsLazyPagingItems()) { id ->
                    viewModel.selectNovel(id)
                    navController.navigate(AppScreen.PageList.name)
                }
            }
            composable(route = AppScreen.PageList.name) {
                val pageViewModel = PageViewModel(uiState.selectedNovel, PageRepository())
                PageScreen(pageViewModel) { id ->
                    viewModel.selectPage(id)
                    navController.navigate(AppScreen.ContentView.name)
                }
            }
            composable(route = AppScreen.ContentView.name) {
                val contentViewModel = ContentViewModel(
                    uiState.selectedNovel, uiState.selectedPage,
                    PageRepository()
                )
                ContentScreen(contentViewModel)
            }
        }
    }
}