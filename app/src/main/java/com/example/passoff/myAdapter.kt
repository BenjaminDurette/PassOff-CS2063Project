package com.example.passoff

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val parentActivity: Activity, private val mDataset: ArrayList<PassItem>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // Update ViewHolder to hold all TextViews in item_layout
    class ViewHolder(var mTextView: TextView) : RecyclerView.ViewHolder(mTextView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false) as TextView
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
            parentActivity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    companion object {
        private const val TAG = "myAdapter"
    }
}
