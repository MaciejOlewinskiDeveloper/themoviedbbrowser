package net.olewinski.themoviedbbrowser.data.sources.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import net.olewinski.themoviedbbrowser.data.models.MovieData

/**
 * Base class for [DataSource.Factory]ies that produce given subtype of [BaseMoviesListDataSource]s.
 */
abstract class BaseMoviesListDataSourceFactory<T : BaseMoviesListDataSource<*>> :
    DataSource.Factory<Long, MovieData>() {
    private val mutableMoviesListDataSource = MutableLiveData<T>()

    /**
     * Current instance of subtype of [BaseMoviesListDataSource] produced by this factory.
     */
    val moviesListDataSource: LiveData<T> = mutableMoviesListDataSource

    override fun create(): DataSource<Long, MovieData> {
        val toReturn = getBaseMoviesListDataSource()

        mutableMoviesListDataSource.postValue(toReturn)

        return toReturn
    }

    /**
     * Used to create new instance of object extending [BaseMoviesListDataSource] that this factory
     * is intended to return from its [create] method.
     *
     * @return  New instance of object extending [BaseMoviesListDataSource]; will be used to return
     *          this instance from [create] method.
     */
    abstract fun getBaseMoviesListDataSource(): T
}
