package com.example.musicplayer.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Artist

class ArtistsAdapter(private val onItemClick: (data: Artist) -> Unit) : RecyclerView.Adapter<ArtistsViewHolder>() {

    var artistList: List<Artist> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsViewHolder {
        return ArtistsViewHolder.create(parent.context, onItemClick)
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    override fun onBindViewHolder(holder: ArtistsViewHolder, position: Int) {
        holder.bind(artistList[position])
    }

    fun setData(data: List<Artist>) {
        artistList = data
        notifyDataSetChanged()
    }
}