package net.olewinski.themoviedbbrowser.di.modules

import net.olewinski.themoviedbbrowser.viewmodels.MoviesListViewModel
import net.olewinski.themoviedbbrowser.viewmodels.SelectedMovieViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MoviesListViewModel(get()) }
    viewModel { SelectedMovieViewModel(get()) }
}
