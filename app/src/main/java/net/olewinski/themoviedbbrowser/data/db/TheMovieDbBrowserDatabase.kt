package net.olewinski.themoviedbbrowser.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.olewinski.themoviedbbrowser.data.db.dao.FavouritesDao
import net.olewinski.themoviedbbrowser.data.db.entities.Favourites

const val DATABASE_VERSION = 1

/**
 * Definition of Room library's database
 */
@Database(entities = [Favourites::class], version = DATABASE_VERSION)
abstract class TheMovieDbBrowserDatabase : RoomDatabase() {
    abstract fun getFavouritesDao(): FavouritesDao
}
