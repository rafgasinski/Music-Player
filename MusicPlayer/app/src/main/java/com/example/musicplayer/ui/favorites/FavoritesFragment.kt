package com.example.musicplayer.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentFavoritesBinding
import com.example.musicplayer.viewmodels.FavoriteTracksViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)

        binding.toolbar.title = getString(R.string.favorites)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("@string/tracks"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("@string/albums"))

        val adapter = FavoritesPagerAdapter(this, binding.tabLayout.tabCount)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position){
                0 -> tab.text = "Tracks"
                1 -> tab.text = "Albums"
            }
        }.attach()

        return binding.root
    }


}