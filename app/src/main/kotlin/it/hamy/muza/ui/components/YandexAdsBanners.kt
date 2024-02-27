package it.hamy.muza.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.AdTheme
import com.yandex.mobile.ads.common.ImpressionData

@Composable
fun PlaylistAd(id: String) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        BannerAdView(context).apply {
            /**
             * ID блока рекламы
             */
            setAdUnitId(id)
            /**
             * Размер блока рекламы
             */
            setAdSize(BannerAdSize.inlineSize(context, 120, 120))
            /**
             * Билдер запроса
             */
            val adRequest = AdRequest.Builder()
                .setPreferredTheme(AdTheme.DARK)
                .build()
            /**
             * Слушатель экшнов
             */
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {

                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    /**
                     * Тут дебажим ошибки
                     */
                }

                override fun onAdClicked() {

                }

                override fun onLeftApplication() {

                }

                override fun onReturnedToApplication() {

                }

                override fun onImpression(p0: ImpressionData?) {

                }

            })
            /**
             * Запуск баннера
             */
            loadAd(adRequest)
        }
    })
}

@Composable
fun QuickpicksAd(id: String) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        BannerAdView(context).apply {
            /**
             * ID блока рекламы
             */
            setAdUnitId(id)
            /**
             * Размер блока рекламы
             */
            setAdSize(BannerAdSize.inlineSize(context, 260, 60))
            /**
             * Билдер запроса
             */
            val adRequest = AdRequest.Builder()
                .setPreferredTheme(AdTheme.DARK)
                .build()
            /**
             * Слушатель экшнов
             */
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {

                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    /**
                     * Тут дебажим ошибки
                     */
                }

                override fun onAdClicked() {

                }

                override fun onLeftApplication() {

                }

                override fun onReturnedToApplication() {

                }

                override fun onImpression(p0: ImpressionData?) {

                }

            })
            /**
             * Запуск баннера
             */
            loadAd(adRequest)
        }
    })
}

@Composable
fun NavigationAd(id: String) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        BannerAdView(context).apply {
            /**
             * ID блока рекламы
             */
            setAdUnitId(id)
            /**
             * Размер блока рекламы
             */
            setAdSize(BannerAdSize.inlineSize(context, 260, 60))
            /**
             * Билдер запроса
             */
            val adRequest = AdRequest.Builder()
                .setPreferredTheme(AdTheme.DARK)
                .build()
            /**
             * Слушатель экшнов
             */
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {

                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    /**
                     * Тут дебажим ошибки
                     */
                }

                override fun onAdClicked() {

                }

                override fun onLeftApplication() {

                }

                override fun onReturnedToApplication() {

                }

                override fun onImpression(p0: ImpressionData?) {

                }

            })
            /**
             * Запуск баннера
             */
            loadAd(adRequest)
        }
    })
}

