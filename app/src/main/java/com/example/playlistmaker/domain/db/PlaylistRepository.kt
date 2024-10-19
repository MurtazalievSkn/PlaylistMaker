package com.example.playlistmaker.domain.db

import com.example.playlistmaker.domain.playlist.Playlist
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    suspend fun insertPlaylist(playlist: Playlist)

//    suspend fun deletePlaylist(id: Int)

    fun getPlaylists(): Flow<List<Playlist>>

    suspend fun updateTracks(playlistId: Int, tracks: ArrayList<Track>, tracksCount: Int)

}