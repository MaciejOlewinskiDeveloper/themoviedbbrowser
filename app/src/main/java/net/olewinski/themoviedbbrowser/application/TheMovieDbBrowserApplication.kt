package net.olewinski.themoviedbbrowser.application

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import net.olewinski.themoviedbbrowser.di.modules.databaseModule
import net.olewinski.themoviedbbrowser.di.modules.networkModule
import net.olewinski.themoviedbbrowser.di.modules.repositoryModule
import net.olewinski.themoviedbbrowser.di.modules.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Fix for issues with DataBinding+srcCompat attribute (for Vector drawables) on Android 4.4
// Redirecting srcCompat attribute to safe setImageDrawable method
@BindingMethods(
    BindingMethod(
        type = ImageView::class,
        attribute = "app:srcCompat",
        method = "setImageDrawable"
    )
)
/**
 * Application class; needed for holding Dagger component with global parts of the app
 */
class TheMovieDbBrowserApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TheMovieDbBrowserApplication)
            modules(listOf(databaseModule, networkModule, repositoryModule, viewModelsModule))
        }
    }
}
