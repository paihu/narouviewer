package dev.paihu.narou_viewer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.ui.ContentScreen
import dev.paihu.narou_viewer.ui.NovelScreen
import dev.paihu.narou_viewer.ui.PageScreen
import dev.paihu.narou_viewer.ui.SearchScreen
import kotlinx.coroutines.flow.Flow

enum class AppScreen {
    NovelList,
    PageList,
    ContentView,
    SearchView,
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: AppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    search: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentScreen != AppScreen.ContentView) {
        TopAppBar(
            title = { Text(currentScreen.name) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = modifier,
            navigationIcon = {
                Row {

                    IconButton(onClick = search) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "search"
                        )
                    }
                    if (canNavigateBack) {
                        IconButton(onClick = navigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "modoru"
                            )
                        }
                    }
                }
            }
        )
    }
}


data class NovelAppState(
    val selectedScreen: String,
    val selectedNovelId: String,
    val selectedNovelType: String,
    val selectedPage: Int,
    val changeSelectedScreen: (screen: AppScreen) -> Unit,
    val changeSelectedNovel: (selectedNovelId: String, selectedNovelType: String) -> Unit,
    val changeSelectedPage: (
        id: Int
    ) -> Unit
)


@Composable
fun rememberNovelAppState(): NovelAppState {
    var selectedNovelId by rememberSaveable { mutableStateOf("") }
    var selectedNovelType by rememberSaveable {
        mutableStateOf("")
    }

    var selectedPage by rememberSaveable { mutableIntStateOf(0) }
    var selectedScreen by rememberSaveable {
        mutableStateOf(AppScreen.NovelList.name)
    }
    return remember(selectedScreen, selectedNovelId, selectedNovelType, selectedPage) {
        NovelAppState(
            selectedScreen,
            selectedNovelId, selectedNovelType,
            selectedPage,
            { selectedScreen = it.name },
            { id, type ->
                selectedNovelId = id
                selectedNovelType = type
            },
            { selectedPage = it },
        )
    }
}

val ITEMS_PER_PAGE = 30

@Composable
fun NovelApp(
    db: AppDatabase,
    navController: NavHostController = rememberNavController(),
) {
    val novelAppState = rememberNovelAppState()
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: novelAppState.selectedScreen
    )


    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                search = { navController.navigate(AppScreen.SearchView.name) }
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
                novelAppState.changeSelectedScreen(AppScreen.NovelList)
                val novels: Flow<PagingData<dev.paihu.narou_viewer.data.Novel>> = remember {
                    Pager(
                        config = PagingConfig(
                            pageSize = ITEMS_PER_PAGE,
                            enablePlaceholders = false
                        ),
                        pagingSourceFactory = { db.novelDao().getPagingSource() }
                    ).flow
                }
                NovelScreen(novels.collectAsLazyPagingItems(), click = { id, type ->
                    novelAppState.changeSelectedNovel(id, type)
                    navController.navigate(AppScreen.PageList.name)
                })
            }
            composable(route = AppScreen.PageList.name) {
                novelAppState.changeSelectedScreen(AppScreen.PageList)
                val novel = remember(
                    key1 = novelAppState.selectedNovelId,
                    key2 = novelAppState.selectedNovelType
                ) {
                    db.novelDao().select(
                        novelAppState.selectedNovelId,
                        novelAppState.selectedNovelType,
                    )
                }
                PageScreen(
                    db,
                    novelAppState.selectedNovelId,
                    novelAppState.selectedNovelType,
                    novel?.lastReadPage ?: 0,
                    click = { num ->
                        novelAppState.changeSelectedPage(num - 1)
                        navController.navigate(AppScreen.ContentView.name)
                    })
            }
            composable(route = AppScreen.ContentView.name) {
                novelAppState.changeSelectedScreen(AppScreen.ContentView)
                ContentScreen(
                    db,
                    novelAppState.selectedNovelId,
                    novelAppState.selectedNovelType,
                    novelAppState.selectedPage
                )
            }
            composable(route = AppScreen.SearchView.name) {
                SearchScreen()
            }
        }
    }
}