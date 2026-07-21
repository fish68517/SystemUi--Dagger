package com.ellison.jetpackdemo.carDemo

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

class Engine @Inject constructor() {
    fun start(): String = "Engine created by @Inject constructor"
}

class InjectOnlyCar @Inject constructor(
    private val engine: Engine
) {
    fun run(): String = "${engine.start()} -> InjectOnlyCar runs without Module"
}

class ModuleCar @Inject constructor(
    private val engine: Engine,
    @Named("carName") private val carName: String,
    @Named("carColor") private val carColor: String
) {
    fun run(): String = "${engine.start()} -> $carColor $carName runs with @Provides values"
}

class Body @Inject constructor() {
    fun assemble(): String = "Body created by @Inject constructor"
}

class BasicCar @Inject constructor(
    private val engine: Engine
) {
    fun run(): String = "${engine.start()} -> BasicCar uses the only @Inject constructor"
}

class CompleteCar @Inject constructor(
    private val engine: Engine,
    private val body: Body
) {
    fun run(): String = "${engine.start()} + ${body.assemble()} -> CompleteCar"
}

class SecondaryConstructorCar @Inject constructor(
    private val engine: Engine
) {
    private var body: Body? = null

    constructor(engine: Engine, body: Body) : this(engine) {
        this.body = body
    }

    fun run(): String {
        val bodyState = body?.assemble() ?: "secondary constructor was not used by Dagger"
        return "${engine.start()} -> $bodyState"
    }
}

class ProviderCarReporter @Inject constructor(
    private val carProvider: Provider<InjectOnlyCar>
) {
    fun report(): String {
        val first = carProvider.get()
        val second = carProvider.get()
        return "Provider.get() lazily requests cars; same instance = ${first === second}; ${first.run()}"
    }
}
