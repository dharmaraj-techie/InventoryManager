package com.example.dharmaraj.inventorymanager.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.data.InventoryContract;
import com.example.dharmaraj.inventorymanager.widget.UpdateWidgetIntentService;

import java.text.NumberFormat;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int DONT_COMEBACK_TO_THIS_ACTIVITY_CODE = 2;

    private Uri mUri;
    private String mProductNameString;
    private String mProductQuantityString;
    private String mProductMobileString = null;
    private String mProductEmailString = null;
    private NumberFormat mFormat;

    private ImageView mProductImageView;
    private TextView mProductNameTextView;
    private TextView mProductBrandTextView;
    private TextView mCostTextView;
    private TextView mQuantityTextView;
    private Button mOrderMoreButton;
    private Button mSellButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_activty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFormat = NumberFormat.getCurrencyInstance();

        mProductImageView = (ImageView) findViewById(R.id.product_imageView);
        mProductNameTextView = (TextView) findViewById(R.id.product_name_tv);
        mProductBrandTextView = (TextView) findViewById(R.id.brand_name_tv);
        mCostTextView = (TextView) findViewById(R.id.cost_of_each_product);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_available_tv);
        mOrderMoreButton = (Button) findViewById(R.id.order_more_btn);
        mSellButton = (Button) findViewById(R.id.sell_btn);

        handleIntent(getIntent());

        //if the intent is from the detail activity then it will have a uri
        //using it populate the fields of the edit product activity
        if (mUri != null) {
            getLoaderManager().initLoader(0, null, this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//         Handle action bar item clicks here. The action bar will
//         automatically handle clicks on the Home/Up button, so long
//         as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.edit:
                Intent intent1 = new Intent(ProductDetailActivity.this, EditProductActivity.class);
                intent1.setData(mUri);
                startActivityForResult(intent1, DONT_COMEBACK_TO_THIS_ACTIVITY_CODE);
                break;
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * shows a dialog to pic either a mobile number or mail id to contact.
     */
    private void showContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);

        builder.setTitle(R.string.contact_using_text_dialog_box)
                .setItems(R.array.contact_info, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (!mProductMobileString.isEmpty() && mProductMobileString != null) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + mProductMobileString));
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ProductDetailActivity.this,
                                                R.string.error_msg_no_app_to_handle_call,
                                                Toast.LENGTH_SHORT).
                                                show();
                                    }
                                    break;
                                } else {
                                    Toast.makeText(ProductDetailActivity.this,
                                            R.string.no_number_found_tost_text,
                                            Toast.LENGTH_SHORT).
                                            show();
                                }
                                break;
                            case 1:
                                if (!mProductEmailString.isEmpty() && mProductEmailString != null) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.setType("text/plain");
                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mProductEmailString});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Order " + mProductNameString);
                                    intent.putExtra(Intent.EXTRA_TEXT, "Currently the available quantity of the  product "
                                            + mProductNameString + " is "
                                            + mProductQuantityString + ". So please send some more");
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ProductDetailActivity.this,
                                                R.string.error_msg_no_email_app,
                                                Toast.LENGTH_SHORT).
                                                show();
                                    }
                                    break;
                                } else {
                                    Toast.makeText(ProductDetailActivity.this,
                                            R.string.error_msg_no_emil_id_found,
                                            Toast.LENGTH_SHORT).
                                            show();
                                    break;
                                }
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * method to handle the intent.
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        mUri = intent.getData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DONT_COMEBACK_TO_THIS_ACTIVITY_CODE) {
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            mProductNameString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
            String brandString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_BRAND));
            String priceString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
            mProductQuantityString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT));
            mProductMobileString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_CONTACT_INFO));
            mProductEmailString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_EMAIL_ID));
            String imagePath = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE_URL));

            Glide.with(this).load(imagePath).into(mProductImageView);
            mProductNameTextView.setText(mProductNameString);
            mProductBrandTextView.setText(brandString);
            mCostTextView.setText(mFormat.format(Double.valueOf(priceString)));
            mQuantityTextView.setText(mProductQuantityString);
        }

        mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialog();
            }
        });
        //sets a listener for the sell button
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int n = Integer.valueOf(mProductQuantityString);
                //check if its 0
                if (n > 0) {
                    n--;
                    //put the new value(new number of product) in the database
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT, String.valueOf(n));
                    view.getContext().getContentResolver().update(mUri, values, null, null);
                    UpdateWidgetIntentService.startActionUpdateProductWidget(view.getContext());
                    getLoaderManager().restartLoader(0, null, ProductDetailActivity.this);

                } else {
                    Toast.makeText(view.getContext(), R.string.no_product_to_sell_error_msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
