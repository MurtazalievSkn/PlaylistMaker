package com.example.playlistmaker.data.player.repositoryImpl

import android.media.MediaPlayer
import com.example.playlistmaker.domain.player.MediaPlayerRepository
import com.example.playlistmaker.domain.player.models.MediaPlayerStatus
import com.example.playlistmaker.domain.player.models.PlayerProgressStatus
import com.example.playlistmaker.domain.search.model.Track

class MediaPlayerRepositoryImpl (
    private var player: MediaPlayer,
    private var playerState: MediaPlayerStatus
) : MediaPlayerRepository {
    override fun preparePlayer(track: Track) {
        player.reset()
        try {
            player.setDataSource(track.previewUrl)
            player.prepareAsync()
            player.setOnPreparedListener {
                playerState = MediaPlayerStatus.STATE_PREPARED
            }
            player.setOnCompletionListener {
                playerState = MediaPlayerStatus.STATE_PREPARED
            }

        } catch (e: Exception) {
            playerState = MediaPlayerStatus.STATE_ERROR
        }

    }

    override fun startPlayer() {
        player.start()
        playerState = MediaPlayerStatus.STATE_PLAYING
    }

    override fun pausePlayer() {
        player.pause()
        playerState = MediaPlayerStatus.STATE_PAUSED
    }

    override fun getPlayerProgressStatus(): PlayerProgressStatus {
        val currentPosition = player.currentPosition
        return when (playerState) {
            MediaPlayerStatus.STATE_PLAYING -> {
                PlayerProgressStatus(
                    mediaPlayerStatus = MediaPlayerStatus.STATE_PLAYING,
                    currentPosition = player.currentPosition
                )
            }

            MediaPlayerStatus.STATE_DEFAULT -> {
                PlayerProgressStatus(
                    mediaPlayerStatus = MediaPlayerStatus.STATE_DEFAULT,
                    currentPosition = 0
                )
            }
            MediaPlayerStatus.STATE_PREPARED -> {
                PlayerProgressStatus(
                    mediaPlayerStatus = MediaPlayerStatus.STATE_PREPARED,
                    currentPosition = player.currentPosition

                )
            }
            MediaPlayerStatus.STATE_PAUSED -> {
                PlayerProgressStatus(
                    mediaPlayerStatus = MediaPlayerStatus.STATE_PAUSED,
                    currentPosition = player.currentPosition
                )
            }
            MediaPlayerStatus.STATE_ERROR -> {
                PlayerProgressStatus(
                    mediaPlayerStatus = MediaPlayerStatus.STATE_ERROR,
                    currentPosition = 0
                )
            }
        }
    }

    override fun destroyPlayer() {
        player.release()
    }
}