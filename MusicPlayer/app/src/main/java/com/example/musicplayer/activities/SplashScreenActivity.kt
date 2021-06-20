package com.example.musicplayer.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySplashBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.viewmodels.SplashScreenViewModel
import com.google.android.material.snackbar.Snackbar

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var viewModel: SplashScreenViewModel

    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(SplashScreenViewModel::class.java)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.background)

        viewModel.doShowGrantPromp.observe(this) { doShowGrantPromp ->
            if (doShowGrantPromp) {
                requestPermissions(permissions, 100)
                viewModel.doneWithGrantPrompt()
            }
        }

        viewModel.response.observe(this) { response ->
            when (response) {
                MusicStore.Response.SUCCESS -> {
                    val startAct = Intent(this, MainActivity::class.java)
                    startActivity(startAct)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    this.finish()
                }

                null -> {
                    binding.logo.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                }

                else -> showError(binding, response)
            }
        }

        if(!permissionGranted(this, permissions)){
            viewModel.notifyNoPermissions()
        }

        if (viewModel.response.value == null) {
            viewModel.load(this)
        }
    }

    private fun showError(binding: ActivitySplashBinding, error: MusicStore.Response) {
        binding.logo.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        when (error) {
            MusicStore.Response.NO_MUSIC -> {
                showSnackbar("No tracks found", "Search Again", false)
            }

            MusicStore.Response.NO_PERMS -> {
                showSnackbar("Player needs access to music files", "Grant", true)
            }

            MusicStore.Response.FAILED -> {
                showSnackbar("Music loading failed", "Retry", false)
            }

            else -> {}
        }
        binding.logo.alpha = 0f
        binding.logo.animate().setDuration(800).alpha(1f)

    }

    override fun onResume() {
        super.onResume()

        if (viewModel.loaded) {
            val startAct = Intent(this, MainActivity::class.java)
            startActivity(startAct)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            this.finish()
        }
    }

    private fun permissionGranted(context: Context, permissions: Array<String>): Boolean {
        var hasAllPermissions = true
        for (permission in permissions) {
            val res = context.checkSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }

    private fun showSnackbar(textInfo : String, actionInfo : String, requestPermissions : Boolean) {
        val snackBar = Snackbar.make(binding.root, textInfo, Snackbar.LENGTH_INDEFINITE)
            .setAction(actionInfo) {
                if(requestPermissions){
                    viewModel.showGrantingPrompt()
                } else {
                    viewModel.load(this)
                }
            }
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.accent));
        val sbView: View = snackBar.view
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.aboveBackground))
        sbView.elevation = 10f
        val tv = sbView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.load(this)
                } else {
                    showSnackbar("Player needs access to Music Files", "Grant", true)
                }
                return
            }

            else -> {
                Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

}