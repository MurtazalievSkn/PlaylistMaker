package com.example.playlistmaker.data.search.network.impl

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.media.AppDatabase
import com.example.playlistmaker.data.search.dto.TrackSearchRequest
import com.example.playlistmaker.data.search.dto.TrackSearchResponse
import com.example.playlistmaker.domain.search.TrackRepository
import com.example.playlistmaker.domain.search.model.SearchStatus
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.search.model.TrackSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrackRepositoryImpl (
    private val networkClient: NetworkClient,
    private val appDatabase: AppDatabase,
    ) : TrackRepository {
    override fun searchTracks(expression: String): Flow<TrackSearchResult> = flow {
        val response = networkClient.doTrackSearchRequest(TrackSearchRequest(expression))

        val favoriteTracksId: List<String> = appDatabase.trackDao().getIdFavoriteTracksId()

        if (response.resultStatus == SearchStatus.RESPONSE_RECEIVED) {
            val tracks: List<Track> = (response as TrackSearchResponse).results.map {
                Track(
                    trackId = it.trackId,
                    trackName = if (it.trackName.isNullOrEmpty()) "unknown" else it.trackName,
                    artistName = if (it.artistName.isNullOrEmpty()) "unknown" else it.artistName,
                    trackTimeMillis = it.trackTimeMillis,
                    artworkUrl100 = it.artworkUrl100,
                    collectionName = if (it.collectionName.isNullOrEmpty()) "unknown" else it.collectionName,
                    releaseDate = if (it.releaseDate.isNullOrEmpty()) "unknown" else it.releaseDate,
                    primaryGenreName = if (it.primaryGenreName.isNullOrEmpty()) "unknown" else it.primaryGenreName,
                    previewUrl = if (it.previewUrl.isNullOrEmpty()) "preview is not available" else it.previewUrl,
                    country = if (it.country.isNullOrEmpty()) "unknown" else it.country
                )
            }
            if (!tracks.isEmpty() && !favoriteTracksId.isEmpty()) {
                favoriteTracksId.forEach { favoriteId ->
                    tracks.forEach { track ->
                        if (track.trackId == favoriteId) track.isFavorite = true
                    }
                }
                tracks.sortedBy { it.isFavorite }
            }

            emit(
                TrackSearchResult(
                    tracks,
                    if (tracks.isEmpty()) SearchStatus.LIST_IS_EMPTY else SearchStatus.RESPONSE_RECEIVED
                )
            )
        } else {
            emit(
                TrackSearchResult(
                    emptyList(),
                    SearchStatus.NETWORK_ERROR
                )
            )
        }
    }
}