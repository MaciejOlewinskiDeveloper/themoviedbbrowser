package net.olewinski.themoviedbbrowser.di.modules

import android.app.Application
import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext

@Module
class ContextModule(private val application: Application) {

    @Provides
    @ApplicationContext
    fun getApplicationContext() = application.applicationContext
}
