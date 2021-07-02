package com.example.musicplayer.ui.albums

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
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
import com.example.musicplayer.adapters.ClickedAlbumTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedAlbumBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.getStatusBarHeight
import com.example.musicplayer.utils.manipulateColor
import com.example.musicplayer.viewmodels.FavoritesViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import kotlin.math.abs


class ClickedAlbumFragment : Fragment() {
    private var _binding: FragmentClickedAlbumBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoritesModel : FavoritesViewModel by activityViewModels()

    private val args : ClickedAlbumFragmentArgs by navArgs()

    val preferencesManager = PreferencesManager.getInstance()

    private var gradientTopColor : Int = 0
    private var isFavorite : Boolean = false

    private var gradientLoaded : Boolean = false
    private var gradientBackground : GradientDrawable? = null

    private val clickedAlbumTracksAdapter: ClickedAlbumTracksAdapter by lazy {
        ClickedAlbumTracksAdapter(playerModel)
    }

    override fun onPause() {
        super.onPause()
        gradientLoaded = true
    }

    override fun onResume() {
        super.onResume()
        binding.gradientView.background = gradientBackground
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedAlbumBinding.inflate(inflater, container, false)

        val currentAlbum = MusicStore.getInstance().albums.find { it.id == args.albumId }!!

        binding.album = currentAlbum

        clickedAlbumTracksAdapter.setData(currentAlbum.tracks)

        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            inflateMenu(R.menu.menu_clicked_album)
            setPadding(0, getStatusBarHeight(requireContext()), 0, 0)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.addTracksListToFav -> {
                        if(isFavorite) {
                            favoritesModel.deleteAlbum(currentAlbum.id)
                            isFavorite = false
                            binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline)
                        } else {
                            favoritesModel.addAlbum(currentAlbum.id)
                            isFavorite = true
                            binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled)
                        }
                        true
                    }

                    else -> false

                }
            }
        }

        binding.relativeLayoutClickedAlbum.setPadding(0, getStatusBarHeight(requireContext()), 0, 0)

        favoritesModel.allFavoriteAlbumsIds.observe(viewLifecycleOwner, { favoritesTracksIds ->
            isFavorite = if(favoritesTracksIds.any{ it.musicId == currentAlbum.id }) {
                binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled)
                true
            } else {
                binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline)
                false
            }
        })

        binding.clickedAlbumTracksRecyclerView.apply {
            adapter = clickedAlbumTracksAdapter
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
            playerModel.playAlbum(currentAlbum, true)
        }

        binding.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(abs(verticalOffset) - appBarLayout.totalScrollRange == 0){
                binding.toolbarTitle.alpha = 1f
                binding.toolbar.setBackgroundColor(gradientTopColor)
            } else if(abs(verticalOffset) == 0) {
                binding.albumCover.scaleX = 1f
                binding.albumCover.scaleY = 1f
                binding.albumCover.alpha = 1f
            } else {
                val offsetFactor = (abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange.toFloat())
                val scaleFactor = 1f - offsetFactor * 0.8f
                binding.albumCover.scaleX = scaleFactor
                binding.albumCover.scaleY = scaleFactor
                if(scaleFactor < 0.8f) {
                    binding.albumCover.alpha = scaleFactor + 0.2f
                }

                binding.toolbarTitle.alpha = 0f
                binding.toolbar.background = null
            }
        })

        preferencesManager.liveGradientColor.observe(viewLifecycleOwner, {
            if(!gradientLoaded) {
                val gradient : GradientDrawable =
                    if(it != ResourcesCompat.getColor(resources, R.color.accent, null)){
                        gradientTopColor = manipulateColor(it, 0.6f)
                        GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.6f), manipulateColor(it, 0.5f),
                                manipulateColor(it, 0.4f), ResourcesCompat.getColor(resources, R.color.background, null))
                        )
                    } else {
                        gradientTopColor = manipulateColor(it, 0.9f)
                        GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(manipulateColor(it, 0.9f),
                                manipulateColor(it, 0.6f), ResourcesCompat.getColor(resources, R.color.background, null))
                        )
                    }

                gradient.cornerRadius = 0f
                binding.gradientView.background = gradient
                gradientBackground = gradient
            }
        })

        return binding.root
    }

}