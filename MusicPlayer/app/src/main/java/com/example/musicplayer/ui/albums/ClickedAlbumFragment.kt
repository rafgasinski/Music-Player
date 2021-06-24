package com.example.musicplayer.ui.albums

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ClickedAlbumTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedAlbumBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.FavoriteAlbumsViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel
import kotlin.math.roundToInt


class ClickedAlbumFragment : Fragment() {
    private var _binding: FragmentClickedAlbumBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoriteAlbumsModel : FavoriteAlbumsViewModel by activityViewModels()

    private val args : ClickedAlbumFragmentArgs by navArgs()

    val preferencesManager = PreferencesManager.getInstance()

    private var isFavorite : Boolean = false

    private val clickedAlbumTracksAdapter: ClickedAlbumTracksAdapter by lazy {
        ClickedAlbumTracksAdapter(playerModel)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedAlbumBinding.inflate(inflater, container, false)

        val currentAlbum = MusicStore.getInstance().albums.find { it.id == args.albumId }!!

        binding.album = currentAlbum

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        favoriteAlbumsModel.albumExist(currentAlbum.id).observe(viewLifecycleOwner, { album ->
            if(album == null){
                isFavorite = false
                binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline)
            }
            else{
                isFavorite = true
                binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled)
            }

            preferencesManager.clickedHeartAlbumId = currentAlbum.id
        })

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addTracksListToFav -> {
                    if(isFavorite){
                        favoriteAlbumsModel.deleteAlbum(currentAlbum.id)
                        isFavorite = false
                        binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline)
                    }
                    else{
                        favoriteAlbumsModel.addAlbum(currentAlbum.id)
                        isFavorite = true
                        binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled)
                    }
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
            val splitArtistList = track.artistName.split(" feat. ", " & ", ", ")
            splitArtistList.forEach {
                if(!albumArtists.contains(it)){
                    albumArtists.add(it)
                }
            }
        }

        binding.albumArtists.text = resources.getString(R.string.album_by, albumArtists.toString().replace("[", "").replace("]", ""))

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

        preferencesManager.liveGradientColor.observe(viewLifecycleOwner, {
            binding.gradientView.alpha = 0f
            binding.gradientView.animate().setDuration(1150).alpha(1f).withStartAction {
                val gradient : GradientDrawable =
                    if(it != ResourcesCompat.getColor(resources, R.color.accent, null)){
                        GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.6f), manipulateColor(it, 0.5f),
                                manipulateColor(it, 0.4f), ResourcesCompat.getColor(resources, R.color.background, null))
                        )
                    } else {
                        GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.8f),
                                manipulateColor(it, 0.5f), ResourcesCompat.getColor(resources, R.color.background, null))
                        )
                    }

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