package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.ui.media.view_model.MediaFavoriteViewModel
import com.example.playlistmaker.presentation.ui.media.view_model.MediaPlaylistsViewModel
import com.example.playlistmaker.presentation.ui.media.view_model.MediaViewModel
import com.example.playlistmaker.presentation.ui.new_playlist.view_model.NewPlaylistViewModel
import com.example.playlistmaker.presentation.ui.player.view_model.PlayerViewModel
import com.example.playlistmaker.presentation.ui.search.view_model.SearchViewModel
import com.example.playlistmaker.presentation.ui.settings.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val activityModule = module {

    viewModel {
        PlayerViewModel(get(), get(), get(), get())
    }

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel {
        MediaViewModel()
    }

    viewModel {
        MediaFavoriteViewModel(get())
    }

    viewModel {
        MediaPlaylistsViewModel(get())
    }

    viewModel {
        NewPlaylistViewModel(get())
    }
}