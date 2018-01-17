package com.example.dharmaraj.inventorymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dharmaraj.inventorymanager.data.InventoryContract.InventoryEntry;

public final class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;


    private final String CREATE_DATABASE_INVENTORY =
            "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ( "
                    + InventoryEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + InventoryEntry.COLUMN_BRAND + " TEXT, "
                    + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                    + InventoryEntry.COLUMN_PRICE + " REAL NOT NULL, "
                    + InventoryEntry.COLUMN_NO_OF_PRODUCT + " INTEGER NOT NULL, "
                    + InventoryEntry.COLUMN_CONTACT_INFO + " INTEGER, "
                    + InventoryEntry.COLUMN_EMAIL_ID + " TEXT, "
                    + InventoryEntry.COLUMN_PRODUCT_IMAGE_URL + " TEXT"
                    + " );";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DATABASE_INVENTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
