package net.olewinski.themoviedbbrowser.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository

class NowPlayingViewModel(nowPlayingRepository: NowPlayingRepository) : ViewModel() {
    private val nowPlayingData = nowPlayingRepository.getNowPlayingData(viewModelScope)

    val pagedData = nowPlayingData.pagedData
    val networkState = nowPlayingData.networkState
    val refreshState = nowPlayingData.refreshState

    fun refresh() {
        nowPlayingData.refreshDataOperation.invoke()
    }

    class NowPlayingViewModelFactory(private val nowPlayingRepository: NowPlayingRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NowPlayingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NowPlayingViewModel(nowPlayingRepository) as T
            } else {
                throw Error("Incorrect ViewModel requested: only NowPlayingViewModel can be provided here")
            }
        }
    }
}
