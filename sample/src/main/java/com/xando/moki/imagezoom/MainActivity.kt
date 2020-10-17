package com.xando.moki.imagezoom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xando.moki.imagezoom.ui.main.MainFragment

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}