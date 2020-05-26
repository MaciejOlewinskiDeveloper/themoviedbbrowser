package net.olewinski.themoviedbbrowser.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Provides
import net.olewinski.themoviedbbrowser.application.TheMovieDbBrowserApplication
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState
import net.olewinski.themoviedbbrowser.data.repository.NowPlayingRepository
import net.olewinski.themoviedbbrowser.databinding.FragmentNowPlayingBinding
import net.olewinski.themoviedbbrowser.ui.adapters.NowPlayingAdapter
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel
import java.lang.RuntimeException

class NowPlayingFragment : Fragment() {

    private lateinit var nowPlayingBinding: FragmentNowPlayingBinding
    private lateinit var nowPlayingAdapter: NowPlayingAdapter

    private lateinit var nowPlayingViewModel: NowPlayingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nowPlayingViewModel = activity?.let { activity ->
            ViewModelProvider(
                viewModelStore,
                (activity.applicationContext as TheMovieDbBrowserApplication).applicationComponent.getNowPlayingViewModelFactory()
            ).get(NowPlayingViewModel::class.java)
        } ?: throw RuntimeException("Lack of Activity!")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        nowPlayingBinding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        nowPlayingBinding.lifecycleOwner = viewLifecycleOwner

        return nowPlayingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nowPlayingAdapter = NowPlayingAdapter { item ->
            findNavController().navigate(NowPlayingFragmentDirections.actionMoviesCollectionFragmentToMovieDetailsFragment(item.id))
        }

        nowPlayingBinding.nowPlayingList.apply {
            adapter = nowPlayingAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        nowPlayingViewModel.pagedData.observe(viewLifecycleOwner, Observer {
            nowPlayingAdapter.submitList(it)
        })

        nowPlayingViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            nowPlayingBinding.swipeToRefresh.isRefreshing = it == NetworkDataLoadingState.LOADING
        })
        nowPlayingBinding.swipeToRefresh.setOnRefreshListener {
            nowPlayingViewModel.refresh()
        }
    }
}
