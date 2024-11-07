package com.example.passoff
import android.content.Context

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.concurrent.Executors

class LoadDataTask(private val activity: AppCompatActivity) {

    private val appContext: Context = activity.applicationContext
    private lateinit var recyclerView: RecyclerView
    private lateinit var circularProgressIndicator: CircularProgressIndicator

    fun setRecyclerView(recyclerView: RecyclerView): LoadDataTask {
        this.recyclerView = recyclerView
        return this
    }

    fun setCircularProgressIndicator(circularProgressIndicator: CircularProgressIndicator): LoadDataTask {
        this.circularProgressIndicator = circularProgressIndicator
        return this
    }

    fun execute() {
        // Show the progress indicator on the main thread before starting background work
        activity.runOnUiThread {
            circularProgressIndicator.visibility = ProgressBar.VISIBLE
        }

        // Perform the data loading in a background thread
        Executors.newSingleThreadExecutor().execute {
            val dbHandler = DBHandler(appContext)
            val newItems = dbHandler.getPasswords()  // Load data from the database

            // Update the UI on the main thread after loading the data
            Handler(Looper.getMainLooper()).post {
                updateDisplay(newItems)
            }
        }
    }

    private fun updateDisplay(newItems: ArrayList<PassItem>) {
        setupRecyclerView(newItems)
        circularProgressIndicator.visibility = ProgressBar.INVISIBLE
        Toast.makeText(appContext, "Data loaded successfully", Toast.LENGTH_SHORT).show()

    }

    private fun setupRecyclerView(newItems: ArrayList<PassItem>) {
        recyclerView.adapter = MyAdapter(activity, newItems)
    }

    companion object {
        private const val TAG = "LoadDataTask"
    }
}