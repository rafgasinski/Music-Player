package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.adapters.TracksViewHolder
import com.example.musicplayer.music.Track
import com.example.musicplayer.viewmodels.FavoritesViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel

class FavoriteTracksAdapter(private val playerModel: PlayerViewModel, private val favoriteModel: FavoritesViewModel, private val onItemClick: (data: Track) -> Unit) : RecyclerView.Adapter<FavoriteTracksViewHolder>() {

    var tracksList: List<Track> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteTracksViewHolder {
        return FavoriteTracksViewHolder.create(parent.context, playerModel, favoriteModel, onItemClick)
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun onBindViewHolder(holder: FavoriteTracksViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    fun setData(data: List<Track>) {
        tracksList = data
        notifyDataSetChanged()
    }
}