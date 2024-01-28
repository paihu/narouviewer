package dev.paihu.narou_viewer.data

import dev.paihu.narou_viewer.model.Novel

class Datasource {
    fun loadNovels(): List<Novel> {
        return (1..100).map {
            Novel("novel$it")
        }
    }
}