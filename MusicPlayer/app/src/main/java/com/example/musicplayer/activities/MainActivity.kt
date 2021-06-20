package com.example.musicplayer.activities

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.player.service.MusicService
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.PlayerViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private lateinit var playerModel: PlayerViewModel

    override fun onStart() {
        super.onStart()

        startService(Intent(this, MusicService::class.java))
        onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MusicService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferencesManager.init(this)

        window.statusBarColor = ContextCompat.getColor(this, R.color.aboveBackground)
        volumeControlStream = AudioManager.STREAM_MUSIC

        playerModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        /**
         * Navigation setup
         * */
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

    }

    override fun onBackPressed() {
        if(!navController.navigateUp() && !super.onSupportNavigateUp()){
            super.onBackPressed();
        } else {
            navController.navigateUp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val action = intent.action
            val isConsumed = intent.getBooleanExtra(KEY_INTENT_USED, false)

            if (action == Intent.ACTION_VIEW && !isConsumed) {
                intent.putExtra(KEY_INTENT_USED, true)

                intent.data?.let { fileUri ->
                    playerModel.playWithUri(fileUri, this)
                }
            }
        }
    }

    companion object {
        private const val KEY_INTENT_USED = BuildConfig.APPLICATION_ID + ".key.FILE_INTENT_USED"
    }
}