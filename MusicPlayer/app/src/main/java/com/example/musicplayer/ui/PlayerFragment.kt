package com.example.musicplayer.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.music.MusicUtils
import com.example.musicplayer.player.state.LoopMode
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.OnSwipeListener
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.memberBinding
import com.example.musicplayer.viewmodels.FavoriteTracksViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel
import kotlin.math.roundToInt

class PlayerFragment : Fragment() {

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoriteTracksModel : FavoriteTracksViewModel by activityViewModels()

    private val binding by memberBinding(FragmentPlayerBinding::inflate) {
        trackTitle.isSelected = false
    }

    private var isFavorite : Boolean = false

    val preferencesManager = PreferencesManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.window?.statusBarColor = ContextCompat.getColor(inflater.context, R.color.background)

        binding.lifecycleOwner = this
        binding.playerModel = playerModel
        binding.executePendingBindings()

        binding.trackTitle.isSelected = true
        binding.trackArtist.isSelected = true

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
            activity?.window?.statusBarColor = ContextCompat.getColor(inflater.context, R.color.aboveBackground)
        }

        playerModel.track.observe(viewLifecycleOwner) { track ->
            if (track != null) {
                binding.track = track
                binding.seekBar.max = track.seconds.toInt()

                when(preferencesManager.queueConstructorMode) {
                    QueueConstructor.CONST_IN_ALBUM -> {
                        binding.playerPlayingFrom.text = getString(R.string.playing_from, "ALBUM")
                        binding.playerPlayingName.text = track.album.name
                    }

                    QueueConstructor.CONST_IN_ARTIST -> {
                        binding.playerPlayingFrom.text = getString(R.string.playing_tracks_by)
                        binding.playerPlayingName.text = track.artist.name
                    }

                    QueueConstructor.CONST_ALL_TRACKS -> {
                        binding.playerPlayingFrom.text = getString(R.string.playing_from, "")
                        binding.playerPlayingName.text = "All Tracks"
                    }

                    QueueConstructor.CONST_FAVORITE_TRACKS -> {
                        binding.playerPlayingFrom.text = getString(R.string.playing_from, "")
                        binding.playerPlayingName.text = "Favorite Tracks"
                    }

                    else -> {}
                }

                favoriteTracksModel.musicExist(track.id).observe(viewLifecycleOwner, { x ->
                    isFavorite = if(x != null){
                        binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_filled)
                        true
                    } else{
                        binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_outline)
                        false
                    }
                })
            }
        }

        playerModel.position.observe(viewLifecycleOwner) { position ->
            if( position != null){
                binding.currentDuration.text = MusicUtils.formatSongDuration(position)
            }
        }

        binding.viewOnImageView.setOnTouchListener(object: OnSwipeListener(inflater.context) {
            override fun onSwipeLeft() {
                val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_left_main_player)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {
                        binding.trackTitle.isSelected = false
                        binding.trackArtist.isSelected = false
                        playerModel.skipNext()
                    }
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        binding.trackTitle.isSelected = true
                        binding.trackArtist.isSelected = true
                    }
                })

                binding.trackCover.startAnimation(animation)
            }

            override fun onSwipeRight() {
                val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_right_main_player)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {
                        binding.trackTitle.isSelected = false
                        binding.trackArtist.isSelected = false
                        playerModel.skipPrev(true)
                    }
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        binding.trackTitle.isSelected = true
                        binding.trackArtist.isSelected = true
                    }
                })

                binding.trackCover.startAnimation(animation)
            }
        })

        binding.next.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_left_main_player)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {
                    binding.trackTitle.isSelected = false
                    binding.trackArtist.isSelected = false
                    playerModel.skipNext()
                }
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    binding.trackTitle.isSelected = true
                    binding.trackArtist.isSelected = true
                }
            })

            binding.trackCover.startAnimation(animation)
        }

        binding.previous.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(inflater.context, R.anim.on_swipe_right_main_player)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {
                        binding.trackTitle.isSelected = false
                        binding.trackArtist.isSelected = false
                        playerModel.skipPrev(true)
                    }
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        binding.trackTitle.isSelected = true
                        binding.trackArtist.isSelected = true
                    }
                })

            binding.trackCover.startAnimation(animation)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    playerModel.updatePositionDisplay(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playerModel.setSeekingStatus(true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playerModel.setSeekingStatus(false)
                playerModel.setPosition(seekBar!!.progress)
            }

        })

        playerModel.isShuffling.observe(viewLifecycleOwner) { isShuffling ->
            if (isShuffling) {
                binding.shuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
            } else {
                binding.shuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        playerModel.loopMode.observe(viewLifecycleOwner) { loopMode ->
            when (loopMode) {
                LoopMode.NONE -> {
                    binding.loop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                    binding.loop.setImageResource(R.drawable.ic_repeat)
                }

                LoopMode.ALL -> {
                    binding.loop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
                    binding.loop.setImageResource(R.drawable.ic_repeat)
                }

                LoopMode.TRACK -> {
                    binding.loop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
                    binding.loop.setImageResource(R.drawable.ic_repeat_track)
                }

                else -> return@observe
            }
        }

        playerModel.positionAsProgress.observe(viewLifecycleOwner) { position ->
            if (!playerModel.isSeeking.value!!) {
                binding.seekBar.progress = position
            }
        }

        playerModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if(isPlaying){
                binding.playPauseButton.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.playPauseButton.setImageResource(R.drawable.ic_play_circle)
            }
        }

        preferencesManager.liveGradientColor.observe(viewLifecycleOwner, {
            val gradient : GradientDrawable =
                if(it != ResourcesCompat.getColor(resources, R.color.accent, null)){
                    activity?.window?.statusBarColor = manipulateColor(it, 0.6f)
                    GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.6f), manipulateColor(it, 0.5f),
                            manipulateColor(it, 0.4f), ResourcesCompat.getColor(resources, R.color.background, null))
                    )
                } else {
                    activity?.window?.statusBarColor = manipulateColor(it, 0.8f)

                    GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.8f),
                            manipulateColor(it, 0.5f), ResourcesCompat.getColor(resources, R.color.background, null))
                    )
                }
            gradient.cornerRadius = 0f
            binding.gradientView.background = gradient
        })

        binding.favoriteCheckbox.setOnClickListener{
            isFavorite = if(isFavorite){
                playerModel.track.value?.let { track -> favoriteTracksModel.deleteMusic(track.id) }
                binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_outline)
                false
            } else{
                playerModel.track.value?.let { track -> favoriteTracksModel.addMusic(track.id) }
                binding.favoriteCheckbox.setImageResource(R.drawable.ic_heart_filled)
                true
            }

            preferencesManager.clickedHeartTrackId = playerModel.track.value?.id ?: Long.MIN_VALUE
        }


        return binding.root
    }

    private fun manipulateColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).roundToInt()
        val g = (Color.green(color) * factor).roundToInt()
        val b = (Color.blue(color) * factor).roundToInt()
        return Color.argb(
            a,
            r.coerceAtMost(255),
            g.coerceAtMost(255),
            b.coerceAtMost(255)
        )
    }

}