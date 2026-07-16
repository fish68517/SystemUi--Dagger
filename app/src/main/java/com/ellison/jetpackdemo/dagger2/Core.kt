package com.ellison.jetpackdemo.dagger2

import android.util.Log
import javax.inject.Inject

data class AppConfig(val appName: String)

class AppLogger @Inject constructor() {
    fun log(tag: String, message: String) {
        Log.d(tag, message)
    }
}

class MovieRepository @Inject constructor(
        private val appConfig: AppConfig,
        private val logger: AppLogger,
        @UserName private val userName: String
) {
    fun search(keyword: String): String {
        val summary = "[$userName] search \"$keyword\" from ${appConfig.appName}"
        logger.log("Dagger2Repo", summary)
        return summary
    }
}

interface ScreenTracker {
    fun track(screenName: String)
}

class LogcatScreenTracker @Inject constructor(
        private val logger: AppLogger
) : ScreenTracker {
    override fun track(screenName: String) {
        logger.log("Dagger2Tracker", "Open screen: $screenName")
    }
}

@ActivityScope
class DemoPresenter @Inject constructor(
        private val movieRepository: MovieRepository,
        private val screenTracker: ScreenTracker,
        @ScreenName private val screenName: String
) {
    fun buildDemoText(): String {
        screenTracker.track(screenName)
        val searchResult = movieRepository.search("Batman")
        return """
            Pure Dagger2 demo is ready.
            - App scope object: ManualAppComponent
            - Activity scope object: DemoActivityComponent
            - Subcomponent runtime param: @ScreenName = $screenName
            - Repository result: $searchResult
        """.trimIndent()
    }
}

