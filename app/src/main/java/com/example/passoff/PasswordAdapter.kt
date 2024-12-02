package com.example.passoff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(private val passwordList: ArrayList<PassItem>) :
    RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.item_name)
        val usernameTextView: TextView = itemView.findViewById(R.id.item_username)
        val passwordTextView: TextView = itemView.findViewById(R.id.item_password)
        val domainTextView: TextView = itemView.findViewById(R.id.item_domain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.password_detail, parent, false)
        return PasswordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val currentPassword = passwordList[position]
        holder.titleTextView.text = currentPassword.title
        holder.usernameTextView.text = currentPassword.username
        holder.passwordTextView.text = currentPassword.password
        holder.domainTextView.text = currentPassword.domain
    }

    override fun getItemCount() = passwordList.size
}