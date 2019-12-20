package com.flover.ocrapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.show_results_btn).setOnClickListener {
            var intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }

    }
}