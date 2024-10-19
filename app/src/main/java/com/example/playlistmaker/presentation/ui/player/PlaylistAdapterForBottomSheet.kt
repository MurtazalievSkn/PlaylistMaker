package com.example.playlistmaker.presentation.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.playlist.Playlist

class PlaylistAdapterForBottomSheet(
    private val playlists: List<Playlist>,
    private val listener: (Playlist) -> Unit
): RecyclerView.Adapter<PlaylistViewHolderForBottomSheet>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolderForBottomSheet {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item_for_bottom_sheet, parent, false)
        return PlaylistViewHolderForBottomSheet(view)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onBindViewHolder(holder: PlaylistViewHolderForBottomSheet, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            listener.invoke(playlists[position])
        }
    }
}