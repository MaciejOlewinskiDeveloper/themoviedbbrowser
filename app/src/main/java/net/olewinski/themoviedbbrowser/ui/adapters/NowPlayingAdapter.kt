package net.olewinski.themoviedbbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.databinding.NowPlayingListItemBinding

class NowPlayingAdapter(
    // TODO Temporary solution, make ViewHolders to be LifecycleOwners
    private val viewLifecycleOwner: LifecycleOwner,
    private val onNowPlayingItemClickedListener: (NowPlaying) -> Unit,
    private val onItemFavouriteItemClickedListener: (NowPlaying) -> Unit
) : PagedListAdapter<NowPlaying, NowPlayingAdapter.NowPlayingItemViewHolder>(DIFF_CALLBACK) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NowPlayingItemViewHolder {
        val nowPlayingListItemBinding =
            NowPlayingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        nowPlayingListItemBinding.lifecycleOwner = viewLifecycleOwner

        return NowPlayingItemViewHolder(nowPlayingListItemBinding)
    }

    override fun onBindViewHolder(holder: NowPlayingItemViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(item)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NowPlaying>() {
            override fun areItemsTheSame(oldItem: NowPlaying, newItem: NowPlaying) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: NowPlaying, newItem: NowPlaying) =
                oldItem == newItem
        }
    }
}
