package net.olewinski.themoviedbbrowser.di.qualifiers

import android.content.Context
import javax.inject.Qualifier

/**
 * [Qualifier] for application's [Context]
 */
@Qualifier
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ApplicationContext
