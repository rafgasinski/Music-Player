package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.adapters.ClickedAlbumTracksViewHolder
import com.example.musicplayer.music.Track
import com.example.musicplayer.viewmodels.PlayerViewModel

class ClickedAlbumTracksAdapter(private val playerModel: PlayerViewModel) : RecyclerView.Adapter<ClickedAlbumTracksViewHolder>() {

    var tracksList: List<Track> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClickedAlbumTracksViewHolder {
        return ClickedAlbumTracksViewHolder.create(parent.context, playerModel)
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun onBindViewHolder(holder: ClickedAlbumTracksViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    fun setData(data: List<Track>) {
        tracksList = data
        notifyDataSetChanged()
    }
}