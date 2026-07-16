package com.ellison.jetpackdemo.hilt.model.analysis

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisModule {
    @Binds
    abstract fun bindAnalysisService(analysisService: AnalysisServiceImpl): AnalysisService
}
