package com.example.playlistmaker.presentation.ui.media.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.MediaPlaylistsFragmentBinding
import com.example.playlistmaker.domain.playlist.Playlist
import com.example.playlistmaker.presentation.isNightModeOn
import com.example.playlistmaker.presentation.ui.BindingFragment
import com.example.playlistmaker.presentation.ui.main.MainActivity
import com.example.playlistmaker.presentation.ui.media.PlaylistAdapter
import com.example.playlistmaker.presentation.ui.media.view_model.MediaPlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaPlaylistsFragment: BindingFragment<MediaPlaylistsFragmentBinding>() {

    private lateinit var adapter: PlaylistAdapter
    private lateinit var playlists: ArrayList<Playlist>

    private val viewModel by viewModel<MediaPlaylistsViewModel>()

    private val onClick: (playlist: Playlist) -> Unit = {
        if (viewModel.clickDebounce()) {

        }
    }
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): MediaPlaylistsFragmentBinding {
        return MediaPlaylistsFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlists = ArrayList<Playlist>()
        adapter = PlaylistAdapter(playlists, onClick)
        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.adapter = adapter

        viewModel.checkPlaylistDb()
        viewModel.playLists().observe(viewLifecycleOwner) {
            showPlaylists(it)
        }


        binding.buttonNewPlaylist.setOnClickListener {
            (activity as? MainActivity)?.hideNavBar()
            findNavController().navigate(R.id.action_mediaFragment_to_newPlaylistFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar()
    }

    private fun showPlaylists(findPlaylists: List<Playlist>) {
        if (findPlaylists.isEmpty()) {
            showErrorImage()
        } else {
            hideErrorImage()
            playlists.clear()
            playlists.addAll(findPlaylists)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showErrorImage() {
        binding.rvPlaylists.isVisible = false
        binding.ivImageError.isVisible = true
        binding.tvMessageError.isVisible = true
        if (requireContext().isNightModeOn()) {
            Glide.with(this)
                .load(R.drawable.not_found_dark)
                .into(binding.ivImageError)
        } else {
            Glide.with(this)
                .load(R.drawable.not_found_light)
                .into(binding.ivImageError)
        }
    }

    private fun hideErrorImage() {
        binding.rvPlaylists.isVisible = true
        binding.ivImageError.isVisible = false
        binding.tvMessageError.isVisible = false
    }

    companion object {
        fun newInstance() = MediaPlaylistsFragment()
    }
}