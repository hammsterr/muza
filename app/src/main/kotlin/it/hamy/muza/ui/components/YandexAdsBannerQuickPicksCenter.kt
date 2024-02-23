package it.hamy.muza.ui.components

import android.os.CountDownTimer
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
fun YandexAdsBannerQuickPicksCenter(id: String) {
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


            val timer = object : CountDownTimer(4000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Здесь можно выполнить действия, которые нужно сделать каждую секунду
                }

                override fun onFinish() {
                    // Здесь вызывается метод loadAd(adRequest) после истечения таймера
                    loadAd(adRequest)
                    // Здесь можно повторить таймер, чтобы он всегда повторялся
                    //start()
                }
            }


            /**
             * Слушатель экшнов
             */
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    // Запускаем таймер
                    timer.start()
                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    /**
                     * Тут дебажим ошибки
                     */
                    loadAd(adRequest)
                }

                override fun onAdClicked() {

                }

                override fun onLeftApplication() {

                }

                override fun onReturnedToApplication() {
                    loadAd(adRequest)
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