package com.example.musicplayer.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.AlbumsGridAdapter
import com.example.musicplayer.adapters.AlbumsLinearAdapter
import com.example.musicplayer.databinding.FragmentFavoriteAlbumsBinding
import com.example.musicplayer.music.Album
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.music.Track
import com.example.musicplayer.ui.albums.AlbumsFragmentDirections
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.FavoriteAlbumsViewModel
import com.example.musicplayer.viewmodels.FavoriteTracksViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel

class FavoriteAlbumsFragment : Fragment() {

    private var _binding: FragmentFavoriteAlbumsBinding? = null
    private val binding get() = _binding!!

    private val playerModel: PlayerViewModel by activityViewModels()
    private val favoriteAlbumsModel : FavoriteAlbumsViewModel by activityViewModels()
    private val musicStore = MusicStore.getInstance()

    private val albumsGridAdapter: AlbumsGridAdapter by lazy {
        AlbumsGridAdapter(playerModel, ::navToClickedAlbum)
    }

    private val albumsLinearAdapter: AlbumsLinearAdapter by lazy {
        AlbumsLinearAdapter(playerModel, ::navToClickedAlbum)
    }

    private val preferencesManager = PreferencesManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteAlbumsBinding.inflate(inflater, container, false)

        favoriteAlbumsModel.allFavoriteAlbumsId.observe(viewLifecycleOwner, { favoritesTracksIds ->
            val favoritesAlbumsFromLoader = arrayListOf<Album>()
            musicStore.albums.forEach { album ->
                if(favoritesTracksIds.any{it.albumId == album.id}){
                    favoritesAlbumsFromLoader.add(album)
                }
            }

            albumsGridAdapter.setData(favoritesAlbumsFromLoader)
            albumsLinearAdapter.setData(favoritesAlbumsFromLoader)
        })

        val albumsUseGridLayout = preferencesManager.albumsUseGridLayout

        binding.albumsRecyclerView.apply {
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
            val animation = AnimationUtils.loadAnimation(binding.albumsRecyclerView.context, R.anim.fade_in)
            binding.albumsRecyclerView.startAnimation(animation)
        }

        binding.albumsRecyclerView.apply {
            adapter = albumsGridAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        binding.albumsRecyclerView.setPadding((7 * scale + 0.5f).toInt(), (10 * scale + 0.5f).toInt(), (7 * scale + 0.5f).toInt(), 0)
    }

    private fun setToLinear(animate : Boolean) {
        val scale = resources.displayMetrics.density

        if(animate){
            val animation = AnimationUtils.loadAnimation(binding.albumsRecyclerView.context, R.anim.fade_in)
            binding.albumsRecyclerView.startAnimation(animation)
        }

        binding.albumsRecyclerView.apply {
            adapter = albumsLinearAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.albumsRecyclerView.setPadding(0, (8 * scale + 0.5f).toInt(), 0, (8 * scale + 0.5f).toInt())
    }

    private fun navToClickedAlbum(album: Album) {
        findNavController().navigate(FavoritesFragmentDirections.actionFavoritesFragmentToFragmentClickedAlbum(album.id))
    }
}