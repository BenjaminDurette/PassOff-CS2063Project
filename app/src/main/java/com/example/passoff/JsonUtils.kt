package com.example.passoff

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONObject
import org.json.JSONException
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class JsonUtils(context: Context) {

    private lateinit var items: ArrayList<PassItem>

    fun processJSON(context: Context): ArrayList<PassItem> {
        items = ArrayList() // Initialize items here
        try {
            val jsonString = loadJSONFromAssets(context)
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("items")

            for (i in 0 until jsonArray.length()) {
                val itemObject = jsonArray.getJSONObject(i)
                val passItem = PassItem(
                    itemObject.getString("itemName"),
                    itemObject.getString("username"),
                    itemObject.getString("password"),
                    itemObject.getString("domain")
                )
                items.add(passItem)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return items
    }



    private fun loadJSONFromAssets(context: Context):String    {
        val inputString = context.assets.open("Items.json").bufferedReader().use { it.readText() }
        return inputString
    }

    companion object {
        private const val TAG = "JsonUtils"
    }

    init    {
        processJSON(context)
    }
}