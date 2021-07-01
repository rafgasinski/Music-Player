package com.example.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentMainBinding
import com.example.musicplayer.viewmodels.PlayerViewModel

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var playerModel: PlayerViewModel

    private lateinit var navController: NavController

    private val TIME_INTERVAL = 1500
    private var mBackPressed: Long = 0
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        playerModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        toast = Toast.makeText(inflater.context, "Press back again to exit", Toast.LENGTH_SHORT)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.lifecycleOwner = this

        binding.bottomBar.setupWithNavController(navController)

        playerModel.track.observe(viewLifecycleOwner) { track ->
            if (track == null) {
                binding.smallPlayer.visibility = View.GONE
            } else {
                binding.smallPlayer.visibility = View.VISIBLE
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(binding.bottomBar.selectedItemId == R.id.tracksFragment){
                    if(!navController.navigateUp()){
                        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                            toast?.cancel()
                            activity?.moveTaskToBack(true)
                        }
                        else {
                            toast?.show()
                        }

                        mBackPressed = System.currentTimeMillis()
                    }
                } else {
                    toast?.cancel()
                    navController.navigateUp()
                }
            }
        })

        return binding.root
    }

}