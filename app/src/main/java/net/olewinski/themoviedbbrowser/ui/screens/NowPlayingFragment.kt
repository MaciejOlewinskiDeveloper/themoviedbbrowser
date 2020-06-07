package net.olewinski.themoviedbbrowser.ui.screens

import android.app.SearchManager
import android.database.Cursor
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import net.olewinski.themoviedbbrowser.R
import net.olewinski.themoviedbbrowser.application.TheMovieDbBrowserApplication
import net.olewinski.themoviedbbrowser.cloud.NetworkDataLoadingState
import net.olewinski.themoviedbbrowser.databinding.FragmentNowPlayingBinding
import net.olewinski.themoviedbbrowser.ui.adapters.NowPlayingAdapter
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

class NowPlayingFragment : Fragment() {

    private lateinit var nowPlayingBinding: FragmentNowPlayingBinding
    private lateinit var nowPlayingAdapter: NowPlayingAdapter

    private lateinit var nowPlayingViewModel: NowPlayingViewModel
    private lateinit var selectedMovieViewModel: SelectedMovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.let { activity ->
            nowPlayingViewModel = ViewModelProvider(
                viewModelStore,
                (activity.applicationContext as TheMovieDbBrowserApplication).applicationComponent.getNowPlayingViewModelFactory()
            ).get(NowPlayingViewModel::class.java)

            selectedMovieViewModel = ViewModelProvider(
                activity.viewModelStore,
                (activity.applicationContext as TheMovieDbBrowserApplication).applicationComponent.getSelectedMovieViewModelFactory()
            ).get(SelectedMovieViewModel::class.java)
        } ?: throw RuntimeException("Lack of Activity!")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nowPlayingBinding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        nowPlayingBinding.lifecycleOwner = viewLifecycleOwner

        return nowPlayingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nowPlayingAdapter = NowPlayingAdapter(viewLifecycleOwner, { item ->
            selectedMovieViewModel.selectMovie(item)
            findNavController().navigate(
                NowPlayingFragmentDirections.actionMoviesCollectionFragmentToMovieDetailsFragment(
                    item.id
                )
            )
        }, { item ->
            nowPlayingViewModel.onItemFavouriteToggleClicked(item)
        })

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.now_playing_list_menu, menu)

        initSearchItem(menu.findItem(R.id.search_item).actionView as SearchView)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.refresh_item -> {
            nowPlayingViewModel.refresh()

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initSearchItem(searchView: SearchView) {
        val simpleCursorAdapter = SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        nowPlayingViewModel.lastTypedSearchQuery?.also { currentSearchQuery ->
            searchView.isIconified = false
            searchView.setQuery(currentSearchQuery, false);
        }

        searchView.apply {
            suggestionsAdapter = simpleCursorAdapter
            isSubmitButtonEnabled = true

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    nowPlayingViewModel.requestSearchSuggestionsUpdate(newText)

                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    nowPlayingViewModel.searchMovies(query)

                    return true
                }
            })

            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int) = false

                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor = suggestionsAdapter.getItem(position) as Cursor
                    val selection =
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))

                    setQuery(selection, false)

                    return true
                }
            })

            setOnCloseListener {
                nowPlayingViewModel.showNowPlaying()
                false
            }
        }

        nowPlayingViewModel.searchSuggestions.observe(
            viewLifecycleOwner,
            Observer { searchSuggestions ->
                searchView.suggestionsAdapter.apply {
                    changeCursor(searchSuggestions)
                    notifyDataSetChanged()
                }
            })
    }
}
