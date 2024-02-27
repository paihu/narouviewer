package dev.paihu.narou_viewer

import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.datastore.AppState
import dev.paihu.narou_viewer.network.KakuyomuService
import dev.paihu.narou_viewer.network.NarouService
import dev.paihu.narou_viewer.ui.ContentScreen
import dev.paihu.narou_viewer.ui.DownloadDialog
import dev.paihu.narou_viewer.ui.NovelScreen
import dev.paihu.narou_viewer.ui.PageScreen
import dev.paihu.narou_viewer.ui.SearchScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    val changeSelectedNovel: (novelId: String, novelType: String) -> Unit,
    val changeSelectedPage: (
        id: Int
    ) -> Unit
)


@Composable
fun rememberNovelAppState(
    novelId: String = "",
    novelType: String = "",
    page: Int = 0,
    screen: String = AppScreen.NovelList.name
): NovelAppState {
    var selectedNovelId by rememberSaveable { mutableStateOf(novelId) }
    var selectedNovelType by rememberSaveable {
        mutableStateOf(novelType)
    }

    var selectedPage by rememberSaveable { mutableIntStateOf(page) }
    var selectedScreen by rememberSaveable {
        mutableStateOf(screen)
    }
    return remember(selectedScreen, selectedNovelId, selectedNovelType, selectedPage) {
        NovelAppState(
            selectedScreen,
            selectedNovelId,
            selectedNovelType,
            selectedPage,
            { selectedScreen = it.name },
            { novelId, novelType -> selectedNovelId = novelId; selectedNovelType = novelType },
            { selectedPage = it },
        )
    }
}

const val ITEMS_PER_PAGE = 30

@Composable
fun NovelApp(
    db: AppDatabase,
    datastore: DataStore<AppState>,
    uri: Uri? = null,
    navController: NavHostController = rememberNavController(),
) {
    val datastoreData = remember {
        runBlocking { datastore.data.first() }
    }

    val novelAppState = rememberNovelAppState(
        datastoreData.selectedNovelId,
        datastoreData.selectedNovelType,
        datastoreData.selectedPage,
        datastoreData.selectedScreen.ifEmpty { AppScreen.NovelList.name }
    )
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: novelAppState.selectedScreen
    )
    var downloadTarget by remember { mutableStateOf<Novel?>(null) }

    LaunchedEffect(uri) {
        uri?.let { uri ->
            when (uri.host) {
                NarouService.host -> NarouService
                KakuyomuService.host -> KakuyomuService
                else -> null
            }?.let { service ->
                service.getNovelId(uri)?.let {
                    downloadTarget = service.getNovelInfo(it)
                }
            }
        }
    }
    downloadTarget?.let {
        DownloadDialog(novel = it) {
            downloadTarget = null
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                search = { navController.navigate(AppScreen.SearchView.name) }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = novelAppState.selectedScreen,
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = AppScreen.NovelList.name) {
                novelAppState.changeSelectedScreen(AppScreen.NovelList)
                LaunchedEffect(novelAppState.selectedScreen) {
                    datastore.updateData { appState ->
                        appState.toBuilder()
                            .setSelectedScreen(AppScreen.NovelList.name)
                            .build()
                    }
                }
                val novels = remember {
                    Pager(
                        config = PagingConfig(
                            pageSize = ITEMS_PER_PAGE,
                            enablePlaceholders = false
                        ),
                        pagingSourceFactory = { db.novelDao().getPagingSource() }
                    ).flow
                }
                NovelScreen(novels.collectAsLazyPagingItems(), click = { novel ->
                    novelAppState.changeSelectedNovel(novel.novelId, novel.type)
                    navController.navigate(AppScreen.PageList.name)
                }, download = { novel ->
                    downloadTarget = novel
                }, delete = { novel ->
                    CoroutineScope(Dispatchers.IO).launch {
                        db.novelDao().delete(novel)
                    }
                })
            }
            composable(route = AppScreen.PageList.name) {
                novelAppState.changeSelectedScreen(AppScreen.PageList)
                LaunchedEffect(novelAppState.selectedScreen) {
                    datastore.updateData { appState ->
                        appState.toBuilder()
                            .setSelectedScreen(AppScreen.PageList.name)
                            .setSelectedNovelId(novelAppState.selectedNovelId)
                            .setSelectedNovelType(novelAppState.selectedNovelType)
                            .build()
                    }
                }

                PageScreen(
                    db,
                    novelAppState.selectedNovelId, novelAppState.selectedNovelType,
                    onBack = { navController.navigate(AppScreen.NovelList.name) },
                    click = { num ->
                        novelAppState.changeSelectedPage(num - 1)
                        navController.navigate(AppScreen.ContentView.name)
                    })
            }
            composable(route = AppScreen.ContentView.name) {
                novelAppState.changeSelectedScreen(AppScreen.ContentView)
                LaunchedEffect(novelAppState.selectedScreen) {
                    datastore.updateData { appState ->
                        appState.toBuilder()
                            .setSelectedScreen(AppScreen.ContentView.name)
                            .setSelectedPage(novelAppState.selectedPage)
                            .build()
                    }
                }
                ContentScreen(
                    db,
                    novelAppState.selectedNovelId, novelAppState.selectedNovelType,
                    novelAppState.selectedPage,
                    onBack = { navController.navigate(AppScreen.PageList.name) },
                )
            }
            composable(route = AppScreen.SearchView.name) {
                SearchScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}