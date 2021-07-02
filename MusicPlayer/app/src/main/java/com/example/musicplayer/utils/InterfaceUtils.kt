package com.example.musicplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Looper
import android.text.Layout
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt
import kotlin.reflect.KClass


val Context.inflater: LayoutInflater get() = LayoutInflater.from(this)

fun RecyclerView.canScroll() = computeVerticalScrollRange() > height

fun <T : Any> Context.getSystemServiceSafe(serviceClass: KClass<T>): T {
    return requireNotNull(ContextCompat.getSystemService(this, serviceClass.java)) {
        "System service ${serviceClass.simpleName} could not be instantiated"
    }
}

fun assertMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "Running on main thread"
    }
}

fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")

    if (resourceId > 0){
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun manipulateColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).roundToInt()
    val g = (Color.green(color) * factor).roundToInt()
    val b = (Color.blue(color) * factor).roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

fun isEllipsized(textView: TextView) : Boolean {
    val l: Layout = textView.layout
    val lines = l.lineCount
    if (lines > 0) {
        if (l.getEllipsisCount(lines - 1) > 0) {
            return true
        }
    }
    return false
}

fun getUriToDrawable(
    context: Context,
    @AnyRes drawableId: Int
): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.resources.getResourcePackageName(drawableId)
                + '/' + context.resources.getResourceTypeName(drawableId)
                + '/' + context.resources.getResourceEntryName(drawableId)
    )
}

fun isOnlyInAZ(string: String): Boolean {
    val regex = "^[a-zA-Z]*$".toRegex()
    return regex.matches(string)
}