package net.olewinski.themoviedbbrowser.di.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext

/**
 * Dagger [Module] providing application's [Context].
 */
@Module
class ContextModule(private val application: Application) {
    @Provides
    @ApplicationContext
    fun getApplicationContext() = application.applicationContext!!
}
