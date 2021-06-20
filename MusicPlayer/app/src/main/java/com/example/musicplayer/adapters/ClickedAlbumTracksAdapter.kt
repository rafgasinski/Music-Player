package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Track

class ClickedAlbumTracksAdapter(private val onItemClick: (data: Track) -> Unit,) : RecyclerView.Adapter<ClickedAlbumTracksViewHolder>() {

    var tracksList: List<Track> = listOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClickedAlbumTracksViewHolder {
        return ClickedAlbumTracksViewHolder.create(parent.context, onItemClick)
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