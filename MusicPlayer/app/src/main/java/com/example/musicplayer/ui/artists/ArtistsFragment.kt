package com.example.musicplayer.ui.artists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ArtistsAdapter
import com.example.musicplayer.databinding.FragmentArtistsBinding
import com.example.musicplayer.music.Artist
import com.example.musicplayer.music.MusicStore

class ArtistsFragment : Fragment() {

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!

    private val artistsAdapter: ArtistsAdapter by lazy {
        ArtistsAdapter(::navToClickedArtist)
    }

    private val musicStore = MusicStore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)

        binding.toolbar.title = getString(R.string.artists)

        binding.artistsRecyclerView.apply {
            adapter = artistsAdapter
            setHasFixedSize(true)

            artistsAdapter.setData(musicStore.artists)
        }

        return binding.root
    }


    private fun navToClickedArtist(artist: Artist) {
        findNavController().navigate(ArtistsFragmentDirections.actionArtistsFragmentToClickedArtistFragment(artist.id))
    }

}