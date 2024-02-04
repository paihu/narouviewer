package dev.paihu.narou_viewer.data

import dev.paihu.narou_viewer.model.Novel

data class NovelState(
    val novels: List<Novel> = Datasource.loadNovels(),
    val selectedNovel: Int = 0,
    val selectedPage: Int = 0,
)