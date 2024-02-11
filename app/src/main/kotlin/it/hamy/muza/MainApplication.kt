package it.hamy.muza

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.yandex.mobile.ads.common.MobileAds
import it.hamy.muza.enums.CoilDiskCacheMaxSize
import it.hamy.muza.utils.coilDiskCacheMaxSizeKey
import it.hamy.muza.utils.getEnum
import it.hamy.muza.utils.preferences

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()
        MobileAds.initialize(this) {
            /**
             * Инициализация либы яндекса
             */
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .respectCacheHeaders(false)
            .diskCache(
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }
}
