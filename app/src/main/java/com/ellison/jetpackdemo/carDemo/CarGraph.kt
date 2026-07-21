package com.ellison.jetpackdemo.carDemo

import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named

@Component
interface InjectOnlyCarComponent {
    fun car(): InjectOnlyCar
    fun basicCar(): BasicCar
    fun completeCar(): CompleteCar
    fun secondaryConstructorCar(): SecondaryConstructorCar
    fun providerCarReporter(): ProviderCarReporter
}

@DisableInstallInCheck
@Module
object CarModule {
    @Provides
    @Named("carName")
    fun provideCarName(): String = "Dagger 示例汽车"

    @Provides
    @Named("carColor")
    fun provideCarColor(): String = "黑色"
}

@Component(modules = [CarModule::class])
interface ModuleCarComponent {
    fun car(): ModuleCar

    @Named("carName")
    fun carName(): String

    @Named("carColor")
    fun carColor(): String
}
