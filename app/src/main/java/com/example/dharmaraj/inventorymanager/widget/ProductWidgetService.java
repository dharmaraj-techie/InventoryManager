package com.example.dharmaraj.inventorymanager.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.data.InventoryContract;

import java.text.NumberFormat;


public class ProductWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ProductRemoteViewFactory(this.getApplicationContext());
    }
}

class ProductRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private NumberFormat mFormat;

    public ProductRemoteViewFactory(Context context) {
        mContext = context;
    }


    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        String[] projection = {
                InventoryContract.InventoryEntry.COLUMN_ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT
        };
        if (mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onDestroy() {
        if (mCursor != null)
            mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        mFormat = NumberFormat.getCurrencyInstance();
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(i);

        long id = mCursor.getLong(mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ID));
        String productName = mCursor.getString(mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
        String cost = mCursor.getString(mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
        String quantity = mCursor.getString(mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT));

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        views.setTextViewText(R.id.product_name_widget_tv, productName);
        views.setTextViewText(R.id.cost_widget_tv, mFormat.format(Double.valueOf(cost)));
        views.setTextViewText(R.id.quantity_widget_tv, quantity);

        Uri uri = InventoryContract.InventoryEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        Intent fillInIntent = new Intent();
        fillInIntent.setData(uri);
        views.setOnClickFillInIntent(R.id.widget_list_item_container, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
