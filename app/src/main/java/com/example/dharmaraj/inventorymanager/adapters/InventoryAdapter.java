package com.example.dharmaraj.inventorymanager.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.data.InventoryContract;
import com.example.dharmaraj.inventorymanager.widget.UpdateWidgetIntentService;

import java.text.NumberFormat;


public class InventoryAdapter extends CursorAdapter {
    private NumberFormat mFormat;

    public InventoryAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        if (mFormat == null) mFormat = NumberFormat.getCurrencyInstance();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name_text_view);
        TextView price = (TextView) view.findViewById(R.id.price_text_view);
        TextView number = (TextView) view.findViewById(R.id.number_text_view);
        Button btn = (Button) view.findViewById(R.id.button);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view_listitem);

        //get the id of the current view with help of cursor
        //and build a uri to match the item item in database relating to the corresponding id
        int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ID));
        Uri uri = InventoryContract.InventoryEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();

//      tag the uri with the button view using seTag method so that you can acsess it(i.e the uri) inside the on click listener
        btn.setTag(uri);

        //sets a listener for the sell button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the uri from the tag
                Uri uri1 = (Uri) view.getTag();

                //get the current number of products by querying the database using uri received by getTag method
                //returned cursor value is stored in the cursor1 reference obj
                Cursor cursor1 = view.getContext().getContentResolver().
                        query(uri1, new String[]{InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT}, null, null, null);

                cursor1.moveToFirst();

                //get the number of products available from the cursor
                int n = cursor1.getInt(cursor1.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT));

                //check if its 0
                if (n > 0) {
                    n--;
                } else {
                    Toast.makeText(view.getContext(), R.string.no_product_to_sell_error_msg, Toast.LENGTH_SHORT).show();
                }

                //put the new value(new number of product) in the database
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT, String.valueOf(n));
                view.getContext().getContentResolver().update(uri1, values, null, null);
                UpdateWidgetIntentService.startActionUpdateProductWidget(view.getContext());
                Toast.makeText(view.getContext(), view.getContext().getString(R.string.update_success_msg), Toast.LENGTH_SHORT).show();
            }
        });

        //get the values
        String nameString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
        String priceString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE)).trim();
        int no = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT));
        String imageUrl = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE_URL));

        name.setText(nameString);
        price.setText(mFormat.format(Double.valueOf(priceString)));
        number.setText(String.valueOf(no));
        Glide.with(view.getContext()).load(imageUrl).thumbnail(0.1f).into(imageView);
    }

}
