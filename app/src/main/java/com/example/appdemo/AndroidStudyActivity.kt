package com.example.appdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AndroidStudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_android_study)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.android_study_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btn_android_study_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_recycler_view).setOnClickListener {
            startActivity(Intent(this, RecyclerViewDemoActivity::class.java))
        }
    }
}
