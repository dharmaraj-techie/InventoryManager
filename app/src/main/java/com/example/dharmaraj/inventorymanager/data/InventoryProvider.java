package com.example.dharmaraj.inventorymanager.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.dharmaraj.inventorymanager.DataValidationUtils;

import java.io.File;


public class InventoryProvider extends ContentProvider {
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    //uri matcher reference with default value as no match
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    //code for multiple and single row
    private static final int SINGLE_INVENTORY = 1;
    private static final int MULTIPLE_INVENTORY = 2;

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH, MULTIPLE_INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH + "/#", SINGLE_INVENTORY);
    }

    InventoryDbHelper mInventoryDbHelper;

    @Override
    public boolean onCreate() {
        //Creating a reference to the SQLitHelper method
        mInventoryDbHelper = new InventoryDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {

        int match = sUriMatcher.match(uri);
        //cursor to hold the returned query
        Cursor cursor;
        switch (match) {
            case MULTIPLE_INVENTORY:
                cursor = queryTheDataBase(strings, s, strings1, s1);
                break;
            case SINGLE_INVENTORY:
                strings1 = new String[]{
                        uri.getLastPathSegment()
                };
                s = InventoryContract.InventoryEntry.COLUMN_ID + "=?";
                cursor = queryTheDataBase(strings, s, strings1, s1);
                break;
            default:
                throw new IllegalArgumentException(LOG_TAG + " unknown uri " + uri);
        }

        //set notification Listener to the uri
        // when the Data at the uri is changed it will be notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor queryTheDataBase(String[] strings, String s, String[] strings1, String s1) {
        //get a readable database
        SQLiteDatabase sqLiteDatabase = mInventoryDbHelper.getReadableDatabase();
        return sqLiteDatabase.query(InventoryContract.InventoryEntry.TABLE_NAME, strings, s, strings1, s1, null, null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case MULTIPLE_INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case SINGLE_INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(LOG_TAG + " unknown uri " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        if (match == MULTIPLE_INVENTORY) {
            return insetStocks(uri, contentValues);
        } else {
            throw new IllegalArgumentException(" Unknown uri " + uri);
        }
    }


    private Uri insetStocks(Uri uri, ContentValues contentValues) {
        //call the method to validate the data before inserting
        if (DataValidationUtils.validateData(contentValues, getContext())) {
            //get a reference to the database
            SQLiteDatabase sqLiteDatabase = mInventoryDbHelper.getWritableDatabase();
            //insert data into the database and the returned value is validated
            long id = sqLiteDatabase.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);
            if (id == -1) {
                Toast.makeText(getContext(), "the new Product was not inserted ", Toast.LENGTH_SHORT).show();
            }
            // this will notify the the Listener in the cursor that the data in this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
            Uri.withAppendedPath(uri, String.valueOf(id));
            return Uri.withAppendedPath(uri, String.valueOf(id));
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MULTIPLE_INVENTORY:
                return deleteFromDatabase(uri, s, strings);

            case SINGLE_INVENTORY:
                strings = new String[]{
                        uri.getLastPathSegment()
                };
                s = InventoryContract.InventoryEntry.COLUMN_ID + "=?";
                return deleteFromDatabase(uri, s, strings);

            default:
                throw new IllegalArgumentException(LOG_TAG + " unknown uri " + uri);
        }

    }

    private int deleteFromDatabase(Uri uri, String s, String[] strings) {
        SQLiteDatabase sqLiteDatabase = mInventoryDbHelper.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.query(
                InventoryContract.InventoryEntry.TABLE_NAME,
                new String[]{InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE_URL},
                s,
                strings,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String imageUrl = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE_URL));
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    File file = new File(imageUrl);
                    file.delete();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        int i = sqLiteDatabase.delete(InventoryContract.InventoryEntry.TABLE_NAME, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);
        return i;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case SINGLE_INVENTORY:
                s = InventoryContract.InventoryEntry.COLUMN_ID + "=?";
                strings = new String[]{
                        uri.getLastPathSegment()
                };

                SQLiteDatabase sqLiteDatabase = mInventoryDbHelper.getWritableDatabase();
                getContext().getContentResolver().notifyChange(uri, null);
                return sqLiteDatabase.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, s, strings);

            default:
                throw new IllegalArgumentException(LOG_TAG + " unknown uri " + uri);
        }
    }

}
