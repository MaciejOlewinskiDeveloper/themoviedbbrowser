package net.olewinski.themoviedbbrowser.application

import android.app.Application
import net.olewinski.themoviedbbrowser.di.components.DaggerApplicationComponent
import net.olewinski.themoviedbbrowser.di.modules.ContextModule

class TheMovieDbBrowserApplication: Application() {
    val applicationComponent =
        DaggerApplicationComponent.builder().contextModule(ContextModule(this)).build()!!
}
