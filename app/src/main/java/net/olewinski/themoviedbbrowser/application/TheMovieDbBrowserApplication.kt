package net.olewinski.themoviedbbrowser.application

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import net.olewinski.themoviedbbrowser.di.components.DaggerApplicationComponent
import net.olewinski.themoviedbbrowser.di.modules.ContextModule

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
    val applicationComponent =
        DaggerApplicationComponent.builder().contextModule(ContextModule(this)).build()!!
}
