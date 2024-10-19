package com.example.playlistmaker.presentation.ui.media.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.db.PlaylistInteractor
import com.example.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaPlaylistsViewModel(private val playlistInteractor: PlaylistInteractor): ViewModel() {

    private var isClickAllowed = true
    private var clickJob: Job? = null

    private val _playlists: MutableLiveData<List<Playlist>> = MutableLiveData<List<Playlist>>()
    fun playLists(): LiveData<List<Playlist>> = _playlists

    fun checkPlaylistDb() {
        viewModelScope.launch {
            playlistInteractor
                .getPlaylists()
                .collect { result ->
                    _playlists.postValue(result)
                }
        }
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            clickJob = viewModelScope.launch {
                isClickAllowed = false
                delay(MediaPlaylistsViewModel.CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}