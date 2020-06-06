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

    @Query("SELECT * FROM $FAVOURITES_DATA_ENTITY_TABLE_NAME")
    fun getAllFavouritesData(): LiveData<List<FavouritesData>>

    @Query("SELECT * FROM $FAVOURITES_DATA_ENTITY_TABLE_NAME WHERE $FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME=:movieId LIMIT 1")
    suspend fun getFavouritesDataForMovie(movieId: Long): FavouritesData?

    @Transaction
    suspend fun toggleDataPresence(movieId: Long) {
        if (getFavouritesDataForMovie(movieId) == null) {
            insert(FavouritesData(movieId))
        } else {
            delete((FavouritesData(movieId)))
        }
    }
}
