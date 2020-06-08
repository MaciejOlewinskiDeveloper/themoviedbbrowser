package net.olewinski.themoviedbbrowser.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_ENTITY_MOVIE_ID_COLUMN_NAME
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_ENTITY_TABLE_NAME
import net.olewinski.themoviedbbrowser.data.db.entities.Favourites

@Dao
interface FavouritesDao {
    @Insert
    suspend fun insert(favourites: Favourites): Long

    @Delete
    suspend fun delete(favourites: Favourites)

    @Query("SELECT $FAVOURITES_ENTITY_MOVIE_ID_COLUMN_NAME FROM $FAVOURITES_ENTITY_TABLE_NAME")
    fun getAllFavouritesMoviesIds(): LiveData<List<Long>>

    @Query("SELECT COUNT(*) FROM $FAVOURITES_ENTITY_TABLE_NAME WHERE $FAVOURITES_ENTITY_MOVIE_ID_COLUMN_NAME=:movieId")
    suspend fun countDataItemsForMovieId(movieId: Long): Long

    @Transaction
    suspend fun toggleFavouritesStatusForMovie(movieId: Long) {
        if (countDataItemsForMovieId(movieId) > 0L) {
            delete((Favourites(movieId)))
        } else {
            insert(Favourites(movieId))
        }
    }
}
