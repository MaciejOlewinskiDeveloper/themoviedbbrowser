package net.olewinski.themoviedbbrowser.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_DATA_ENTITY_TABLE_NAME

@Entity(tableName = FAVOURITES_DATA_ENTITY_TABLE_NAME)
data class FavouritesData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = FAVOURITES_DATA_ENTITY_MOVIE_ID_COLUMN_NAME)
    val movieId: Long
)
