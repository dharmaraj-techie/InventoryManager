package com.example.dharmaraj.inventorymanager.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.dharmaraj.inventorymanager";

    //name of the Table
    public static final String PATH = "stocks";

    //connect the CONTENT_AUTHORITY with the Scheme to produce the base uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {
        /**
         * Content Uri used to define the path for the content resolver
         * content://com.example.dharmaraj.inventorymanager/Stocks
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH);

        //Defines the MIME type for the group of records
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        //Defines the MIME type for a individual item
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;


        public static final String TABLE_NAME = "stocks";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_NO_OF_PRODUCT = "count";
        public static final String COLUMN_CONTACT_INFO = "contact";
        public static final String COLUMN_EMAIL_ID = "email";
        public static final String COLUMN_PRODUCT_IMAGE_URL = "image_url";
    }

}
