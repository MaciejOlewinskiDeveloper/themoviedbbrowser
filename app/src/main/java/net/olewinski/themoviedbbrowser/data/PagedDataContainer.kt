package net.olewinski.themoviedbbrowser.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState

/**
 * Container for:
 * - Observable paged data (represented as [LiveData] of [PagedList])
 * - Observable data loading states (initial and non-initial)
 * - Handles to data operations (refresh/retry)
 *
 * Differentiation between initial and non-initial data loading state might be useful e.g. for UI.
 */
data class PagedDataContainer<T : Any>(
    /**
     * Observable paged data.
     */
    val pagedData: LiveData<PagedList<T>>,

    /**
     * Observable non-initial data loading state.
     */
    val state: LiveData<DataLoadingState>,

    /**
     * Observable initial (during 100% refresh) data loading state.
     */
    val refreshState: LiveData<DataLoadingState>,

    /**
     * Operation handle that can be used to request data refresh.
     */
    val refreshDataOperation: () -> Unit,

    /**
     * Operation handle that can be used to request data fetching retry (if any error occurred).
     */
    val retryOperation: () -> Unit
)
