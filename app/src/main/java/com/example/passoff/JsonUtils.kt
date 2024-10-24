package com.example.passoff

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import java.io.FileOutputStream
import java.io.IOException

class JsonUtils(context: Context) {

    private lateinit var items: ArrayList<PassItem>

    fun processJSON(context: Context): ArrayList<PassItem> {
        items = ArrayList() // Initialize items here
        try {
            val jsonString = loadJSONFromFile(context)
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

    // Adding a new PassItem
    fun addPassItem(context: Context,passItem: PassItem) {
        items.add(passItem)
        saveToJSON(context) // Save the updated list back to JSON
    }

    // Editing an existing PassItem (searching by item name)
    fun editPassItem(context: Context,title: String, updatedItem: PassItem) {
        val index = items.indexOfFirst { it.title == title }
        if (index != -1) {
            items[index] = updatedItem
            saveToJSON(context) // Save the updated list back to JSON
        }
    }

    // Deleting a PassItem by item name
    fun deletePassItem(context: Context, title: String) {
        items.removeAll { it.title == title }
        saveToJSON(context) // Save the updated list back to JSON
    }



    private fun loadJSONFromAssets(context: Context):String    {
        val inputString = context.assets.open("Items.json").bufferedReader().use { it.readText() }
        return inputString
    }

    private fun loadJSONFromFile(context: Context): String {
        val file = File(context.filesDir, "Items.json")
        if (!file.exists()) {
            copyJSONFromAssets(context) // Copy from assets if it doesn't exist
        }
        return file.readText() // Read from internal storage
    }

    private fun copyJSONFromAssets(context: Context) {
        val file = File(context.filesDir, "Items.json")
        try {
            val inputStream: InputStream = context.assets.open("Items.json")
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveToJSON(context: Context) {
        try {
            val jsonArray = JSONArray()
            for (item in items) {
                val itemObject = JSONObject()
                itemObject.put("itemName", item.title)
                itemObject.put("username", item.username)
                itemObject.put("password", item.password)
                itemObject.put("domain", item.domain)
                jsonArray.put(itemObject)
            }

            val jsonObject = JSONObject()
            jsonObject.put("items", jsonArray)

            // Write the updated JSON to the internal storage file (Items.json)
            val file = File(context.filesDir, "Items.json")
            file.writeText(jsonObject.toString())

        } catch (e: Exception) {
            Log.e(TAG, "Error saving to JSON", e)
        }
    }

    companion object {
        private const val TAG = "JsonUtils"
    }

    init    {
        processJSON(context)
    }
}