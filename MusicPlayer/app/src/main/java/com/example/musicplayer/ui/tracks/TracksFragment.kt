package com.example.musicplayer.ui.tracks

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.musicplayer.R
import com.example.musicplayer.adapters.TracksAdapter
import com.example.musicplayer.databinding.FragmentTracksBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.isOnlyInAZ
import com.example.musicplayer.viewmodels.PlayerViewModel

class TracksFragment : Fragment() {

    private var _binding: FragmentTracksBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private val musicStore = MusicStore.getInstance()

    private val tracksAdapter: TracksAdapter by lazy {
        TracksAdapter(playerModel, onItemClick = { playerModel.playTrack(it, QueueConstructor.ALL_TRACKS.toInt()) })
    }

    private var menuItemSearch: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTracksBinding.inflate(inflater, container, false)

        binding.toolbar.apply {
            title = getString(R.string.all_tracks)
            inflateMenu(R.menu.menu_tracks_fragment)
            setOnMenuItemClickListener{
                when(it.itemId){
                    R.id.itemSearch -> {
                        menuItemSearch = it
                        searchView = menuItemSearch!!.actionView as SearchView

                        val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                        searchEditText?.setHint(R.string.search_in_tracks_list)

                        searchView!!.setOnQueryTextListener(
                            object : SearchView.OnQueryTextListener {
                                override fun onQueryTextSubmit(textInput: String?): Boolean {
                                    return false
                                }

                                override fun onQueryTextChange(textInput: String?): Boolean {
                                    tracksAdapter.filter.filter(textInput)

                                    if(textInput.isNullOrEmpty()) {
                                        binding.fastScroll.visibility = View.VISIBLE
                                    } else {
                                        binding.fastScroll.visibility = View.GONE
                                    }

                                    return true
                                }
                            }
                        )

                        true
                    }

                    R.id.itemShuffle -> {
                        playerModel.shuffleAll()
                        true
                    }
                    else -> false
                }
            }
        }

        binding.tracksRecyclerView.apply {
            adapter = tracksAdapter
            setHasFixedSize(true)

            tracksAdapter.setData(musicStore.tracks)
        }

        binding.fastScroll.setup(binding.tracksRecyclerView) { pos ->
            val char = musicStore.tracks[pos].name.first
            if (char.isDigit()) {
                '#'
            } else {
                if(isOnlyInAZ(char.toString())){
                    char
                } else {
                    null
                }
            }
        }

        return binding.root
    }

    private val String.first: Char get() {

        if (length > 5 && startsWith("the ", true)) {
            return get(4).uppercaseChar()
        }

        if (length > 3 && startsWith("a ", true)) {
            return get(2).uppercaseChar()
        }

        if (length > 4 && startsWith("an ", true)) {
            return get(3).uppercaseChar()
        }

        for (i  in 0..length){
            if (get(i).isLetterOrDigit()){
                return get(i).uppercaseChar()
            }
        }

        return get(0).uppercaseChar()

    }

}