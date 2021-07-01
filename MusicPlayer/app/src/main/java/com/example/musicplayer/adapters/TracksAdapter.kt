package com.example.musicplayer.adapters

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.music.Track
import com.example.musicplayer.viewmodels.PlayerViewModel
import java.util.*

class TracksAdapter(private val playerModel: PlayerViewModel, private val onItemClick: (data: Track) -> Unit) : RecyclerView.Adapter<TracksViewHolder>(), Filterable {

    var tracksAll : List<Track> = listOf()
    var tracksSearched: List<Track> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder {
        return TracksViewHolder.create(parent.context, playerModel, onItemClick)
    }

    override fun getItemCount(): Int {
        return tracksSearched.size
    }

    override fun getFilter(): Filter {
        return filterSearch
    }

    private var filterSearch  = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList : MutableList<Track> = mutableListOf()
            if(constraint.toString().isEmpty()){
                filteredList.addAll(tracksAll)
            } else {
                for (track in tracksAll) {
                    if(track.name.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault()))
                        || track.artistName.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault()))) {
                        filteredList.add(track)
                    }
                }
            }

            val filterResults = FilterResults()
            filterResults.values = filteredList

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val resultsList = results?.values as List<Track>
            if(resultsList.count() > 0){
                tracksSearched = arrayListOf()
                tracksSearched = resultsList
                notifyDataSetChanged()
            }
        }

    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        holder.bind(tracksSearched[position])
    }

    fun setData(data: List<Track>) {
        tracksAll = data
        tracksSearched = data
        notifyDataSetChanged()
    }
}