package net.olewinski.themoviedbbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.data.models.MovieData
import net.olewinski.themoviedbbrowser.databinding.MoviesListItemBinding
import net.olewinski.themoviedbbrowser.databinding.NetworkStateListItemBinding

private const val ITEM_TYPE_REGULAR = 0
private const val ITEM_TYPE_NETWORK_STATE = 1

class MoviesListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val onNowPlayingItemClickedListener: (MovieData) -> Unit,
    private val onItemFavouriteItemClickedListener: (MovieData) -> Unit,
    private val onRetryButtonClickedListener: () -> Unit
) : PagedListAdapter<MovieData, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    inner class NowPlayingItemViewHolder(private val moviesListItemBinding: MoviesListItemBinding) :
        RecyclerView.ViewHolder(moviesListItemBinding.root) {
        fun bind(movieData: MovieData) {
            moviesListItemBinding.apply {
                item = movieData

                root.setOnClickListener {
                    onNowPlayingItemClickedListener.invoke(movieData)
                }

                favouriteStatus.setOnClickListener {
                    onItemFavouriteItemClickedListener.invoke(movieData)
                }

                executePendingBindings()
            }
        }
    }

    private var dataLoadingState: DataLoadingState? = null

    inner class NetworkDataLoadingStateItemViewHolder(private val networkStateListItemBinding: NetworkStateListItemBinding) :
        RecyclerView.ViewHolder(networkStateListItemBinding.root) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_REGULAR -> {
                val nowPlayingListItemBinding = MoviesListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                nowPlayingListItemBinding.lifecycleOwner = viewLifecycleOwner

                NowPlayingItemViewHolder(nowPlayingListItemBinding)
            }

            ITEM_TYPE_NETWORK_STATE -> {
                val networkStateListItemBinding = NetworkStateListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                networkStateListItemBinding.lifecycleOwner = viewLifecycleOwner

                NetworkDataLoadingStateItemViewHolder(networkStateListItemBinding)
            }

            else -> {
                throw IllegalArgumentException("Unsupported viewType: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_TYPE_REGULAR -> (holder as NowPlayingItemViewHolder).bind(getItem(position)!!)
            ITEM_TYPE_NETWORK_STATE -> (holder as NetworkDataLoadingStateItemViewHolder).bind(
                dataLoadingState
            )
        }
    }

    override fun getItemViewType(position: Int) =
        if (shouldHaveNetworkDataLoadingStateRow() && position == itemCount - 1) ITEM_TYPE_NETWORK_STATE else ITEM_TYPE_REGULAR

    override fun getItemCount() =
        super.getItemCount() + if (shouldHaveNetworkDataLoadingStateRow()) 1 else 0

    fun updateNetworkState(newState: DataLoadingState) {
        // Remembering previous state
        val previousState = dataLoadingState
        val previousShouldHaveNetworkDataLoadingStateRow = shouldHaveNetworkDataLoadingStateRow()

        // Applying new state
        dataLoadingState = newState

        // Checking if network state row should be displayed
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

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MovieData>() {
            override fun areItemsTheSame(oldItem: MovieData, newItem: MovieData) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MovieData, newItem: MovieData) =
                oldItem == newItem
        }
    }
}
