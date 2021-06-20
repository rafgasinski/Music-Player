package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Album
import com.example.musicplayer.viewmodels.PlayerViewModel

class AlbumsGridAdapter(private val playerModel: PlayerViewModel, private val onItemClick: (data: Album) -> Unit) : RecyclerView.Adapter<AlbumGridViewHolder>() {

    var albumsList: List<Album> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumGridViewHolder {
        return AlbumGridViewHolder.create(parent.context, playerModel, onItemClick)
    }

    override fun getItemCount(): Int {
        return albumsList.size
    }

    override fun onBindViewHolder(holder: AlbumGridViewHolder, position: Int) {
        holder.bind(albumsList[position])
    }

    fun setData(data: List<Album>) {
        albumsList = data
        notifyDataSetChanged()
    }
}