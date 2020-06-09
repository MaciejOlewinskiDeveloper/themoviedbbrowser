package net.olewinski.themoviedbbrowser.di.scopes

import javax.inject.Scope

/**
 * [Scope] for application's global singletons
 */
@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ApplicationScope
