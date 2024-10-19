package com.example.playlistmaker.presentation.ui.media.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaFavoriteViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
): ViewModel() {

    private var isClickAllowed = true
    private var clickJob: Job? = null
    private var dataBaseJob: Job? = null


    private val _favoriteTracks: MutableLiveData<List<Track>> =
        MutableLiveData(ArrayList<Track>())

    fun favoriteTracks(): LiveData<List<Track>> =
        _favoriteTracks

    fun onCreate() {
        dataBaseJob = viewModelScope.launch(Dispatchers.IO) {
            favoriteTracksInteractor.getAllFavoriteTracks().collect { it ->
                _favoriteTracks.postValue(it)
            }
        }
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            clickJob = viewModelScope.launch(Dispatchers.IO) {
                isClickAllowed = false
                delay(MediaFavoriteViewModel.CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    fun onDestroy() {
        clickJob?.cancel()
        dataBaseJob?.cancel()
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}