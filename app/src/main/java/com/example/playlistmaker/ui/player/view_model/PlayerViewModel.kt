package com.example.playlistmaker.ui.player.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.player.MediaPlayerInteractor
import com.example.playlistmaker.domain.player.models.MediaPlayerStatus
import com.example.playlistmaker.domain.player.models.PlayerProgressStatus
import com.example.playlistmaker.domain.search.model.Track

class PlayerViewModel(
    val mediaPlayerInteractor: MediaPlayerInteractor
) : ViewModel() {
    private val mainThreadHandler = Handler(Looper.getMainLooper())


    private val _playerProgressStatus: MutableLiveData<PlayerProgressStatus> =
        MutableLiveData(updatePlayerProgressStatus())
    fun getPlayerProgressStatus(): LiveData<PlayerProgressStatus> = _playerProgressStatus

    fun onCreate(track: Track) {
        mediaPlayerInteractor.preparePlayer(track)
        _playerProgressStatus.value = updatePlayerProgressStatus()
    }

    private fun updatePlayerProgressStatus(): PlayerProgressStatus {
        return mediaPlayerInteractor.getPlayerProgressStatus()
    }

    fun pauseMediaPlayer() {
        mediaPlayerInteractor.pausePlayer()
    }

    fun destroyMediaPlayer() {
        mainThreadHandler.removeCallbacks(updateTimeOfPlay())
        mediaPlayerInteractor.destroyPlayer()
    }

    fun updateTimeOfPlay(): Runnable {                   //Обновленеи времени проигрования трека
        return object : Runnable {
            override fun run() {
                _playerProgressStatus.value = updatePlayerProgressStatus()

                if (_playerProgressStatus.value!!.mediaPlayerStatus == MediaPlayerStatus.STATE_PLAYING) {
                    mainThreadHandler.postDelayed(this, UPDATE)
                } else if (_playerProgressStatus.value!!.mediaPlayerStatus == MediaPlayerStatus.STATE_PAUSED) {
                    mainThreadHandler.removeCallbacks(updateTimeOfPlay())
                } else {
                    mainThreadHandler.removeCallbacks(updateTimeOfPlay())
                }
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

    private fun startPlayer() {
        mediaPlayerInteractor.startPlayer()
        mainThreadHandler.post(updateTimeOfPlay())
        _playerProgressStatus.value = updatePlayerProgressStatus()
    }

    private fun pausePlayer() {
        mediaPlayerInteractor.pausePlayer()
        _playerProgressStatus.value = updatePlayerProgressStatus()
    }
    companion object {
        private const val UPDATE = 250L
    }
}