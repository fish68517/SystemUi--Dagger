package com.ellison.jetpackdemo.dagger2

import javax.inject.Qualifier
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UserName

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenName

