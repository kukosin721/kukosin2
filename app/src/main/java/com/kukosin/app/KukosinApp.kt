package com.kukosin.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.kukosin.app.data.LibraryInsights
import com.kukosin.app.data.artwork.EmbeddedArtwork
import com.kukosin.app.ui.theme.AccentTheme

/**
 * Cargador de imágenes propio.
 *
 * Solo agrega un [coil.fetch.Fetcher] para el esquema `kukosin-cover://`, que devuelve la
 * carátula embebida en el archivo de audio a resolución completa. El resto de las URLs
 * (miniaturas de YouTube Music, etc.) sigue usando los fetchers por defecto de Coil.
 */
class KukosinApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Color de acento guardado por el usuario, antes de que se dibuje cualquier pantalla.
        AccentTheme.init(this)
        LibraryInsights.init(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(EmbeddedArtwork.FetcherFactory(this@KukosinApp)) }
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.20)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(64L * 1024 * 1024)
                .build()
        }
        .crossfade(true)
        .build()
}
