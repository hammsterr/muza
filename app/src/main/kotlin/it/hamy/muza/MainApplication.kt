package it.hamy.muza

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import it.hamy.compose.persist.PersistMap
import it.hamy.muza.preferences.DataPreferences
import androidx.work.Configuration as WorkManagerConfiguration
import com.yandex.mobile.ads.common.MobileAds


class MainApplication : Application(), ImageLoaderFactory, WorkManagerConfiguration.Provider {
    override fun onCreate() {
        super.onCreate()
        Dependencies.init(this)
        DatabaseInitializer()
        MobileAds.initialize(this) {
            /**
             * Инициализация либы яндекса
             */
        }
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes(DataPreferences.coilDiskCacheMaxSize.bytes)
                .build()
        )
        .let { if (BuildConfig.DEBUG) it.logger(DebugLogger()) else it }
        .build()

    val persistMap = PersistMap()

    override val workManagerConfiguration = WorkManagerConfiguration.Builder()
        .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
        .build()
}
