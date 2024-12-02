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

    fun deletePassword(id: Int): Boolean {
        val db = this.writableDatabase
        if (id <= 0) {
            db.close()
            return false
        }
        db.delete(TABLE_NAME, "$ID_COL=?", arrayOf(id.toString()))
        db.close()
        return true
    }

    fun deleteAllPasswords(): Boolean {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
        return true
    }

    fun updatePassword(id: Int, entryName: String?, username: String?, password: String?, url: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(ENTRYNAME_COL, entryName)
        values.put(USERNAME_COL, username)
        values.put(PASSWORD_COL, password)
        values.put(DESCRIPTION_COL, url)

        db.update(TABLE_NAME, values, "$ID_COL=?", arrayOf(id.toString()))
        db.close()
        return true
    }


    fun getPasswords(): ArrayList<PassItem> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val passList = ArrayList<PassItem>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(ENTRYNAME_COL))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_COL))
                val domain = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL))

                val passItem = PassItem(id, title, username, password, domain) // Ensure PassItem constructor matches the columns
                passList.add(passItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return passList
    }

    fun searchPasswords(query: String): ArrayList<PassItem> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $ENTRYNAME_COL LIKE ?", arrayOf("%$query%"))
        val passList = ArrayList<PassItem>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(ENTRYNAME_COL))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_COL))
                val domain = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL))

                val passItem = PassItem(
                    id,
                    title,
                    username,
                    password,
                    domain
                ) // Ensure PassItem constructor matches the columns
                passList.add(passItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return passList
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