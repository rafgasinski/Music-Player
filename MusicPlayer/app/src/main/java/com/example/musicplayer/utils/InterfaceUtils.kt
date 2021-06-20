package com.example.musicplayer.utils

import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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