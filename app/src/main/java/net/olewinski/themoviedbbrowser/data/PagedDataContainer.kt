package net.olewinski.themoviedbbrowser.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState

data class PagedDataContainer<T>(
    val pagedData: LiveData<PagedList<T>>,
    val state: LiveData<DataLoadingState>,
    val refreshState: LiveData<DataLoadingState>,
    val refreshDataOperation: () -> Unit,
    val retryOperation: () -> Unit
)
