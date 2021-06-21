package com.example.musicplayer.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.adapters.ClickedArtistTracksAdapter
import com.example.musicplayer.databinding.FragmentClickedArtistBinding
import com.example.musicplayer.music.MusicStore
import com.example.musicplayer.player.state.QueueConstructor
import com.example.musicplayer.utils.PreferencesManager
import com.example.musicplayer.viewmodels.FavoriteViewModel
import com.example.musicplayer.viewmodels.PlayerViewModel
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlin.math.abs


class ClickedArtistFragment : Fragment() {
    private var _binding: FragmentClickedArtistBinding? = null
    private val binding get() = _binding!!

    private lateinit var playerModel: PlayerViewModel
    private lateinit var favoriteModel : FavoriteViewModel

    private val args : ClickedArtistFragmentArgs by navArgs()

    val preferencesManager = PreferencesManager.getInstance()

    private var isFavorite : Boolean = true

    private val clickedArtistAdapter: ClickedArtistTracksAdapter by lazy {
        ClickedArtistTracksAdapter(onItemClick = { playerModel.playTrack(it, QueueConstructor.IN_ARTIST.toInt()) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClickedArtistBinding.inflate(inflater, container, false)
        playerModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        favoriteModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)
        val currentArtist = MusicStore.getInstance().artists.find { it.id == args.artistId }!!

        binding.artist = currentArtist

        binding.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)
            } else {
                activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.accent)
            }
        })

        currentArtist.tracks.forEach { track ->
            favoriteModel.MusicExist(track.id).observe(viewLifecycleOwner, Observer { item ->
                if(item == null){
                    Toast.makeText(activity, "BRAK w ulubionych", Toast.LENGTH_SHORT).show()
                    isFavorite = false
                    binding.toolbar.menu.getItem(0).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline))
                }
            })

            if(isFavorite == false){
                return@forEach
            }
        }

        if(isFavorite){
            Toast.makeText(activity, "ulubione", Toast.LENGTH_SHORT).show()
            binding.toolbar.menu.getItem(0).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled))
        }
        else{
            Toast.makeText(activity, "nie ulubione", Toast.LENGTH_SHORT).show()
            binding.toolbar.menu.getItem(0).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline))
        }


        binding.toolbar.setNavigationOnClickListener {
            activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.aboveBackground)
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addTracksListToFav -> {
                    // TO DO ADD FAVORITE MECHANISM
                    true
                }

                else -> false

            }
        }

        binding.clickedArtistTracksRecyclerView.apply {
            adapter = clickedArtistAdapter
            setHasFixedSize(true)

            clickedArtistAdapter.setData(currentArtist.tracks)
        }

        binding.shuffleArtistButton.setOnClickListener {
            val animationClick = AnimationUtils.loadAnimation(inflater.context, R.anim.shuffle_button_clicked)
            it.startAnimation(animationClick)

            animationClick.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    val animationReset = AnimationUtils.loadAnimation(inflater.context, R.anim.shuffle_button_reset)
                    it.startAnimation(animationReset)
                }
            })

            playerModel.playArtist(currentArtist, true)



        }

        return binding.root
    }
}