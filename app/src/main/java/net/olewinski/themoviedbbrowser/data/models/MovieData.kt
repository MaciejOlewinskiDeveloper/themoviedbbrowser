package net.olewinski.themoviedbbrowser.data.models

import androidx.lifecycle.LiveData
import com.google.gson.annotations.SerializedName

/**
 * Model of movie's data
 */
data class MovieData(
    /**
     * Movie's ID
     */
    @SerializedName("id")
    val id: Long,

    /**
     * Movie's title
     */
    @SerializedName("title")
    val title: String,

    /**
     * Movie's original title
     */
    @SerializedName("original_title")
    val originalTitle: String,

    /**
     * Movie's short description ("overview")
     */
    @SerializedName("overview")
    val overview: String,

    /**
     * Release date in textual form
     */
    @SerializedName("release_date")
    val releaseDate: String,

    /**
     * Average rate based on users' votes
     */
    @SerializedName("vote_average")
    val voteAverage: Double,

    /**
     * Number of users' votes
     */
    @SerializedName("vote_count")
    val votesCount: Int,

    /**
     * Last part of URL for downloading movie's poster
     */
    @SerializedName("poster_path")
    val posterPath: String,

    /**
     * Last part of URL for downloading movie's poster
     */
    @SerializedName("backdrop_path")
    val backdropPath: String,

    /**
     * Observable movie's favourite status
     */
    @Transient
    var favouriteStatus: LiveData<Boolean>
)
