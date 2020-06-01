package net.olewinski.themoviedbbrowser.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.THE_MOVIE_DB_BROWSER_DATABASE_NAME
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import net.olewinski.themoviedbbrowser.di.qualifiers.ApplicationContext
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope

@Module(includes = [ContextModule::class])
class DatabaseModule {
    @Provides
    @ApplicationScope
    fun getTheMovieDbBrowserDatabase(@ApplicationContext context: Context): TheMovieDbBrowserDatabase =
        Room.databaseBuilder(
            context,
            TheMovieDbBrowserDatabase::class.java,
            THE_MOVIE_DB_BROWSER_DATABASE_NAME
        ).build()
}
