package com.example.passoff

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    private var dbHandler: DBHandler? = null
    private val ADD_PASSWORD_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener {
            val intent = Intent(this, AddPasswordActivity::class.java)
            startActivityForResult(intent, ADD_PASSWORD_REQUEST_CODE)
        }

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressIndicator = findViewById(R.id.circularProgressIndicator)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PASSWORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val title = it.getStringExtra("title")
                val username = it.getStringExtra("username")
                val password = it.getStringExtra("password")
                val domain = it.getStringExtra("domain")

                if (title != null && username != null && password != null && domain != null) {
                    dbHandler = DBHandler(this)
                    dbHandler!!.addNewPassword(title, username, password, domain)
                    Toast.makeText(this, "Password has been added.", Toast.LENGTH_SHORT).show()
                    loadData()
                }
            }
        }
    }

    private fun loadData() {
        val searchBar = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        val appTitle = findViewById<android.widget.TextView>(R.id.appTitle)
        searchBar.visibility = android.view.View.INVISIBLE
        appTitle.visibility = android.view.View.VISIBLE

        val loadDataTask = LoadDataTask(this)
        loadDataTask.setRecyclerView(recyclerView)
        loadDataTask.setCircularProgressIndicator(progressIndicator)
        loadDataTask.execute()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_search -> {
                    val searchBar = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
                    val appTitle = findViewById<android.widget.TextView>(R.id.appTitle)
                    searchBar.visibility = android.view.View.VISIBLE
                    appTitle.visibility = android.view.View.INVISIBLE
                    true
                }
                R.id.navigation_quickshare -> {
                    Toast.makeText(this, "Quickshare selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        lateinit var progressIndicator: CircularProgressIndicator
        lateinit var recyclerView: RecyclerView
    }
}
