package net.olewinski.themoviedbbrowser.application

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import net.olewinski.themoviedbbrowser.di.components.DaggerApplicationComponent
import net.olewinski.themoviedbbrowser.di.modules.ContextModule

@BindingMethods(
    BindingMethod(
        type = ImageView::class,
        attribute = "app:srcCompat",
        method = "setImageDrawable"
    )
)
class TheMovieDbBrowserApplication: Application() {
    val applicationComponent =
        DaggerApplicationComponent.builder().contextModule(ContextModule(this)).build()!!
}
