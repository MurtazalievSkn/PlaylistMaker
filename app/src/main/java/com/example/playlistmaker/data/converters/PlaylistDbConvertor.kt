package com.example.playlistmaker.data.converters

import com.example.playlistmaker.data.media.entity.PlaylistEntity
import com.example.playlistmaker.domain.playlist.Playlist
import com.example.playlistmaker.domain.search.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbConvertor {

    fun map(playlist: PlaylistEntity): Playlist {
        return Playlist(
            playlist.id,
            playlist.playlistName,
            playlist.playlistDescription,
            playlist.playlistImage,
            createTracksFromJson(playlist.tracksId),
            playlist.tracksCount
        )
    }

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            playlist.id,
            playlist.playlistName,
            playlist.playlistDescription,
            playlist.playlistImage,
            createJsonFromTracks(playlist.tracks),
            playlist.tracksCount
        )
    }

    private fun createTracksFromJson(json: String?): ArrayList<Track> {
        if (json.isNullOrEmpty()) {
            return ArrayList()
        } else {
            val token = object : TypeToken<ArrayList<Track>>() {}.type
            return Gson().fromJson<ArrayList<Track>>(json, token)
        }
    }

    private fun createJsonFromTracks(tracks: ArrayList<Track>): String? {
        if (tracks.isEmpty()) {
            return null
        } else {
            return Gson().toJson(tracks)
        }
    }
}