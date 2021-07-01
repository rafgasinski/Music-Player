package com.example.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.R
import com.example.musicplayer.player.state.QueueConstructor

class PreferencesManager private constructor(context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    private val accentColor : Int

    init {
        currentQueueConstructor(sharedPrefs)
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        accentColor = ResourcesCompat.getColor(context.resources, R.color.accent, null)
    }

    var queueConstructorMode: Int
        get() = sharedPrefs.getInt(KEY_TRACK_QUEUE_CONSTRUCTOR_MODE, QueueConstructor.CONST_ALL_TRACKS)

        set(value) {
            mLiveQueueConstructorMode.value = value
            sharedPrefs.edit {
                putInt(KEY_TRACK_QUEUE_CONSTRUCTOR_MODE, value)
                apply()
            }
        }

    var albumsUseGridLayout: Int
        get() = sharedPrefs.getInt(KEY_ALBUMS_USE_GRID_LAYOUT, 1)

        set(value) {
            sharedPrefs.edit {
                putInt(KEY_ALBUMS_USE_GRID_LAYOUT, value)
                apply()
            }
        }

    var favoritesUseGridLayout: Int
        get() = sharedPrefs.getInt(KEY_FAVORITES_USE_GRID_LAYOUT, 1)

        set(value) {
            sharedPrefs.edit {
                putInt(KEY_FAVORITES_USE_GRID_LAYOUT, value)
                apply()
            }
        }

    var gradientAccentColor: Int
        get() = sharedPrefs.getInt(KEY_GRADIENT_ACCENT_COLOR, accentColor)

        set(value) {
            mLiveGradientColor.value = value
            sharedPrefs.edit {
                putInt(KEY_GRADIENT_ACCENT_COLOR, value)
                apply()
            }
        }

    private val mLiveGradientColor = MutableLiveData(gradientAccentColor)
    val liveGradientColor: LiveData<Int> get() = mLiveGradientColor

    private val mLiveQueueConstructorMode = MutableLiveData(QueueConstructor.CONST_ALL_TRACKS)
    val liveQueueConstructorMode: LiveData<Int> get() = mLiveQueueConstructorMode

    companion object {
        const val KEY_TRACK_QUEUE_CONSTRUCTOR_MODE = "KEY_TRACK_QUEUE_CONSTRUCTOR_MODE"
        const val KEY_ALBUMS_USE_GRID_LAYOUT = "KEY_ALBUMS_USE_GRID_LAYOUT"
        const val KEY_FAVORITES_USE_GRID_LAYOUT = "KEY_FAVORITES_USE_GRID_LAYOUT"
        const val KEY_GRADIENT_ACCENT_COLOR = "KEY_GRADIENT_ACCENT_COLOR"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun init(context: Context): PreferencesManager {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = PreferencesManager(context)
                }
            }

            return getInstance()
        }

        fun getInstance(): PreferencesManager {
            val instance = INSTANCE

            if (instance != null) {
                return instance
            }

            error("PreferencesManager must be initialized first.")
        }
    }

    private fun currentQueueConstructor(prefs: SharedPreferences): Int {
        val mode = when (prefs.getInt(KEY_TRACK_QUEUE_CONSTRUCTOR_MODE, QueueConstructor.CONST_ALL_TRACKS)){
            QueueConstructor.CONST_IN_ARTIST -> QueueConstructor.IN_ARTIST
            QueueConstructor.CONST_IN_ALBUM -> QueueConstructor.IN_ALBUM
            QueueConstructor.CONST_ALL_TRACKS -> QueueConstructor.ALL_TRACKS

            else -> QueueConstructor.ALL_TRACKS
        }

        prefs.edit {
            putInt(KEY_TRACK_QUEUE_CONSTRUCTOR_MODE, mode.toInt())
            remove(KEY_TRACK_QUEUE_CONSTRUCTOR_MODE)
            apply()
        }

        return mode.toInt()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    interface Callback

}