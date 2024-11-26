package com.example.passoff

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val parentActivity: Activity, private val mDataset: ArrayList<PassItem>, private val jsonUtils: JsonUtils) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // Update ViewHolder to hold all TextViews in item_layout
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mTextView: TextView = view.findViewById(R.id.item_textview)
        val deleteButton: Button = view.findViewById(R.id.delete_button) // Reference to the delete button
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataset[position]

        // Set the data for each TextView
        holder.mTextView.text = item.name

        holder.itemView.setOnClickListener {
            val intent = Intent(parentActivity, itemView::class.java) // Use the correct target activity
            // Pass all necessary data to the itemView activity
            intent.putExtra("itemName", item.name)
            intent.putExtra("username", item.username)
            intent.putExtra("password", item.password)
            intent.putExtra("domain", item.domain)
            intent.putExtra("id", item.id)
            parentActivity.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            jsonUtils.deletePassItem(parentActivity, item.title) // Use the title to delete the item
            Toast.makeText(parentActivity, "Item successfully deleted", Toast.LENGTH_SHORT).show()
            // Finish the current MainActivity and restart it
            val intent = Intent(parentActivity, MainActivity::class.java)
            parentActivity.finish() // Finish the current instance
            parentActivity.startActivity(intent) // Start a new instance

        }

    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    companion object {
        private const val TAG = "myAdapter"
    }
}
