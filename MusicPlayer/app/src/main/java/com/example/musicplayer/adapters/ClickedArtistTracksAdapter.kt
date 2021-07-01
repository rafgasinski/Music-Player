package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Track
import com.example.musicplayer.viewmodels.PlayerViewModel

class ClickedArtistTracksAdapter(private val playerModel: PlayerViewModel, private val onItemClick: (data: Track) -> Unit) : RecyclerView.Adapter<TracksViewHolder>() {

    var tracksList: List<Track> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TracksViewHolder {
        return TracksViewHolder.create(parent.context, playerModel, onItemClick)
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    fun setData(data: List<Track>) {
        tracksList = data
        notifyDataSetChanged()
    }
}