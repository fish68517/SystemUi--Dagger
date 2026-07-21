package com.ellison.jetpackdemo.carDemo

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CarDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Car Dagger Demo"

        val injectOnlyComponent = DaggerInjectOnlyCarComponent.create()
        val moduleComponent = DaggerModuleCarComponent.create()

        val text = buildString {
            appendLine("Car Dagger demo is ready.")
            appendLine()
            appendLine("1. 删除 @Module / @Provides 的纯 @Inject 版本")
            appendLine(injectOnlyComponent.car().run())
            appendLine()
            appendLine("2. @Module + @Provides + @Component 调用逻辑")
            appendLine("Component method carName() does not match by method name.")
            appendLine("It requests key @Named(\"carName\") String -> ${moduleComponent.carName()}")
            appendLine("It requests key @Named(\"carColor\") String -> ${moduleComponent.carColor()}")
            appendLine(moduleComponent.car().run())
            appendLine()
            appendLine("3. 多个 @Inject 构造函数的正确理解")
            appendLine("Dagger requires only one @Inject constructor per class.")
            appendLine(injectOnlyComponent.basicCar().run())
            appendLine(injectOnlyComponent.completeCar().run())
            appendLine(injectOnlyComponent.secondaryConstructorCar().run())
            appendLine()
            appendLine("4. Provider<T> 延迟获取")
            appendLine(injectOnlyComponent.providerCarReporter().report())
            appendLine()
            appendLine("Core rule: dependency key = type + qualifier, not method name.")
        }

        val content = TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(48, 48, 48, 48)
        }

        setContentView(ScrollView(this).apply {
            addView(content)
        })
    }
}
