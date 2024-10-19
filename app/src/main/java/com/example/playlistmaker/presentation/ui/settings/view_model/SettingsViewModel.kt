package com.example.playlistmaker.presentation.ui.settings.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.sharing.SharingInteractor

class SettingsViewModel(
    private val sharingInteractorImpl: SharingInteractor,
    private val settingsInteractorImpl: SettingsInteractor,
) : ViewModel() {
    private val _nightMode = MutableLiveData(getNightModeSettings())
    val nightMode: LiveData<Boolean> = _nightMode

    private fun getNightModeSettings() : Boolean {
        return settingsInteractorImpl.getNightModeSettings()
    }

    fun shareApp() {
        sharingInteractorImpl.shareApp()
    }

    fun writeToSupport() {
        sharingInteractorImpl.openSupport()
    }

    fun openTerms() {
        sharingInteractorImpl.openTerms()
    }

    fun putNightModeSettings(checked: Boolean) {
        settingsInteractorImpl.putNightModeSettings(checked)
    }

    fun switchTheme(checked: Boolean) {
        settingsInteractorImpl.switchTheme(checked)
    }

}