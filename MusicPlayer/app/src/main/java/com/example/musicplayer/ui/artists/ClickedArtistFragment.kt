package com.example.musicplayer.ui.artists

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
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ClickedArtistTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedArtistBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.getStatusBarHeight
import com.example.musicplayer.utils.manipulateColor
import com.example.musicplayer.viewmodels.PlayerViewModel
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import kotlin.math.abs


class ClickedArtistFragment : Fragment() {
    private var _binding: FragmentClickedArtistBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()

    private val args : ClickedArtistFragmentArgs by navArgs()

    val preferencesManager = PreferencesManager.getInstance()

    private val clickedArtistAdapter: ClickedArtistTracksAdapter by lazy {
        ClickedArtistTracksAdapter(playerModel, onItemClick = { playerModel.playTrack(it, QueueConstructor.IN_ARTIST.toInt()) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedArtistBinding.inflate(inflater, container, false)

        val currentArtist = MusicStore.getInstance().artists.find { it.id == args.artistId }!!

        binding.artist = currentArtist

        binding.appBarLayout.apply {
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                ResourcesCompat.getColor(resources, R.color.accent, null),
                manipulateColor(ResourcesCompat.getColor(resources, R.color.accent, null), 0.6f),
                ResourcesCompat.getColor(resources, R.color.background, null)))

            addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if(abs(verticalOffset) - appBarLayout.totalScrollRange == 0){
                    binding.toolbarTitle.alpha = 1f
                    binding.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.aboveBackground))
                } else if(abs(verticalOffset) == 0) {
                    binding.artistName.scaleX = 1f
                    binding.artistName.scaleY = 1f
                    binding.artistName.alpha = 1f
                } else {
                    binding.toolbarTitle.alpha = 0f
                    binding.toolbar.background = null

                    val offsetFactor = (abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange.toFloat())
                    val scaleFactor = 1f - offsetFactor * 0.5f
                    binding.artistName.scaleX = scaleFactor
                    binding.artistName.scaleY = scaleFactor
                    if(scaleFactor < 0.8f) {
                        binding.artistName.alpha = scaleFactor + 0.2f
                    }

                    binding.toolbarTitle.alpha = 0f
                    binding.toolbar.background = null
                }
            })
        }

        binding.artistName.setPadding(0, getStatusBarHeight(requireContext()), 0, 0)

        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setPadding(0, getStatusBarHeight(requireContext()), 0, 0)
        }

        binding.clickedArtistTracksRecyclerView.apply {
            adapter = clickedArtistAdapter
            setHasFixedSize(true)

            clickedArtistAdapter.setData(currentArtist.tracks)
        }

        binding.shuffleArtistButton.setOnClickListener {
            playerModel.playArtist(currentArtist, true)
        }

        return binding.root
    }
}