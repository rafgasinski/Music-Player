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

    var gradientAccentColor: Int
        get() = sharedPrefs.getInt(KEY_GRADIENT_ACCENT_COLOR, accentColor)

        set(value) {
            mLiveGradientColor.value = value
            sharedPrefs.edit {
                putInt(KEY_GRADIENT_ACCENT_COLOR, value)
                apply()
            }
        }

    var clickedHeartTrackId: Long
        get() = sharedPrefs.getLong(KEY_CLICKED_HEART_TRACK_ID, Long.MIN_VALUE)

        set(value) {
            mLiveClickedHeartTrackId.value = value
            sharedPrefs.edit {
                putLong(KEY_CLICKED_HEART_TRACK_ID, value)
                apply()
            }
        }

    var clickedHeartAlbumId: Long
        get() = sharedPrefs.getLong(KEY_CLICKED_HEART_ALBUM_ID, Long.MIN_VALUE)

        set(value) {
            mLiveClickedHeartAlbumId.value = value
            sharedPrefs.edit {
                putLong(KEY_CLICKED_HEART_ALBUM_ID, value)
                apply()
            }
        }

    private val mLiveGradientColor = MutableLiveData(gradientAccentColor)
    val liveGradientColor: LiveData<Int> get() = mLiveGradientColor

    private val mLiveClickedHeartTrackId = MutableLiveData(Long.MIN_VALUE)
    val liveClickedHeartTrackId: LiveData<Long> get() = mLiveClickedHeartTrackId

    private val mLiveClickedHeartAlbumId = MutableLiveData(Long.MIN_VALUE)
    val liveClickedHeartAlbumId: LiveData<Long> get() = mLiveClickedHeartAlbumId

    companion object {
        const val KEY_TRACK_QUEUE_CONSTRUCTOR_MODE = "KEY_TRACK_QUEUE_CONSTRUCTOR_MODE"
        const val KEY_ALBUMS_USE_GRID_LAYOUT = "KEY_ALBUMS_USE_GRID_LAYOUT"
        const val KEY_GRADIENT_ACCENT_COLOR = "KEY_GRADIENT_ACCENT_COLOR"
        const val KEY_CLICKED_HEART_TRACK_ID = "KEY_CLICKED_HEART_TRACK_ID"
        const val KEY_CLICKED_HEART_ALBUM_ID = "KEY_CLICKED_HEART_ALBUM_ID"

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