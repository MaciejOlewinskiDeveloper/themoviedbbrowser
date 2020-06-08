package net.olewinski.themoviedbbrowser.data.models

import androidx.lifecycle.LiveData
import com.google.gson.annotations.SerializedName

data class MovieData(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("original_title")
    val originalTitle: String,

    @SerializedName("overview")
    val overview: String,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("vote_average")
    val voteAverage: Double,

    @SerializedName("vote_count")
    val votesCount: Int,

    @SerializedName("poster_path")
    val posterPath: String,

    @SerializedName("backdrop_path")
    val backdropPath: String,

    @Transient
    var favouriteStatus: LiveData<Boolean>
)
