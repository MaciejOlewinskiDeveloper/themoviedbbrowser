package net.olewinski.themoviedbbrowser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.olewinski.themoviedbbrowser.data.models.NowPlaying
import net.olewinski.themoviedbbrowser.databinding.NowPlayingListItemBinding

class NowPlayingAdapter(private val onNowPlayingItemClickedListener: (NowPlaying) -> Unit) :
    PagedListAdapter<NowPlaying, NowPlayingAdapter.NowPlayingItemViewHolder>(DIFF_CALLBACK) {

    interface OnNowPlayingItemClickedListener {
        fun onNowPlayingItemClicked(item: NowPlaying)
    }

    inner class NowPlayingItemViewHolder(private val nowPlayingListItemBinding: NowPlayingListItemBinding) :
        RecyclerView.ViewHolder(nowPlayingListItemBinding.root) {
        fun bind(nowPlaying: NowPlaying) {
            nowPlayingListItemBinding.apply {
                item = nowPlaying

                root.setOnClickListener {
                    onNowPlayingItemClickedListener.invoke(nowPlaying)
                }

                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NowPlayingItemViewHolder(
        NowPlayingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

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
