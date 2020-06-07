package net.olewinski.themoviedbbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.databinding.NetworkStateListItemBinding
import net.olewinski.themoviedbbrowser.databinding.NowPlayingListItemBinding

private const val ITEM_TYPE_REGULAR = 0
private const val ITEM_TYPE_NETWORK_STATE = 1

class NowPlayingAdapter(
    // TODO Temporary solution, make ViewHolders to be LifecycleOwners
    private val viewLifecycleOwner: LifecycleOwner,
    private val onNowPlayingItemClickedListener: (NowPlaying) -> Unit,
    private val onItemFavouriteItemClickedListener: (NowPlaying) -> Unit,
    private val onRetryButtonClickedListener: () -> Unit
) : PagedListAdapter<NowPlaying, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    inner class NowPlayingItemViewHolder(private val nowPlayingListItemBinding: NowPlayingListItemBinding) :
        RecyclerView.ViewHolder(nowPlayingListItemBinding.root) {
        fun bind(nowPlaying: NowPlaying) {
            nowPlayingListItemBinding.apply {
                item = nowPlaying

                root.setOnClickListener {
                    onNowPlayingItemClickedListener.invoke(nowPlaying)
                }

                favouriteStatus.setOnClickListener {
                    onItemFavouriteItemClickedListener.invoke(nowPlaying)
                }

                executePendingBindings()
            }
        }
    }

    private var networkDataLoadingState: NetworkDataLoadingState? = null

    inner class NetworkDataLoadingStateItemViewHolder(private val networkStateListItemBinding: NetworkStateListItemBinding) :
        RecyclerView.ViewHolder(networkStateListItemBinding.root) {
        fun bind(state: NetworkDataLoadingState?) {
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
                val nowPlayingListItemBinding = NowPlayingListItemBinding.inflate(
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
                networkDataLoadingState
            )
        }
    }

    override fun getItemViewType(position: Int) =
        if (shouldHaveNetworkDataLoadingStateRow() && position == itemCount - 1) ITEM_TYPE_NETWORK_STATE else ITEM_TYPE_REGULAR

    override fun getItemCount() =
        super.getItemCount() + if (shouldHaveNetworkDataLoadingStateRow()) 1 else 0

    fun updateNetworkState(newState: NetworkDataLoadingState) {
        // Remembering previous state
        val previousState = networkDataLoadingState
        val previousShouldHaveNetworkDataLoadingStateRow = shouldHaveNetworkDataLoadingStateRow()

        // Applying new state
        networkDataLoadingState = newState

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
        networkDataLoadingState != null && networkDataLoadingState != NetworkDataLoadingState.READY

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NowPlaying>() {
            override fun areItemsTheSame(oldItem: NowPlaying, newItem: NowPlaying) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: NowPlaying, newItem: NowPlaying) =
                oldItem == newItem
        }
    }
}
