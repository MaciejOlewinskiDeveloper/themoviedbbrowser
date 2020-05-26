package net.olewinski.themoviedbbrowser.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState

data class PagedDataContainer<T>(
    val pagedData: LiveData<PagedList<T>>,
    val networkState: LiveData<NetworkDataLoadingState>,
    val refreshState: LiveData<NetworkDataLoadingState>,
    val refreshDataOperation: () -> Unit,
    val retryOperation: () -> Unit
)
