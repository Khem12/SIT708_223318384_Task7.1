package com.example.lostandfoundapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LostFound.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For version 1 to 3, we'll recreate the table to ensure consistency
        if (oldVersion < 3) {
            try {
                // Create temporary table with new schema
                db.execSQL("CREATE TABLE " + TABLE_ITEMS + "_temp (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TYPE + " TEXT, " +
                        COLUMN_NAME + " TEXT, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_DATE + " TEXT, " +
                        COLUMN_LOCATION + " TEXT, " +
                        COLUMN_LATITUDE + " REAL, " +
                        COLUMN_LONGITUDE + " REAL)");

                // Copy data from old table to temporary table
                // For existing records, set default values for new columns
                db.execSQL("INSERT INTO " + TABLE_ITEMS + "_temp (" +
                        COLUMN_ID + ", " +
                        COLUMN_TYPE + ", " +
                        COLUMN_NAME + ", " +
                        COLUMN_PHONE + ", " +
                        COLUMN_DESCRIPTION + ", " +
                        COLUMN_DATE + ", " +
                        COLUMN_LOCATION + ") " +
                        "SELECT " +
                        COLUMN_ID + ", " +
                        COLUMN_TYPE + ", " +
                        COLUMN_NAME + ", " +
                        COLUMN_PHONE + ", " +
                        COLUMN_DESCRIPTION + ", " +
                        COLUMN_DATE + ", " +
                        COLUMN_LOCATION + " " +
                        "FROM " + TABLE_ITEMS);

                // Drop old table
                db.execSQL("DROP TABLE " + TABLE_ITEMS);

                // Rename temporary table
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + "_temp RENAME TO " + TABLE_ITEMS);

            } catch (SQLiteException e) {
                Log.e("DatabaseHelper", "Migration failed", e);
                // If migration fails, recreate the table from scratch
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
                onCreate(db);
            }
        }

        // For future version upgrades, add additional conditions here
        if (oldVersion < 4) {
            // Any additional migrations for version 4 would go here
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The safest approach for downgrade is to recreate the database
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public boolean addItem(String type, String name, String phone, String description,
                           String date, String location, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TYPE, type);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_LOCATION, location);
        contentValues.put(COLUMN_LATITUDE, latitude);
        contentValues.put(COLUMN_LONGITUDE, longitude);

        long result = db.insert(TABLE_ITEMS, null, contentValues);
        return result != -1;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
    }

    public boolean deleteItem(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS, COLUMN_ID + "=?", new String[]{id}) > 0;
    }

    // Optional: Get a single item by ID
    public Cursor getItemById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ITEMS,
                null,
                COLUMN_ID + "=?",
                new String[]{id},
                null, null, null
        );
    }
}