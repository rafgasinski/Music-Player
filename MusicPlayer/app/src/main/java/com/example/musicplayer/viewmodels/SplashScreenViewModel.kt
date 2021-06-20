package com.example.musicplayer.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.music.MusicStore
import kotlinx.coroutines.launch

class SplashScreenViewModel : ViewModel() {
    private val mResponse = MutableLiveData<MusicStore.Response?>(null)
    private var _doShowGrantPrompt = MutableLiveData(false)

    private var isBusy = false

    val response: LiveData<MusicStore.Response?> = mResponse
    val doShowGrantPromp: LiveData<Boolean> = _doShowGrantPrompt
    val loaded: Boolean get() = musicStore.musicAlreadyLoaded

    private val musicStore = MusicStore.getInstance()

    fun load(context: Context) {
        if (isBusy) return
        isBusy = true
        mResponse.value = null

        viewModelScope.launch {
            mResponse.value = musicStore.load(context)
            isBusy = false
        }
    }

    fun showGrantingPrompt() {
        _doShowGrantPrompt.value = true
    }

    fun doneWithGrantPrompt() {
        _doShowGrantPrompt.value = false
    }

    fun notifyNoPermissions() {
        mResponse.value = MusicStore.Response.NO_PERMS
    }
}