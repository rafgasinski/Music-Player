package com.example.musicplayer.ui.albums

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ClickedAlbumTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedAlbumBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.PlayerViewModel
import kotlin.math.roundToInt


class ClickedAlbumFragment : Fragment() {
    private var _binding: FragmentClickedAlbumBinding? = null
    private val binding get() = _binding!!

    private lateinit var playerModel: PlayerViewModel

    private val args : ClickedAlbumFragmentArgs by navArgs()

    val preferencesManager = PreferencesManager.getInstance()

    private val clickedAlbumTracksAdapter: ClickedAlbumTracksAdapter by lazy {
        ClickedAlbumTracksAdapter(onItemClick = { playerModel.playTrack(it, QueueConstructor.IN_ALBUM.toInt()) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedAlbumBinding.inflate(inflater, container, false)
        playerModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        val currentAlbum = MusicStore.getInstance().albums.find { it.id == args.albumId }!!

        binding.album = currentAlbum

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addTracksListToFav -> {
                    // TO DO ADD FAVORITE MECHANISM
                    true
                }

                else -> false

            }
        }

        binding.clickedAlbumTracksRecyclerView.apply {
            adapter = clickedAlbumTracksAdapter
            setHasFixedSize(true)

            clickedAlbumTracksAdapter.setData(currentAlbum.tracks)
        }

        val albumArtists = arrayListOf<String>()

        currentAlbum.tracks.forEach { track ->
            val splitArtistList = track.artistName.split(" feat. ", " & ")
            splitArtistList.forEach {
                if(!albumArtists.contains(it)){
                    albumArtists.add(it)
                }
            }
        }

        binding.shuffleAlbumButton.setOnClickListener {
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

            playerModel.playAlbum(currentAlbum, true)
        }

        binding.albumArtists.text = resources.getString(R.string.album_by, albumArtists.toString().replace("[", "").replace("]", ""))

        preferencesManager.liveGradientColor.observe(viewLifecycleOwner, {
            binding.gradientView.alpha = 0f
            binding.gradientView.animate().setDuration(1150).alpha(1f).withStartAction {
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.5f),
                        manipulateColor(it, 0.3f), ResourcesCompat.getColor(resources, R.color.background, null))
                )

                gradient.cornerRadius = 0f

                binding.gradientView.background = gradient
            }
        })

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