package com.example.passoff

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener    {
            addPasswordIntent()
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val progressIndicator: CircularProgressIndicator = findViewById(R.id.circularProgressIndicator)

        val loadDataTask = LoadDataTask(this)
        loadDataTask.setRecyclerView(recyclerView)
        loadDataTask.setCircularProgressIndicator(progressIndicator)
        loadDataTask.execute()




    }

    private fun addPasswordIntent() {
        val intent = Intent(this, addItem::class.java)
        startActivity(intent)
    }
}