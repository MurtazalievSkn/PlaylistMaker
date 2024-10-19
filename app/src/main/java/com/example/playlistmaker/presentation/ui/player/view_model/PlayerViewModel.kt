package com.example.playlistmaker.presentation.ui.player.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.db.PlaylistInteractor
import com.example.playlistmaker.domain.player.MediaPlayerInteractor
import com.example.playlistmaker.domain.player.models.MediaPlayerStatus
import com.example.playlistmaker.domain.player.models.PlayerProgressStatus
import com.example.playlistmaker.domain.playlist.Playlist
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val mediaPlayerInteractor: MediaPlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val playlistInteractor: PlaylistInteractor,

) : ViewModel() {
    private var updateTimeOfPlayJob: Job? = null
    private var favoriteButtonJob: Job? = null
    private var addTrackInDb: Job? = null
    private var deleteTrackFromDb: Job? = null


    private val _playerProgressStatus: MutableLiveData<PlayerProgressStatus> =
        MutableLiveData(updatePlayerProgressStatus())

    fun playerProgressStatus(): LiveData<PlayerProgressStatus> = _playerProgressStatus

    private var _trackAddInFavorite: MutableLiveData<Boolean> = MutableLiveData(false)
    fun trackAddInFavorite(): LiveData<Boolean> = _trackAddInFavorite

    private val _playlistsLiveData: MutableLiveData<List<Playlist>> =
        MutableLiveData<List<Playlist>>()

    fun playlistsLiveData(): LiveData<List<Playlist>> = _playlistsLiveData

    private val _toastMessage: MutableLiveData<String> = MutableLiveData<String>()
    fun toastMessage(): LiveData<String> = _toastMessage

    fun onCreate(track: Track) {
        mediaPlayerInteractor.preparePlayer(track)
        _playerProgressStatus.value = updatePlayerProgressStatus()
        _trackAddInFavorite.postValue(track.isFavorite)
    }

    private fun updatePlayerProgressStatus(): PlayerProgressStatus {
        return mediaPlayerInteractor.getPlayerProgressStatus()
    }

    fun pauseMediaPlayer() {
        mediaPlayerInteractor.pausePlayer()
    }

    fun destroyMediaPlayer() {
        updateTimeOfPlayJob?.cancel()
        favoriteButtonJob?.cancel()
        addTrackInDb?.cancel()
        deleteTrackFromDb?.cancel()
        mediaPlayerInteractor.destroyPlayer()
    }

    fun updateTimeOfPlay() {                   //Обновленеи времени проигрования трека

        _playerProgressStatus.value = updatePlayerProgressStatus()


        when (_playerProgressStatus.value!!.mediaPlayerStatus) {
            MediaPlayerStatus.STATE_PLAYING -> {
                updateTimeOfPlayJob = viewModelScope.launch {
                    delay(UPDATE)
                    updateTimeOfPlay()
                }
            }

            else -> {
                updateTimeOfPlayJob?.cancel()
            }
        }
    }


    fun playbackControl() {
        _playerProgressStatus.value = updatePlayerProgressStatus()
        when (_playerProgressStatus.value!!.mediaPlayerStatus) {
            MediaPlayerStatus.STATE_PLAYING -> {
                pausePlayer()
            }

            MediaPlayerStatus.STATE_PREPARED, MediaPlayerStatus.STATE_PAUSED -> {
                startPlayer()
            }

            MediaPlayerStatus.STATE_ERROR, MediaPlayerStatus.STATE_DEFAULT -> {
            }
        }
    }

    fun clickButtonFavorite(track: Track) {
        if (_trackAddInFavorite.value!!) {
            track.isFavorite = false
            deleteTrackFromDb = viewModelScope.launch {          //удалить и изменить значение livedata
                favoriteTracksInteractor.deleteTrackFromFavorite(track)
            }
            _trackAddInFavorite.postValue(false)
            searchHistoryInteractor.addTrack(track)
        }
        else {
            track.isFavorite = true
            addTrackInDb = viewModelScope.launch {
                favoriteTracksInteractor.insertTrackToFavorite(track)
            }
            _trackAddInFavorite.postValue(true)
            searchHistoryInteractor.addTrack(track)
        }
    }

    private fun startPlayer() {
        mediaPlayerInteractor.startPlayer()
        updateTimeOfPlay()
        _playerProgressStatus.value = updatePlayerProgressStatus()
    }

    private fun pausePlayer() {
        mediaPlayerInteractor.pausePlayer()
        _playerProgressStatus.value = updatePlayerProgressStatus()
    }

    fun checkPlaylistsInDb() {
        viewModelScope.launch {
            playlistInteractor
                .getPlaylists()
                .collect { result ->
                    _playlistsLiveData.postValue(result)
                }
        }
    }

    fun addTrackInPlaylist(playlist: Playlist, track: Track) {
        if (playlist.tracks.contains(track)) {
            _toastMessage.value = "Трек уже добавлен в плейлист ${playlist.playlistName}"
        } else {
            val tracks = playlist.tracks
            tracks.add(track)
            viewModelScope.launch {
                playlistInteractor.updateTracks(playlist.id, tracks, playlist.tracksCount + 1)
            }
            _toastMessage.value = "Добавлено в плейлист ${playlist.playlistName}"
            checkPlaylistsInDb()
        }
    }

    companion object {
        private const val UPDATE = 250L
    }
}