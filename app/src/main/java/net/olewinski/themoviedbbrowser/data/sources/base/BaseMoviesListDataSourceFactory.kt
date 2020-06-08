package net.olewinski.themoviedbbrowser.data.sources.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import net.olewinski.themoviedbbrowser.data.models.MovieData

abstract class BaseMoviesListDataSourceFactory<T : BaseMoviesListDataSource<*>> :
    DataSource.Factory<Long, MovieData>() {
    private val mutableMoviesListDataSource = MutableLiveData<T>()
    val moviesListDataSource: LiveData<T> = mutableMoviesListDataSource

    override fun create(): DataSource<Long, MovieData> {
        val toReturn = getBaseMoviesListDataSource()

        mutableMoviesListDataSource.postValue(toReturn)

        return toReturn
    }

    abstract fun getBaseMoviesListDataSource(): T
}
