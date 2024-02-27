package it.hamy.muza.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.graphics.applyCanvas
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import it.hamy.muza.utils.thumbnail

context(Context)
class BitmapProvider(
    private val getBitmapSize: () -> Int,
    private val getColor: (isDark: Boolean) -> Int
) {
    var lastUri: Uri? = null
        private set

    var lastBitmap: Bitmap? = null
    private var lastIsSystemInDarkMode = false

    private var lastEnqueued: Disposable? = null

    private lateinit var defaultBitmap: Bitmap

    val bitmap: Bitmap
        get() = lastBitmap ?: defaultBitmap

    var listener: ((Bitmap?) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(lastBitmap)
        }

    init {
        setDefaultBitmap()
    }

    fun setDefaultBitmap(): Boolean {
        val isSystemInDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (::defaultBitmap.isInitialized && isSystemInDarkMode == lastIsSystemInDarkMode) return false

        lastIsSystemInDarkMode = isSystemInDarkMode

        val size = getBitmapSize()
        defaultBitmap = Bitmap.createBitmap(
            /* width = */ size,
            /* height = */ size,
            /* config = */ Bitmap.Config.ARGB_8888
        ).applyCanvas {
            drawColor(getColor(isSystemInDarkMode))
        }

        return lastBitmap == null
    }

    fun load(uri: Uri?, onDone: (Bitmap) -> Unit) {
        if (lastUri == uri) return

        lastEnqueued?.dispose()
        lastUri = uri

        lastEnqueued = applicationContext.imageLoader.enqueue(
            ImageRequest.Builder(applicationContext)
                .data(uri.thumbnail(getBitmapSize()))
                .allowHardware(false)
                .listener(
                    onError = { _, _ ->
                        lastBitmap = null
                        onDone(bitmap)
                        listener?.invoke(lastBitmap)
                    },
                    onSuccess = { _, result ->
                        lastBitmap = (result.drawable as BitmapDrawable).bitmap
                        onDone(bitmap)
                        listener?.invoke(lastBitmap)
                    }
                )
                .build()
        )
    }
}
