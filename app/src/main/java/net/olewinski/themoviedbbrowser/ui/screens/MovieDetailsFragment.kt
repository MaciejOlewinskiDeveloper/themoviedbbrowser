package net.olewinski.themoviedbbrowser.ui.screens

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Transformations
import net.olewinski.themoviedbbrowser.R
import net.olewinski.themoviedbbrowser.application.TheMovieDbBrowserApplication
import net.olewinski.themoviedbbrowser.databinding.FragmentMovieDetailsBinding
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel

/**
 * Screen with movie details.
 */
class MovieDetailsFragment : Fragment() {

    private val selectedMovieViewModel: SelectedMovieViewModel by activityViewModels {
        (requireContext().applicationContext as TheMovieDbBrowserApplication).applicationComponent.getSelectedMovieViewModelFactory()
    }

    private lateinit var movieDetailsBinding: FragmentMovieDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        movieDetailsBinding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        movieDetailsBinding.lifecycleOwner = viewLifecycleOwner

        return movieDetailsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedMovieViewModel.selectedMovie.observe(viewLifecycleOwner, { movie ->
            movieDetailsBinding.item = movie

            (requireActivity() as AppCompatActivity).supportActionBar?.title = movie.title
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.movie_details_menu, menu)

        val favouriteStatusMenuItem = menu.findItem(R.id.favourite_item)

        Transformations.switchMap(selectedMovieViewModel.selectedMovie) { selectedMovie ->
            selectedMovie.favouriteStatus
        }.observe(viewLifecycleOwner, { isFavourite ->
            // Menu items don't support data binding yet, we need to change icon in old way
            favouriteStatusMenuItem.setIcon(if (isFavourite) R.drawable.ic_toolbar_favourite_enabled_24 else R.drawable.ic_toolbar_favourite_disabled_24)
            favouriteStatusMenuItem.setTitle(if (isFavourite) R.string.favourite_menu_item_disable_label else R.string.favourite_menu_item_enable_label)
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.favourite_item -> {
            selectedMovieViewModel.onItemFavouriteToggleClicked()

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
