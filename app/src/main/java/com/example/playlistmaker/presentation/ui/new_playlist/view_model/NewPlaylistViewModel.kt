package com.example.playlistmaker.presentation.ui.new_playlist.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.db.PlaylistInteractor
import com.example.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    fun createNewPlaylist(albumName: String, description: String, albumImage: String?) {
        val playlist = Playlist(
            id = 0,
            playlistName = albumName,
            playlistDescription = description,
            playlistImage = albumImage,
            tracks = ArrayList(),
            tracksCount = 0
        )

        viewModelScope.launch {
            playlistInteractor.insertPlaylist(playlist)
        }
    }
}