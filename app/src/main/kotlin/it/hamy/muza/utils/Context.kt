package it.hamy.muza.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import it.hamy.muza.BuildConfig

inline fun <reified T> Context.intent(): Intent = Intent(this@Context, T::class.java)

inline fun <reified T : BroadcastReceiver> Context.broadcastPendingIntent(
    requestCode: Int = 0,
    flags: Int = if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0
): PendingIntent = PendingIntent.getBroadcast(this, requestCode, intent<T>(), flags)

inline fun <reified T : Activity> Context.activityPendingIntent(
    requestCode: Int = 0,
    flags: Int = 0,
    block: Intent.() -> Unit = {}
): PendingIntent = PendingIntent.getActivity(
    this,
    requestCode,
    intent<T>().apply(block),
    (if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0) or flags
)

val Context.isIgnoringBatteryOptimizations
    get() = !isAtLeastAndroid6 ||
            getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) ?: true

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun launchYouTubeMusic(
    context: Context,
    endpoint: String,
    tryWithoutBrowser: Boolean = true
): Boolean {
    return try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://music.youtube.com/${endpoint.dropWhile { it == '/' }}")
        ).apply {
            if (tryWithoutBrowser && isAtLeastAndroid11) {
                flags = Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
            }
        }
        intent.`package` =
            context.applicationContext.packageManager.queryIntentActivities(intent, 0)
                .firstOrNull {
                    it?.activityInfo?.packageName != null &&
                            BuildConfig.APPLICATION_ID !in it.activityInfo.packageName
                }?.activityInfo?.packageName
                ?: return false
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        if (tryWithoutBrowser) launchYouTubeMusic(
            context = context,
            endpoint = endpoint,
            tryWithoutBrowser = false
        ) else false
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("Should be called in the context of an Activity")
}

fun Context.hasPermission(permission: String) = ContextCompat.checkSelfPermission(
    applicationContext,
    permission
) == PackageManager.PERMISSION_GRANTED

operator fun Cursor.get(column: String): Int = getColumnIndexOrThrow(column)
