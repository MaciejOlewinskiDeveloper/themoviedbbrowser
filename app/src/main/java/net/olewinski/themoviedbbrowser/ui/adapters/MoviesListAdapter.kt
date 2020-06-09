package net.olewinski.themoviedbbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.databinding.MoviesListItemBinding
import net.olewinski.themoviedbbrowser.databinding.NetworkStateListItemBinding

private const val LIST_ITEM_TYPE_REGULAR = 0
private const val LIST_ITEM_TYPE_NETWORK_STATE = 1

/**
 * [PagedListAdapter] for handling paged lists of [MovieData].
 *
 * @param lifecycleOwner                            [LifecycleOwner] for observing [LiveData] by
 *                                                  Android Data Binding library. In fact, it would
 *                                                  be more optimal to make ViewHolders lifecycle
 *                                                  aware so that they would implement
 *                                                  LifecycleOwner themselves.
 * @param onMovieDataItemClickedListener            Operation handle that should be called when
 *                                                  [MovieData] item is clicked.
 * @param onMovieFavouriteIndicatorClickedListener  Operation handle that should be called when
 *                                                  [MovieData] is marked as favourite.
 * @param onRetryButtonClickedListener              Operation handle that should be called when
 *                                                  "Retry" button is clicked.
 */
class MoviesListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val onMovieDataItemClickedListener: (MovieData) -> Unit,
    private val onMovieFavouriteIndicatorClickedListener: (MovieData) -> Unit,
    private val onRetryButtonClickedListener: () -> Unit
) : PagedListAdapter<MovieData, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    /**
     * [RecyclerView.ViewHolder] for [MovieData] items.
     *
     * @param moviesListItemBinding View binding representation.
     */
    inner class MovieDataItemViewHolder(private val moviesListItemBinding: MoviesListItemBinding) :
        RecyclerView.ViewHolder(moviesListItemBinding.root) {
        /**
         * Binds [MovieData] item to view.
         *
         * @param movieData [MovieData] item to bind.
         */
        fun bind(movieData: MovieData) {
            moviesListItemBinding.apply {
                item = movieData

                root.setOnClickListener {
                    onMovieDataItemClickedListener.invoke(movieData)
                }

                favouriteStatus.setOnClickListener {
                    onMovieFavouriteIndicatorClickedListener.invoke(movieData)
                }

                executePendingBindings()
            }
        }
    }

    private var dataLoadingState: DataLoadingState? = null

    /**
     * [RecyclerView.ViewHolder] for [DataLoadingState] item.
     *
     * @param networkStateListItemBinding View binding representation.
     */
    inner class NetworkDataLoadingStateItemViewHolder(private val networkStateListItemBinding: NetworkStateListItemBinding) :
        RecyclerView.ViewHolder(networkStateListItemBinding.root) {
        /**
         * Binds [DataLoadingState] item to view.
         *
         * @param state [DataLoadingState] item to bind.
         */
        fun bind(state: DataLoadingState?) {
            networkStateListItemBinding.apply {
                networkDataLoadingState = state

                retryButton.setOnClickListener {
                    onRetryButtonClickedListener.invoke()
                }

                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        LIST_ITEM_TYPE_REGULAR -> {
            val moviesListItemBinding = MoviesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            moviesListItemBinding.lifecycleOwner = lifecycleOwner

            MovieDataItemViewHolder(moviesListItemBinding)
        }

        LIST_ITEM_TYPE_NETWORK_STATE -> {
            val networkStateListItemBinding = NetworkStateListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            networkStateListItemBinding.lifecycleOwner = lifecycleOwner

            NetworkDataLoadingStateItemViewHolder(networkStateListItemBinding)
        }

        else -> {
            throw IllegalArgumentException("Unsupported viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            LIST_ITEM_TYPE_REGULAR -> (holder as MovieDataItemViewHolder).bind(getItem(position)!!)
            LIST_ITEM_TYPE_NETWORK_STATE -> (holder as NetworkDataLoadingStateItemViewHolder).bind(
                dataLoadingState
            )
        }
    }

    override fun getItemViewType(position: Int) =
        // Only last element can be network state item, only if it should be displayed
        if (shouldHaveNetworkDataLoadingStateRow() && position == itemCount - 1) LIST_ITEM_TYPE_NETWORK_STATE else LIST_ITEM_TYPE_REGULAR

    override fun getItemCount() =
        // 1 additional network item should be added if there is a need to show network state
        super.getItemCount() + if (shouldHaveNetworkDataLoadingStateRow()) 1 else 0

    /**
     * Updates network state; show or hides additional network state item depending on need.
     *
     * @param newState  New [DataLoadingState] to update.
     */
    fun updateNetworkState(newState: DataLoadingState) {
        // Remembering previous state
        val previousState = dataLoadingState
        val previousShouldHaveNetworkDataLoadingStateRow = shouldHaveNetworkDataLoadingStateRow()

        // Applying new state
        dataLoadingState = newState

        // Checking if network state row should be displayed after applying new state
        val newShouldHaveNetworkDataLoadingStateRow = shouldHaveNetworkDataLoadingStateRow()

        if (previousShouldHaveNetworkDataLoadingStateRow != newShouldHaveNetworkDataLoadingStateRow) {
            // We need to remove or add the row
            if (previousShouldHaveNetworkDataLoadingStateRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (newShouldHaveNetworkDataLoadingStateRow && previousState != newState) {
            // We need to update the row
            notifyItemChanged(itemCount - 1)
        }
    }

    private fun shouldHaveNetworkDataLoadingStateRow() =
        dataLoadingState != null && dataLoadingState != DataLoadingState.SUCCESS

    /**
     * Code for calculating diffs between paged lists
     */
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MovieData>() {
            override fun areItemsTheSame(oldItem: MovieData, newItem: MovieData) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MovieData, newItem: MovieData) =
                oldItem == newItem
        }
    }
}
