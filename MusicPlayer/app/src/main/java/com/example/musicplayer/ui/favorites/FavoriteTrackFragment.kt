package com.example.musicplayer.ui.favorites

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.adapters.FavoriteTracksAdapter
import com.example.musicplayer.databinding.FragmentFavoriteTracksBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.music.Track
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.getStatusBarHeight
import com.example.musicplayer.utils.manipulateColor
import com.example.musicplayer.viewmodels.FavoritesViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import kotlin.math.abs

class FavoriteTrackFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoritesModel: FavoritesViewModel by activityViewModels()
    private val musicStore = MusicStore.getInstance()

    private val favoriteTracksAdapter: FavoriteTracksAdapter by lazy {
        FavoriteTracksAdapter(playerModel, favoritesModel, onItemClick = { playerModel.playTrack(it, QueueConstructor.FAVORITE_TRACKS.toInt()) })
    }

    val preferencesManager = PreferencesManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)

        binding.favoriteTracks.setPadding(0, getStatusBarHeight(requireContext()), 0, 0)

        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setPadding(0, getStatusBarHeight(requireContext()), 0, 0)
        }

        binding.shuffleFavoritesButton.setOnClickListener {
            playerModel.playFavorites(shuffled = true)
        }

        favoritesModel.allFavoriteTracksIds.observe(viewLifecycleOwner, { favoritesTracksIds ->
            if(favoritesTracksIds.isEmpty()){
                findNavController().navigateUp()
            }
            val favoritesTracksFromLoader = arrayListOf<Track>()

            favoritesTracksIds.forEach { favoriteId ->
                favoritesTracksFromLoader.add(musicStore.tracks.first{ it.id == favoriteId.musicId })
            }

            favoriteTracksAdapter.setData(favoritesTracksFromLoader.reversed())
            binding.tracksRecyclerView.apply {
                adapter = favoriteTracksAdapter
                setHasFixedSize(true)
            }
        })

        binding.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(abs(verticalOffset) - appBarLayout.totalScrollRange == 0){
                binding.toolbarTitle.alpha = 1f
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.aboveBackground))
            } else {
                binding.toolbarTitle.alpha = 0f
                binding.toolbar.background = null

                val offsetFactor = (abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange.toFloat())
                val scaleFactor = 1f - offsetFactor * 0.5f
                binding.favoriteTracks.scaleX = scaleFactor
                binding.favoriteTracks.scaleY = scaleFactor
                if(scaleFactor < 0.8f) {
                    binding.favoriteTracks.alpha = scaleFactor + 0.2f
                }

                binding.toolbarTitle.alpha = 0f
                binding.toolbar.background = null
            }
        })

        binding.appBarLayout.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
            ResourcesCompat.getColor(resources, R.color.accent, null),
            manipulateColor(ResourcesCompat.getColor(resources, R.color.accent, null), 0.6f),
            ResourcesCompat.getColor(resources, R.color.background, null)))

        return binding.root
    }
}