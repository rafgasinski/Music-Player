package com.example.musicplayer.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.AlbumsGridAdapter
import com.example.musicplayer.adapters.AlbumsLinearAdapter
import com.example.musicplayer.databinding.FragmentAlbumsBinding
import com.example.musicplayer.music.Album
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.PlayerViewModel

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    val playerModel: PlayerViewModel by activityViewModels()
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
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)

        binding.toolbar.title = getString(R.string.albums)

        val albumsUseGridLayout = preferencesManager.albumsUseGridLayout

        albumsGridAdapter.setData(musicStore.albums)
        albumsLinearAdapter.setData(musicStore.albums)

        binding.albumsRecyclerView.apply {
            if(albumsUseGridLayout == 1) {
                setToGrid(false)
            } else {
                setToLinear(false)
            }
        }

        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.changeToLinear  -> {
                    preferencesManager.albumsUseGridLayout = 0
                    setToLinear(true)

                    true
                }

                R.id.changeToGrid -> {
                    preferencesManager.albumsUseGridLayout = 1
                    setToGrid(true)

                    true
                }

                else -> false
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
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.menu_albums_linear)
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
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.menu_albums_grid)
    }

    private fun navToClickedAlbum(album: Album) {
        findNavController().navigate(AlbumsFragmentDirections.actionAlbumsFragmentToFragmentClickedAlbum(album.id))
    }


}