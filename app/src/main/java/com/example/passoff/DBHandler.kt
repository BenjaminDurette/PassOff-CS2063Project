package com.example.passoff

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHandler (context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE_COL + " TEXT,"
                + USERNAME_COL + " TEXT,"
                + PASSWORD_COL + " TEXT,"
                + DOMAIN_COL + " TEXT)")
        db.execSQL(query)
    }

    // this method is used to add a new password to our database.
    fun addNewPassword(title: String?, username: String?, password: String?, domain: String?) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(TITLE_COL, title)
        values.put(USERNAME_COL, username)
        values.put(PASSWORD_COL, password)
        values.put(DOMAIN_COL, domain)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllPasswords(): ArrayList<PassItem> {
        val passwordList = ArrayList<PassItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_COL))
                val domain = cursor.getString(cursor.getColumnIndexOrThrow(DOMAIN_COL))

                val passwordEntry = PassItem(title, username, password, domain)
                passwordList.add(passwordEntry)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return passwordList
    }

data class PassItem(val title: String, val username: String, val password: String, val domain: String)

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "passwords"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "mypasswords"
        private const val ID_COL = "id"
        private const val TITLE_COL = "entryname"
        private const val USERNAME_COL = "username"
        private const val PASSWORD_COL = "password"
        private const val DOMAIN_COL = "domain"
    }
}