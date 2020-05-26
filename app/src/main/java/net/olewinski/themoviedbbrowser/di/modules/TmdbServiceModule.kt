package net.olewinski.themoviedbbrowser.di.modules

import dagger.Module
import dagger.Provides
import net.olewinski.themoviedbbrowser.BuildConfig
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService
import net.olewinski.themoviedbbrowser.cloud.service.TmdbService.Companion.TMDB_ENDPOINT
import net.olewinski.themoviedbbrowser.di.scopes.ApplicationScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class TmdbServiceModule {

    @Provides
    @ApplicationScope
    fun getTmdbService(): TmdbService {
        val okHttpClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }

        return Retrofit.Builder()
            .baseUrl(TMDB_ENDPOINT)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)
    }
}
