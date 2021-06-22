package com.example.musicplayer.adapters

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Artist
import com.example.musicplayer.music.Track
import java.util.*

class ArtistsAdapter(private val onItemClick: (data: Artist) -> Unit) : RecyclerView.Adapter<ArtistsViewHolder>(), Filterable {

    var artistAll: List<Artist> = listOf()
    var artistSearched: List<Artist> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsViewHolder {
        return ArtistsViewHolder.create(parent.context, onItemClick)
    }

    override fun getItemCount(): Int {
        return artistSearched.size
    }

    override fun getFilter(): Filter {
        return filterSearch
    }

    override fun onBindViewHolder(holder: ArtistsViewHolder, position: Int) {
        holder.bind(artistSearched[position])
    }

    private var filterSearch  = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList : MutableList<Artist> = mutableListOf()
            if(constraint.toString().isEmpty()){
                filteredList.addAll(artistAll)
            } else {
                for (artist in artistAll) {
                    if(artist.name.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault()))) {
                        filteredList.add(artist)
                    }
                }
            }

            val filterResults = FilterResults()
            filterResults.values = filteredList

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val resultsList = results?.values as List<Artist>
            if(resultsList.count() > 0){
                artistSearched = arrayListOf()
                artistSearched = resultsList
                notifyDataSetChanged()
            }
        }

    }

    fun setData(data: List<Artist>) {
        artistAll = data
        artistSearched = data
        notifyDataSetChanged()
    }
}