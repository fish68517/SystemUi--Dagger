package com.ellison.jetpackdemo.dagger2

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.migration.DisableInstallInCheck

@DisableInstallInCheck
@Module(subcomponents = [DemoActivityComponent::class])
object SubcomponentsModule

@DisableInstallInCheck
@Module
object AppModule {
    @Provides
    @AppScope
    fun provideAppConfig(): AppConfig = AppConfig(appName = "JetpackDemo")
}

@DisableInstallInCheck
@Module
interface DemoActivityModule {
    @Binds
    @ActivityScope
    fun bindScreenTracker(tracker: LogcatScreenTracker): ScreenTracker
}

@ActivityScope
@Subcomponent(modules = [DemoActivityModule::class])
interface DemoActivityComponent {
    fun inject(activity: DemoActivity)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance @ScreenName screenName: String): DemoActivityComponent
    }
}

@AppScope
@Component(modules = [AppModule::class, SubcomponentsModule::class])
interface ManualAppComponent {
    fun demoActivityComponentFactory(): DemoActivityComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance @UserName userName: String): ManualAppComponent
    }
}

