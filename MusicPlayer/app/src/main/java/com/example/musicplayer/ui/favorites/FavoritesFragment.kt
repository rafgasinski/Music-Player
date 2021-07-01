package com.example.musicplayer.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.AlbumsGridAdapter
import com.example.musicplayer.adapters.AlbumsLinearAdapter
import com.example.musicplayer.databinding.FragmentFavoritesBinding
import com.example.musicplayer.music.Album
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.utils.getUriToDrawable
import com.example.musicplayer.viewmodels.FavoritesViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()

    private val favoritesModel: FavoritesViewModel by activityViewModels()

    private val musicStore = MusicStore.getInstance()

    private val favoritesGridAdapter: AlbumsGridAdapter by lazy {
        AlbumsGridAdapter(playerModel, ::navToClickedItem)
    }

    private val favoritesLinearAdapter: AlbumsLinearAdapter by lazy {
        AlbumsLinearAdapter(playerModel, ::navToClickedItem)
    }

    private val preferencesManager = PreferencesManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        binding.toolbar.apply {
            title = getString(R.string.favorites)
            inflateMenu(R.menu.menu_layout_switch)
            setOnMenuItemClickListener{
                when(it.itemId){
                    R.id.changeLayout  -> {
                        if(preferencesManager.favoritesUseGridLayout == 1) {
                            preferencesManager.favoritesUseGridLayout = 0
                            setToLinear(true)
                        } else {
                            preferencesManager.favoritesUseGridLayout = 1
                            setToGrid(true)
                        }

                        true
                    }

                    else -> false
                }
            }
        }

        favoritesModel.allFavoriteIds.observe(viewLifecycleOwner, { favoritesIds ->
            var trackCount = 0
            val favoritesAlbumsFromLoader = mutableListOf<Album>()

            favoritesIds.forEach { favoriteId ->
                if(favoriteId.isTrack){
                    trackCount++
                } else {
                    favoritesAlbumsFromLoader.add(musicStore.albums.first{ it.id == favoriteId.musicId })
                }
            }

            if(trackCount != 0) {
                val textCount = if(trackCount != 1){
                    requireContext().resources.getString(R.string.tracks_count, trackCount)
                } else {
                    requireContext().resources.getString(R.string.one_track, trackCount)
                }

                val favoriteTracksAlbum = Album(Long.MAX_VALUE, getString(R.string.favorite_tracks), textCount, getUriToDrawable(requireContext(), R.drawable.ic_favorites_playlist))
                favoritesAlbumsFromLoader.add(favoriteTracksAlbum)
            }

            if(favoritesAlbumsFromLoader.isEmpty()) {
                binding.noFavoritesInfo.animate().setDuration(300).alpha(1f)
                binding.favoritesRecyclerView.animate().setDuration(300).alpha(0f)
            } else if(favoritesAlbumsFromLoader.count() == 1) {
                binding.favoritesRecyclerView.animate().setDuration(300).alpha(1f)
                binding.noFavoritesInfo.animate().setDuration(300).alpha(0f)
            }

            favoritesGridAdapter.setData(favoritesAlbumsFromLoader.reversed())
            favoritesLinearAdapter.setData(favoritesAlbumsFromLoader.reversed())
        })

        val albumsUseGridLayout = preferencesManager.favoritesUseGridLayout

        binding.favoritesRecyclerView.apply {
            if(albumsUseGridLayout == 1) {
                setToGrid(false)
            } else {
                setToLinear(false)
            }
        }

        return binding.root
    }

    private fun setToGrid(animate : Boolean) {
        val scale = resources.displayMetrics.density

        if(animate){
            val animation = AnimationUtils.loadAnimation(binding.favoritesRecyclerView.context, R.anim.fade_in)
            binding.favoritesRecyclerView.startAnimation(animation)
        }

        binding.favoritesRecyclerView.apply {
            adapter = favoritesGridAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setPadding((6 * scale + 0.5f).toInt(), (14 * scale + 0.5f).toInt(), (6 * scale + 0.5f).toInt(), 0)
        }
    }

    private fun setToLinear(animate : Boolean) {
        val scale = resources.displayMetrics.density

        if(animate){
            val animation = AnimationUtils.loadAnimation(binding.favoritesRecyclerView.context, R.anim.fade_in)
            binding.favoritesRecyclerView.startAnimation(animation)
        }

        binding.favoritesRecyclerView.apply {
            adapter = favoritesLinearAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setPadding(0, (8 * scale + 0.5f).toInt(), 0, (8 * scale + 0.5f).toInt())
        }
    }

    private fun navToClickedItem(album: Album) {
        if(album.id == Long.MAX_VALUE){
            findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToFavoriteTrackFragment())
        } else {
            findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToFragmentClickedAlbum(album.id))
        }
    }


}