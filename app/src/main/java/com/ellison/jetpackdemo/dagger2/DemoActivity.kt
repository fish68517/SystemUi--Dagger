package com.ellison.jetpackdemo.dagger2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ellison.jetpackdemo.MyApplication
import javax.inject.Inject

class DemoActivity : AppCompatActivity() {
    @Inject
    lateinit var presenter: DemoPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as MyApplication)
                .manualAppComponent
                .demoActivityComponentFactory()
                .create("Dagger2DemoActivity")
                .inject(this)

        val demoText = presenter.buildDemoText()
        val content = TextView(this).apply {
            text = demoText
            textSize = 16f
            setPadding(48, 48, 48, 48)
        }
        setContentView(content)
    }
}

