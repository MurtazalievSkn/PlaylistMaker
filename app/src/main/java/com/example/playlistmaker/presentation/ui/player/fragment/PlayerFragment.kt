package com.example.playlistmaker.presentation.ui.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.domain.player.models.MediaPlayerStatus
import com.example.playlistmaker.domain.player.models.PlayerProgressStatus
import com.example.playlistmaker.domain.playlist.Playlist
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.presentation.isNightModeOn
import com.example.playlistmaker.presentation.ui.player.PlaylistAdapterForBottomSheet
import com.example.playlistmaker.presentation.ui.player.view_model.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerFragment : Fragment() {
    private var binding: FragmentPlayerBinding? = null
    private var trackAddInQueue = false

    private val viewModel by viewModel<PlayerViewModel>()
    private lateinit var adapter: PlaylistAdapterForBottomSheet
    private lateinit var playlists: ArrayList<Playlist>
    private lateinit var track: Track

    private val onClick: (playlist: Playlist) -> Unit = {
        viewModel.addTrackInPlaylist(it, track)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlists = ArrayList()
        adapter = PlaylistAdapterForBottomSheet(playlists, onClick)
        binding!!.rvPlaylist.adapter = adapter
        binding!!.timeOfPlay.text = getString(R.string.player_dafault_time)
        val bottomSheetBehavior = BottomSheetBehavior.from(binding!!.playlistsBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        track = arguments?.getParcelable<Track>(TRACK_KEY) as Track


        writeDataInActivity(track)
        viewModel.onCreate(track)
        viewModel.checkPlaylistsInDb()

        viewModel.playerProgressStatus().observe(viewLifecycleOwner) { playerProgressStatus ->
            playbackControl(playerProgressStatus)
        }

        viewModel.trackAddInFavorite().observe(viewLifecycleOwner) { favoriteStatus ->
            changeButtonFavoriteImage(favoriteStatus)
        }

        viewModel.playlistsLiveData().observe(viewLifecycleOwner) { playlistsInDb ->
            showPlaylistsInBottomSheet(playlistsInDb)
        }

        viewModel.toastMessage().observe(viewLifecycleOwner) { message ->
            showToast(message)
        }

        binding!!.backButtonIv.setOnClickListener {
            findNavController().navigateUp()
        }

        binding!!.ibButtonQueue.setOnClickListener {
            showBottomSheet()
//            changeButtonQueueImage()
        }

        binding!!.ivButtonFavorite.setOnClickListener {
            viewModel.clickButtonFavorite(track)
        }

        binding!!.buttonPlay.setOnClickListener {
            viewModel.playbackControl()
        }

        binding!!.buttonNewPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding!!.overlay.isVisible = false
                        binding!!.buttonPlay.isEnabled = true
                        binding!!.ivButtonFavorite.isEnabled = true
                        binding!!.ibButtonQueue.isEnabled = true
                    }
                    else -> {
                        binding!!.overlay.isVisible = true
                        binding!!.buttonPlay.isEnabled = false
                        binding!!.ivButtonFavorite.isEnabled = false
                        binding!!.ibButtonQueue.isEnabled = false
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseMediaPlayer()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        viewModel.destroyMediaPlayer()
        super.onDestroy()
    }

    private fun playbackControl(playerProgressStatus: PlayerProgressStatus) {
        when (playerProgressStatus.mediaPlayerStatus) {
            MediaPlayerStatus.STATE_PLAYING -> {
                binding!!.buttonPlay.setImageResource(R.drawable.button_pause)
                binding!!.timeOfPlay.text =
                    SimpleDateFormat(
                        "m:ss",
                        Locale.getDefault()
                    ).format(playerProgressStatus.currentPosition)
            }

            MediaPlayerStatus.STATE_PAUSED -> {
                binding!!.buttonPlay.setImageResource(R.drawable.button_play)
            }

            MediaPlayerStatus.STATE_PREPARED -> {
                binding!!.timeOfPlay.text = "0:00"
                binding!!.buttonPlay.setImageResource(R.drawable.button_play)
            }

            MediaPlayerStatus.STATE_ERROR -> {
                showErrorMassage()
            }

            MediaPlayerStatus.STATE_DEFAULT -> {
            }
        }
    }

    private fun writeDataInActivity(track: Track) {
        binding!!.trackName.text = track.trackName
        binding!!.artistName.text = track.artistName
        binding!!.trackDuration.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        binding!!.albumName.text = track.collectionName
        binding!!.trackYear.text =
            if (!track.releaseDate.equals(getString(R.string.unknown))) track.releaseDate.substring(
                0,
                4
            ) else getString(R.string.not_found)
        binding!!.genreName.text = track.primaryGenreName
        binding!!.countryName.text = track.country

        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(this.resources.getDimensionPixelSize(R.dimen.rounded_corners_radius)))
            .into(binding!!.albumPage)
    }

    private fun showBottomSheet() {
        BottomSheetBehavior.from(binding!!.playlistsBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun changeButtonFavoriteImage(trackAddInFavorite: Boolean) {
        if (trackAddInFavorite) {
            if (requireContext().isNightModeOn()) binding!!.ivButtonFavorite.setImageResource(R.drawable.button_favorite_nm_2)
            else binding!!.ivButtonFavorite.setImageResource(R.drawable.button_favorite_lm_2)
        } else {
            if (requireContext().isNightModeOn()) binding!!.ivButtonFavorite.setImageResource(R.drawable.button_favorite_nm_1)
            else binding!!.ivButtonFavorite.setImageResource(R.drawable.button_favorite_lm_1)
        }
    }

    private fun changeButtonQueueImage() {
        if (trackAddInQueue) {
            trackAddInQueue = false
            binding!!.ibButtonQueue.setImageResource(R.drawable.button_queue)
        } else {
            trackAddInQueue = true
            binding!!.ibButtonQueue.setImageResource(R.drawable.button_add_in_queue)
        }
    }

    private fun showErrorMassage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.audio_not_found),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPlaylistsInBottomSheet(playListsInDb: List<Playlist>){
        playlists.clear()
        playlists.addAll(playListsInDb)
        adapter.notifyDataSetChanged()
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TRACK_KEY = "track"
    }
}