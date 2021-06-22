package com.example.musicplayer.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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

    private var menuItemSearch: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)

        binding.toolbar.title = getString(R.string.artists)

        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)

        binding.artistsRecyclerView.apply {
            adapter = artistsAdapter
            setHasFixedSize(true)

            artistsAdapter.setData(musicStore.artists)
        }

        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.itemSearch -> {
                    menuItemSearch = it
                    searchView = menuItemSearch!!.actionView as SearchView

                    val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                    searchEditText?.setHint(R.string.search_in_artists_list)

                    searchView!!.setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(textInput: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(textInput: String?): Boolean {
                                artistsAdapter.filter.filter(textInput)

                                return true
                            }
                        }
                    )

                    true
                }

                else -> false
            }
        }

        return binding.root
    }


    private fun navToClickedArtist(artist: Artist) {
        findNavController().navigate(ArtistsFragmentDirections.actionArtistsFragmentToClickedArtistFragment(artist.id))
    }

}