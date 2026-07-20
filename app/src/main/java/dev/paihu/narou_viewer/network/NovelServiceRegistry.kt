package dev.paihu.narou_viewer.network

import android.net.Uri

object NovelServiceRegistry {
    val services = listOf(
        NarouService,
        KakuyomuService,
        AlphapolisService,
        Narou18Service
    )

    fun getServiceByHost(host: String?): SearchService? {
        if (host == null) return null
        return services.find { it.host == host }
    }

    fun getServiceByUri(uri: Uri): SearchService? {
        return getServiceByHost(uri.host)
    }

    fun getServiceByType(type: String): SearchService? {
        return services.find { it.type == type }
    }
}
