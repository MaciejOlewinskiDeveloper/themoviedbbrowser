package net.olewinski.themoviedbbrowser.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
    fun getFavouritesDataForMovie(movieId: Long): LiveData<FavouritesData>
}
