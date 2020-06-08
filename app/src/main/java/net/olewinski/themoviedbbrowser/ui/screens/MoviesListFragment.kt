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
import net.olewinski.themoviedbbrowser.cloud.DataLoadingState
import net.olewinski.themoviedbbrowser.databinding.FragmentMoviesListBinding
import net.olewinski.themoviedbbrowser.ui.adapters.MoviesListAdapter
import net.olewinski.themoviedbbrowser.viewmodels.MovieDetailsNavigationRequest
import net.olewinski.themoviedbbrowser.viewmodels.MoviesListViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

class MoviesListFragment : Fragment() {

    private lateinit var moviesListBinding: FragmentMoviesListBinding
    private lateinit var moviesListAdapter: MoviesListAdapter

    private lateinit var moviesListViewModel: MoviesListViewModel
    private lateinit var selectedMovieViewModel: SelectedMovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.let { activity ->
            moviesListViewModel = ViewModelProvider(
                viewModelStore,
                (activity.applicationContext as TheMovieDbBrowserApplication).applicationComponent.getMoviesListViewModelFactory()
            ).get(MoviesListViewModel::class.java)

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
        moviesListBinding = FragmentMoviesListBinding.inflate(inflater, container, false)
        moviesListBinding.lifecycleOwner = viewLifecycleOwner

        return moviesListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moviesListViewModel.navigationRequest.observe(
            viewLifecycleOwner,
            Observer { navigationRequestOneTimeEvent ->
                navigationRequestOneTimeEvent.getContent()?.let { navigationRequest ->
                    when (navigationRequest) {
                        is MovieDetailsNavigationRequest -> {
                            selectedMovieViewModel.selectMovie(navigationRequest.movieData)
                            findNavController().navigate(MoviesListFragmentDirections.actionMoviesCollectionFragmentToMovieDetailsFragment())
                        }
                    }
                }
            })

        moviesListAdapter = MoviesListAdapter(viewLifecycleOwner, { item ->
            moviesListViewModel.onItemClicked(item)
        }, { item ->
            moviesListViewModel.onItemFavouriteToggleClicked(item)
        }, {
            moviesListViewModel.retry()
        })

        moviesListBinding.nowPlayingList.apply {
            adapter = moviesListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        moviesListViewModel.pagedData.observe(viewLifecycleOwner, Observer {
            moviesListAdapter.submitList(it)
        })

        moviesListViewModel.networkState.observe(viewLifecycleOwner, Observer {
            moviesListAdapter.updateNetworkState(it)
        })

        moviesListViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            moviesListBinding.swipeToRefresh.isRefreshing = it == DataLoadingState.LOADING
        })

        moviesListBinding.swipeToRefresh.setOnRefreshListener {
            moviesListViewModel.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.movies_list_menu, menu)

        initSearchItem(menu.findItem(R.id.search_item).actionView as SearchView)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.refresh_item -> {
            moviesListViewModel.refresh()

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

        moviesListViewModel.lastTypedSearchQuery?.also { currentSearchQuery ->
            searchView.isIconified = false
            searchView.setQuery(currentSearchQuery, false)
        }

        searchView.apply {
            suggestionsAdapter = simpleCursorAdapter
            isSubmitButtonEnabled = true

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    moviesListViewModel.requestSearchSuggestionsUpdate(newText)

                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    moviesListViewModel.searchMovies(query)

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
                moviesListViewModel.showNowPlaying()
                false
            }
        }

        moviesListViewModel.searchSuggestions.observe(
            viewLifecycleOwner,
            Observer { searchSuggestions ->
                searchView.suggestionsAdapter.apply {
                    changeCursor(searchSuggestions)
                    notifyDataSetChanged()
                }
            })
    }
}
