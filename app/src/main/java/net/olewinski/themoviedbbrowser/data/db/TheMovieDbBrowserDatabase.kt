package net.olewinski.themoviedbbrowser.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.olewinski.themoviedbbrowser.data.db.dao.FavouritesDataDao
import net.olewinski.themoviedbbrowser.data.db.entities.FavouritesData

const val DATABASE_VERSION = 1

@Database(
    entities = [FavouritesData::class],
    version = DATABASE_VERSION
)
abstract class TheMovieDbBrowserDatabase : RoomDatabase() {
    abstract fun getFavouritesDataDao(): FavouritesDataDao
}
