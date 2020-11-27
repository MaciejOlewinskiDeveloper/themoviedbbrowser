package net.olewinski.themoviedbbrowser.di.modules

import net.olewinski.themoviedbbrowser.data.repository.MoviesRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { MoviesRepository(get(), get()) }
}
