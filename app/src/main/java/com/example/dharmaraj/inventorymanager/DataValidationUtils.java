package com.example.dharmaraj.inventorymanager;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.dharmaraj.inventorymanager.data.InventoryContract;

public final class DataValidationUtils {
    public static boolean validateData(ContentValues contentValues, Context context) {
        //get the string value(i.e) name of product from the ContentValue
        String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        //Check to see if it is empty if it is Empty then throw an exception
        if (TextUtils.isEmpty(name)) {
            showToast(context.getString(R.string.warning_add_name), context);
            return false;
        }

        //check the price of the product
        float price;
        price = contentValues.getAsFloat(InventoryContract.InventoryEntry.COLUMN_PRICE);
        //Check to see if price is valid then throw an exception
        if (price <= 0) {
            showToast(context.getString(R.string.warning_add_price), context);
            return false;
        }

        //check the quantity of the product
        int quantity = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT);
        //Check to see if quantity is valid then throw an exception
        if (quantity < 0) {
            showToast(context.getString(R.string.warning_add_quantity), context);
            return false;
        }
        //Check whether one of the contact info is provided
        String mobile_no = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_CONTACT_INFO);
        String email = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_EMAIL_ID);
        if (TextUtils.isEmpty(mobile_no) && TextUtils.isEmpty(email)) {
            showToast(context.getString(R.string.warning_add_mobile_or_email), context);
            return false;
        }
        return true;
    }

    private static void showToast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
