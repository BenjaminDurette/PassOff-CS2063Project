package com.example.passoff

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHandler (context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ENTRYNAME_COL + " TEXT,"
                + USERNAME_COL + " TEXT,"
                + PASSWORD_COL + " TEXT,"
                + DESCRIPTION_COL + " TEXT)")

        db.execSQL(query)
    }

    // this method is used to add a new password to our database.
    fun addNewPassword(entryName: String?, username: String?, password: String?, description: String?) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(ENTRYNAME_COL, entryName)
        values.put(USERNAME_COL, username)
        values.put(PASSWORD_COL, password)
        values.put(DESCRIPTION_COL, description)

        db.insert(TABLE_NAME, null, values)

        db.close()
    }

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
        private const val ENTRYNAME_COL = "entryname"
        private const val USERNAME_COL = "username"
        private const val PASSWORD_COL = "password"
        private const val DESCRIPTION_COL = "description"
    }
}