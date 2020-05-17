package net.olewinski.themoviedbbrowser.data.models

import com.google.gson.annotations.SerializedName

data class NowPlaying(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("original_title")
    val originalTitle: String,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("poster_path")
    val posterPath: String
)
