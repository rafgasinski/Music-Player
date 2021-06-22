package com.example.musicplayer.ui.favorites

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FavoritesPagerAdapter(fragment: Fragment, var totalTabs : Int) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return totalTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> FavoriteTrackFragment()
            1 -> FavoriteAlbumsFragment()
            else -> FavoriteTrackFragment()
        }
    }

}