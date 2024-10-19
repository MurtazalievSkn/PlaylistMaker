package com.example.playlistmaker.presentation.ui.search.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TrackInteractor
import com.example.playlistmaker.domain.search.model.SearchStatus
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.search.model.TrackSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.function.Consumer

class SearchViewModel (
    val searchHistoryInteractor: SearchHistoryInteractor,
    val trackInteractor: TrackInteractor,

) : ViewModel(), Consumer<TrackSearchResult> {
    private var requestText = ""
    private var isClickAllowed = true
    private var clickJob: Job? = null
    private var searchJob: Job? = null

    private val _tracksHistory: MutableLiveData<ArrayList<Track>> =
        MutableLiveData(searchHistoryInteractor.getTracksHistory())

    fun tracksHistory(): LiveData<ArrayList<Track>> =
        _tracksHistory

    private val _foundTracks: MutableLiveData<TrackSearchResult> =
        MutableLiveData(TrackSearchResult(results = emptyList(), SearchStatus.DEFAULT))

    fun foundTracks(): LiveData<TrackSearchResult> = _foundTracks
    
    private fun search() {
        _foundTracks.postValue(getLoadingStatus())
        sendRequest()
    }

    fun removeCallbacks() {
        searchJob?.cancel()
    }

    fun updateTrackHistory() {
        _tracksHistory.postValue(searchHistoryInteractor.getTracksHistory())
    }

    fun changeRequestText(text: String) {
        requestText = text
    }

    fun addTrackInSearchHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
    }

    fun searchDebounce() {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            search()
        }
    }

    fun cleanHistory() {
        searchHistoryInteractor.clean()
    }

    fun deleteFoundTracks() {
        _foundTracks.postValue(TrackSearchResult(results = emptyList(), SearchStatus.DEFAULT))
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            clickJob = viewModelScope.launch {
                isClickAllowed = false
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private fun sendRequest() {
        viewModelScope.launch {
            trackInteractor
                .searchTracks(requestText)
                .collect{result->
                    _foundTracks.postValue(result)
                }
        }
    }
    
    private fun getLoadingStatus(): TrackSearchResult {
        return TrackSearchResult(
            results = emptyList(),
            SearchStatus.LOADING
        )
    }

    override fun accept(trackSearchResult: TrackSearchResult) {
        when (trackSearchResult.status) {
            SearchStatus.RESPONSE_RECEIVED -> {
                _foundTracks.postValue(trackSearchResult)
            }

            SearchStatus.NETWORK_ERROR, SearchStatus.DEFAULT, SearchStatus.LIST_IS_EMPTY -> {
                _foundTracks.postValue(trackSearchResult)
            }

            SearchStatus.LOADING -> {

            }
        }
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}