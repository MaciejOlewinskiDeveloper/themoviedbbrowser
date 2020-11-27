package net.olewinski.themoviedbbrowser.di.modules

import android.content.Context
import androidx.room.Room
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants
import net.olewinski.themoviedbbrowser.data.db.TheMovieDbBrowserDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { getTheMovieDbBrowserDatabase(get()) }
}

fun getTheMovieDbBrowserDatabase(context: Context): TheMovieDbBrowserDatabase =
    Room.databaseBuilder(
        context,
        TheMovieDbBrowserDatabase::class.java,
        DbSchemaConstants.THE_MOVIE_DB_BROWSER_DATABASE_NAME
    ).build()
