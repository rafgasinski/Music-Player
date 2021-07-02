package com.example.musicplayer.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        binding.toolbar.apply {
            title = getString(R.string.albums)
            setOnMenuItemClickListener{
                when(it.itemId){
                    R.id.changeLayout  -> {
                        if(preferencesManager.albumsUseGridLayout == 1) {
                            preferencesManager.albumsUseGridLayout = 0
                            setToLinear(true)
                        } else {
                            preferencesManager.albumsUseGridLayout = 1
                            setToGrid(true)
                        }

                        true
                    }

                    else -> false
                }
            }
        }

        albumsGridAdapter.setData(musicStore.albums)
        albumsLinearAdapter.setData(musicStore.albums)

        if(preferencesManager.albumsUseGridLayout == 1) {
            setToGrid(false)
        } else {
            setToLinear(false)
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
            setPadding((6 * scale + 0.5f).toInt(), (14 * scale + 0.5f).toInt(), (6 * scale + 0.5f).toInt(), 0)
        }

        binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_view)
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
            setPadding(0, (10 * scale + 0.5f).toInt(), 0, (10 * scale + 0.5f).toInt())
        }

        binding.toolbar.menu.getItem(0).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid_view)
    }

    private fun navToClickedAlbum(album: Album) {
        findNavController().navigate(AlbumsFragmentDirections.actionAlbumsFragmentToFragmentClickedAlbum(album.id))
    }

}