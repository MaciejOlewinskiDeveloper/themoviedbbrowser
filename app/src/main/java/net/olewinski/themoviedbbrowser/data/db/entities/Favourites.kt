package net.olewinski.themoviedbbrowser.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_ENTITY_MOVIE_ID_COLUMN_NAME
import net.olewinski.themoviedbbrowser.data.db.DbSchemaConstants.FAVOURITES_ENTITY_TABLE_NAME

@Entity(tableName = FAVOURITES_ENTITY_TABLE_NAME)
data class Favourites(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = FAVOURITES_ENTITY_MOVIE_ID_COLUMN_NAME)
    val movieId: Long
)
