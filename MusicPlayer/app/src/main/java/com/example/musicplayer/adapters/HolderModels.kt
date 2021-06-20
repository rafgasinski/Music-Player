package com.example.musicplayer.adapters

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.*
import com.example.musicplayer.music.Album
import com.example.musicplayer.music.Artist
import com.example.musicplayer.music.BaseModel
import com.example.musicplayer.music.Track
import com.example.musicplayer.utils.inflater
import com.example.musicplayer.viewmodels.PlayerViewModel

abstract class BaseHolder<T : BaseModel>(
    private val binding: ViewDataBinding,
    private val onItemClick: ((data: T) -> Unit)? = null,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    fun bind(data: T) {
        onItemClick?.let { onClick ->
            binding.root.setOnClickListener {
                onClick(data)
            }
        }

        onBind(data)
        binding.executePendingBindings()
    }

    protected abstract fun onBind(data: T)
}

class TracksViewHolder private constructor(
    private val binding: ItemTrackBinding,
    onItemClick: (data: Track) -> Unit
) : BaseHolder<Track>(binding, onItemClick) {

    override fun onBind(data: Track) {
        binding.track = data

        binding.trackTitle.requestLayout()
        binding.trackArtist.requestLayout()
    }

    companion object {
        fun create(
            context: Context,
            onItemClick: (data: Track) -> Unit
        ): TracksViewHolder {
            return TracksViewHolder(
                ItemTrackBinding.inflate(context.inflater),
                onItemClick
            )
        }
    }
}

class AlbumGridViewHolder private constructor(
    private val binding: ItemAlbumGridBinding,
    private val playerModel: PlayerViewModel,
    onItemClick: (data: Album) -> Unit
) : BaseHolder<Album>(binding, onItemClick) {

    override fun onBind(data: Album) {
        binding.album = data
        binding.albumName.requestLayout()
        binding.albumArtist.requestLayout()

        playerModel.currentPlayingAlbum.observe(itemView.context as LifecycleOwner, Observer {
            if(it == data){
                binding.playAlbum.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.playAlbum.setImageResource(R.drawable.ic_play_circle)
            }
        })

        playerModel.isPlaying.observe(itemView.context as LifecycleOwner, Observer { isPlaying ->
            if(isPlaying && playerModel.currentPlayingAlbum.value == data){
                binding.playAlbum.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.playAlbum.setImageResource(R.drawable.ic_play_circle)
            }
        })

        binding.playAlbum.setOnClickListener {
            if(playerModel.currentPlayingAlbum.value == data){
                playerModel.invertPlayingStatus()
            } else {
                playerModel.playAlbum(data, false)
            }
        }

    }

    companion object {
        fun create(
            context: Context,
            playerModel: PlayerViewModel,
            onItemClick: (data: Album) -> Unit
        ): AlbumGridViewHolder {
            return AlbumGridViewHolder(
                ItemAlbumGridBinding.inflate(context.inflater),
                playerModel,
                onItemClick
            )
        }
    }
}

class AlbumLinearViewHolder private constructor(
    private val binding: ItemAlbumLinearBinding,
    private val playerModel: PlayerViewModel,
    onItemClick: (data: Album) -> Unit
) : BaseHolder<Album>(binding, onItemClick) {

    override fun onBind(data: Album) {
        binding.album = data
        binding.albumName.requestLayout()
        binding.albumArtist.requestLayout()

        playerModel.currentPlayingAlbum.observe(itemView.context as LifecycleOwner, Observer {
            if(it == data){
                binding.playAlbum.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.playAlbum.setImageResource(R.drawable.ic_play_circle)
            }
        })

        playerModel.isPlaying.observe(itemView.context as LifecycleOwner, Observer { isPlaying ->
            if(isPlaying && playerModel.currentPlayingAlbum.value == data){
                binding.playAlbum.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.playAlbum.setImageResource(R.drawable.ic_play_circle)
            }
        })

        binding.playAlbum.setOnClickListener {
            if(playerModel.currentPlayingAlbum.value == data){
                playerModel.invertPlayingStatus()
            } else {
                playerModel.playAlbum(data, false)
            }
        }

    }

    companion object {
        fun create(
            context: Context,
            playerModel: PlayerViewModel,
            onItemClick: (data: Album) -> Unit
        ): AlbumLinearViewHolder {
            return AlbumLinearViewHolder(
                ItemAlbumLinearBinding.inflate(context.inflater),
                playerModel,
                onItemClick
            )
        }
    }
}

class ClickedAlbumTracksViewHolder private constructor(
    private val binding: ItemAlbumTrackBinding,
    onItemClick: (data: Track) -> Unit,
) : BaseHolder<Track>(binding, onItemClick) {

    override fun onBind(data: Track) {
        binding.track = data

        binding.trackTitle.requestLayout()
        binding.trackArtist.requestLayout()
    }

    companion object {
        fun create(
            context: Context,
            onItemClick: (data: Track) -> Unit,
        ): ClickedAlbumTracksViewHolder {
            return ClickedAlbumTracksViewHolder(
                ItemAlbumTrackBinding.inflate(context.inflater),
                onItemClick
            )
        }
    }
}

class ArtistsViewHolder private constructor(
    private val binding: ItemArtistBinding,
    onItemClick: (data: Artist) -> Unit
) : BaseHolder<Artist>(binding, onItemClick) {

    override fun onBind(data: Artist) {
        binding.artist = data

        binding.artistsTracksCount.text = binding.artistsTracksCount.context.resources.getString(R.string.artists_track_count, data.tracksCount)

        binding.artistName.requestLayout()
        binding.artistsTracksCount.requestLayout()
    }

    companion object {
        fun create(
            context: Context,
            onItemClick: (data: Artist) -> Unit
        ): ArtistsViewHolder {
            return ArtistsViewHolder(
                ItemArtistBinding.inflate(context.inflater),
                onItemClick
            )
        }
    }
}

class ClickedArtistTracksViewHolder private constructor(
    private val binding: ItemTrackBinding,
    onItemClick: (data: Track) -> Unit,
) : BaseHolder<Track>(binding, onItemClick) {

    override fun onBind(data: Track) {
        binding.track = data

        binding.trackTitle.requestLayout()
        binding.trackArtist.requestLayout()
    }

    companion object {
        fun create(
            context: Context,
            onItemClick: (data: Track) -> Unit,
        ): ClickedArtistTracksViewHolder {
            return ClickedArtistTracksViewHolder(
                ItemTrackBinding.inflate(context.inflater),
                onItemClick
            )
        }
    }
}