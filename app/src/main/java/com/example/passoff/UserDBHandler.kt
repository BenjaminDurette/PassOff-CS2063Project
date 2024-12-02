import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LoginDatabase"
        private const val DATABASE_VERSION = 1
        private const val TABLE_LOGIN = "LoginInfo"

        private const val KEY_ID = "id"
        private const val KEY_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_LOGIN (" +
                "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_PASSWORD TEXT)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOGIN")
        onCreate(db)
    }

    // Insert a record
    fun addLogin(password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PASSWORD, password)
        }
        val result = db.insert(TABLE_LOGIN, null, values)
        db.close()
        return result
    }

    // Get all records
    fun getAllLogins(): List<Map<String, String>> {
        val loginList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_LOGIN"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val login = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)).toString(),
                    "password" to cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD))
                )
                loginList.add(login)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return loginList
    }

    // Update a record
    fun updateLogin(id: Int, password: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PASSWORD, password)
        }
        val result = db.update(TABLE_LOGIN, values, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun deleteAllLogins() {
        val db = this.writableDatabase
        db.delete(TABLE_LOGIN, null, null)
        db.close()
    }
}