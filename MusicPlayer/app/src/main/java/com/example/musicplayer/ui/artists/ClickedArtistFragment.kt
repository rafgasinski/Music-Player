package com.example.musicplayer.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ClickedArtistTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedArtistBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager

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
        ClickedArtistTracksAdapter(playerModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedArtistBinding.inflate(inflater, container, false)

        val currentArtist = MusicStore.getInstance().artists.find { it.id == args.artistId }!!

        binding.artist = currentArtist

        binding.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                binding.artistName.alpha = 0f
                binding.toolbarTitle.animate().setDuration(400).alpha(1f).withStartAction {
                    activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)
                }
            } else {
                binding.toolbarTitle.alpha = 0f
                binding.artistName.animate().setDuration(400).alpha(1f).withStartAction {
                    activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.accent)
                }
            }
        })

        binding.toolbar.setNavigationOnClickListener {
            activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)
            findNavController().navigateUp()
        }

        binding.clickedArtistTracksRecyclerView.apply {
            adapter = clickedArtistAdapter
            setHasFixedSize(true)

            clickedArtistAdapter.setData(currentArtist.tracks)
        }

        binding.shuffleArtistButton.setOnClickListener {
            val animationClick = AnimationUtils.loadAnimation(inflater.context, R.anim.shuffle_button_clicked)
            it.startAnimation(animationClick)

            animationClick.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    val animationReset = AnimationUtils.loadAnimation(inflater.context, R.anim.shuffle_button_reset)
                    it.startAnimation(animationReset)
                }
            })

            playerModel.playArtist(currentArtist, true)
        }

        return binding.root
    }
}