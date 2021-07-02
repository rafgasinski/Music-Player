package com.example.musicplayer.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentSmallPlayerBinding
import com.example.musicplayer.utils.OnSwipeListener
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.FavoritesViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel


class SmallPlayerFragment : Fragment() {

    private var _binding: FragmentSmallPlayerBinding? = null
    private val binding get() = _binding!!
    private var isFavorite : Boolean = false

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoritesModel: FavoritesViewModel by activityViewModels()

    val preferencesManager = PreferencesManager.getInstance()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSmallPlayerBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.playerModel = playerModel
        binding.executePendingBindings()

        binding.root.setOnTouchListener(object: OnSwipeListener(inflater.context) {
            override fun onSwipeLeft() {
                val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_left_small_player)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {
                        binding.smallPlayerTitle.isSelected = false
                        binding.smallPlayerArtist.isSelected = false
                        playerModel.skipNext()
                    }
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        binding.smallPlayerTitle.isSelected = true
                        binding.smallPlayerArtist.isSelected = true
                    }
                })
                binding.smallPlayerTitle.startAnimation(animation)
                binding.smallPlayerArtist.startAnimation(animation)
            }

            override fun onSwipeRight() {
                val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_right_small_player)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {
                        binding.smallPlayerTitle.isSelected = false
                        binding.smallPlayerArtist.isSelected = false
                        playerModel.skipPrev(false)
                    }
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        binding.smallPlayerTitle.isSelected = true
                        binding.smallPlayerArtist.isSelected = true
                    }
                })
                binding.smallPlayerTitle.startAnimation(animation)
                binding.smallPlayerArtist.startAnimation(animation)
            }

            override fun onClick() {
                super.onClick()

                findNavController().navigate(MainFragmentDirections.actionMainFragmentToPlayerFragment())
            }
        })

        binding.smallPlayerTitle.isSelected = true
        binding.smallPlayerArtist.isSelected = true

        playerModel.track.observe(viewLifecycleOwner) { track ->
            if (track != null) {
                binding.track = track
                binding.smallPlayerProgress.max = track.seconds.toInt()

                favoritesModel.allFavoriteTracksIds.observe(viewLifecycleOwner, { favoritesTracksIds ->
                    isFavorite = if(favoritesTracksIds.any{ it.musicId == track.id }) {
                        binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_filled)
                        true
                    } else {
                        binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_outline)
                        false
                    }
                })
            }
        }

        playerModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                binding.smallPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
            } else {
                binding.smallPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
            }
        }

        binding.favoriteCheckbox.setOnClickListener{
            isFavorite = if(isFavorite){
                binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_outline)
                it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                false
            } else {
                binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_filled)
                it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_down))
                true
            }

            favoritesModel.setFavorite(isFavorite)
        }

        binding.smallPlayerPlayPauseButton.setOnTouchListener { view, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.scaleX = 0.98f
                    view.scaleY = 0.98f
                    view.alpha = 0.6f
                    true
                }

                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.alpha = 1f
                    true
                }

                else -> false
            }
        }


        return binding.root

    }
}