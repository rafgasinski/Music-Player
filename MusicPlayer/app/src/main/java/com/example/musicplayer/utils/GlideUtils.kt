package com.example.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.R
import com.example.musicplayer.music.Album
import com.example.musicplayer.music.BaseModel
import com.example.musicplayer.music.Track


@BindingAdapter("albumArt")
fun ImageView.bindAlbumArt(track: Track?) {
    load(track?.album, R.drawable.track_placeholder)
}

@BindingAdapter("albumArt")
fun ImageView.bindAlbumArt(album: Album?) {
    load(album, R.drawable.album_placeholder)
}

@BindingAdapter("albumArtAndColor")
fun ImageView.bindAlbumArtAndGetColor(track: Track?) {
    loadAndGetVibrantColor(track?.album, R.drawable.track_placeholder)
}

@BindingAdapter("albumArtAndColor")
fun ImageView.bindAlbumArtAndGetColor(album: Album?) {
    loadAndGetVibrantColor(album, R.drawable.album_placeholder)
}

inline fun <reified T : BaseModel> ImageView.load(
    data: T?,
    @DrawableRes placeholderDrawable: Int,
) {

    val reqOpt = RequestOptions.fitCenterTransform().diskCacheStrategy(DiskCacheStrategy.NONE).override(this.width, this.height)

    if(data is Album){
        Glide.with(context).load(data.coverUri).transition(DrawableTransitionOptions().crossFade()).apply(reqOpt)
            .placeholder(placeholderDrawable).into(this)
    }

}

inline fun <reified T : BaseModel> ImageView.loadAndGetVibrantColor(
    data: T?,
    @DrawableRes placeholderDrawable: Int,
) {

    val reqOpt = RequestOptions.fitCenterTransform().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).override(2015, 2015)
    val preferencesManager = PreferencesManager.getInstance()

    if(data is Album){
        Glide.with(context).asBitmap().load(data.coverUri).apply(reqOpt)
            .placeholder(placeholderDrawable)
            .listener(object : RequestListener<Bitmap>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    preferencesManager.gradientAccentColor = ResourcesCompat.getColor(context.resources, R.color.accent, null)
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    if (resource != null){
                        val p = Palette.from(resource).generate()
                        if(p.vibrantSwatch != null){
                            preferencesManager.gradientAccentColor = p.getVibrantColor(ResourcesCompat.getColor(context.resources, R.color.accent, null))
                        } else {
                            preferencesManager.gradientAccentColor = p.getMutedColor(ResourcesCompat.getColor(context.resources, R.color.accent, null))
                        }
                    }
                    return false
                }

            }).into(this)
    }

}

fun loadBitmap(context: Context, track: Track, onFinished: (Bitmap?) -> Unit) {

    Glide.with(context).asBitmap().load(track.album.coverUri)
        .error(onFinished(BitmapFactory.decodeResource(context.resources, R.drawable.track_placeholder)))
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).placeholder(android.R.color.transparent).into(object : CustomTarget<Bitmap>(){

        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
            onFinished(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}