package net.olewinski.themoviedbbrowser.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_DATA_ENTITY_TABLE_NAME
import net.olewinski.themoviedbbrowser.data.db.entities.FavouritesData

@Dao
interface FavouritesDataDao {
    @Insert
    suspend fun insert(favouritesData: FavouritesData): Long

    @Delete
    suspend fun delete(favouritesData: FavouritesData)

    @Query("SELECT $FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME FROM $FAVOURITES_DATA_ENTITY_TABLE_NAME")
    fun getAllFavouritesMoviesIds(): LiveData<List<Long>>

    @Query("SELECT COUNT(*) FROM $FAVOURITES_DATA_ENTITY_TABLE_NAME WHERE $FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME=:movieId")
    suspend fun countDataItemsForMovieId(movieId: Long): Long

    @Transaction
    suspend fun toggleFavouritesDataPresenceForMovie(movieId: Long) {
        if (countDataItemsForMovieId(movieId) > 0L) {
            delete((FavouritesData(movieId)))
        } else {
            insert(FavouritesData(movieId))
        }
    }
}
