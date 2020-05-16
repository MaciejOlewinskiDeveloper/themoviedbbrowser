package net.olewinski.themoviedbbrowser.di.components

import android.content.Context
import dagger.Component
import net.olewinski.themoviedbbrowser.di.modules.ContextModule
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope

@ApplicationScope
@Component(modules = [ContextModule::class])
interface ApplicationComponent {
    @ApplicationContext
    fun getApplicationContext(): Context
}
