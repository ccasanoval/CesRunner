package com.cesoft.cesrunner

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


//TODO: Export routes to KML

//TODO: Save in DB Vo2Max and after a run, check if it's a new high and congratulate user!


//TO READ: SIDE EFFECTS: https://medium.com/@ramadan123sayed/understanding-and-handling-side-effects-in-jetpack-compose-a-comprehensive-guide-85b219e495f1
//TO READ: PERF. FLOW: https://bladecoder.medium.com/kotlins-flow-in-viewmodels-it-s-complicated-556b472e281a
//TO READ: PERF. FLOW: https://stackoverflow.com/questions/78277363/collecting-flows-in-the-viewmodel
//TO READ: PERF. FLOW: https://bladecoder.medium.com/smarter-shared-kotlin-flows-d6b75fc66754
//TO READ: PERF. STABILITY: https://developer.android.com/develop/ui/compose/performance/stability/fix
//TO READ: PERF. STABILITY: https://medium.com/androiddevelopers/jetpack-compose-stability-explained-79c10db270c8

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(appModule)
        }
    }
}
