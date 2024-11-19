package com.example.passoff

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class itemView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val itemName = this.intent.getStringExtra("itemName")
        val username = this.intent.getStringExtra("username")
        val password = this.intent.getStringExtra("password")
        val domain = this.intent.getStringExtra("domain")

        val titleText = findViewById<TextView>(R.id.item_name)
        titleText.text = itemName

        val usernameText = findViewById<TextView>(R.id.item_username)
        usernameText.text = username

        val passwordText = findViewById<TextView>(R.id.item_password)
        passwordText.text = password

        val domainText = findViewById<TextView>(R.id.item_domain)
        domainText.text = domain

        this.title = itemName

        val deleteButton = findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener {
            val dbHandler = DBHandler(this)
            val success = dbHandler.deletePassword(this.intent.getIntExtra("id", -1))
            val loadDataTask = LoadDataTask(this)
            loadDataTask.setRecyclerView(MainActivity.recyclerView)
            loadDataTask.setCircularProgressIndicator(MainActivity.progressIndicator)
            loadDataTask.execute()
            if (success) {
                Log.d("Database", "Password deleted successfully")
            } else {
                Log.e("Database", "Failed to delete password")
            }
            finish()
        }

    }
}