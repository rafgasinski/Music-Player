package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Track

class FavoriteTracksAdapter(private val onItemClick: (data: Track) -> Unit) : RecyclerView.Adapter<TracksViewHolder>() {

    var tracksList: ArrayList<Track> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder {
        return TracksViewHolder.create(parent.context, onItemClick)
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    fun setData(data: ArrayList<Track>) {
        tracksList = data
        notifyDataSetChanged()
    }
}