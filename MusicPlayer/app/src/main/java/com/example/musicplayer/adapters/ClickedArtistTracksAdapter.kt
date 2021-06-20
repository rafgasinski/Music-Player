package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Track

class ClickedArtistTracksAdapter(private val onItemClick: (data: Track) -> Unit,) : RecyclerView.Adapter<ClickedArtistTracksViewHolder>() {

    var tracksList: List<Track> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClickedArtistTracksViewHolder {
        return ClickedArtistTracksViewHolder.create(parent.context, onItemClick)
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun onBindViewHolder(holder: ClickedArtistTracksViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    fun setData(data: List<Track>) {
        tracksList = data
        notifyDataSetChanged()
    }
}