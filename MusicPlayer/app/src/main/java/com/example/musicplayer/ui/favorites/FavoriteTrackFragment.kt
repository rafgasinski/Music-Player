package com.example.musicplayer.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.adapters.FavoriteTracksAdapter
import com.example.musicplayer.databinding.FragmentFavoritesTracksBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.music.Track
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.FavoriteTracksViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel

class FavoriteTrackFragment : Fragment() {

    private var _binding: FragmentFavoritesTracksBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private lateinit var favoriteTracksModel : FavoriteTracksViewModel
    private val musicStore = MusicStore.getInstance()

    private val favoriteTracksAdapter: FavoriteTracksAdapter by lazy {
        FavoriteTracksAdapter(onItemClick = { playerModel.playTrack(it, QueueConstructor.FAVORITE_TRACKS.toInt()) })
    }

    val preferencesManager = PreferencesManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesTracksBinding.inflate(inflater, container, false)

        favoriteTracksModel = ViewModelProvider(this).get(FavoriteTracksViewModel::class.java)

        favoriteTracksModel.allFavoriteTracksId.observe(viewLifecycleOwner, { favoritesTracksIds ->
            val favoritesTracksFromLoader = arrayListOf<Track>()
            musicStore.tracks.forEach { track ->
                if(favoritesTracksIds.any{it.trackId == track.id}){
                    favoritesTracksFromLoader.add(track)
                }
            }

            binding.tracksRecyclerView.apply {
                adapter = favoriteTracksAdapter
                setHasFixedSize(true)

                favoriteTracksAdapter.setData(favoritesTracksFromLoader)
            }
        })

        return binding.root
    }
}