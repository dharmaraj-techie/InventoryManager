package com.example.dharmaraj.inventorymanager.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dharmaraj.inventorymanager.DataValidationUtils;
import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.data.InventoryContract;
import com.example.dharmaraj.inventorymanager.data.InventoryContract.InventoryEntry;
import com.example.dharmaraj.inventorymanager.widget.UpdateWidgetIntentService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String IMAGE_PATH_KEY = "image_path_key";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText nameEditText;
    private EditText brandEditText;
    private EditText amountEditText;
    private EditText numberEditText;
    private EditText mobileEditText;
    private EditText emailEditText;
    private ImageView mImageView;
    private Uri mUri;
    private Button addProductButton;
    private boolean stocksChanged = false;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        //get the mUri from the intent passed to this activity by the catalog activity
        mUri = intent.getData();

//        this method will notify the os to redraw the menu since it has changed in run time
//        it will call the onPrepareOptionMenu
        invalidateOptionsMenu();
//      Reference to the all editTexts fields
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        brandEditText = (EditText) findViewById(R.id.brand_edit_text);
        amountEditText = (EditText) findViewById(R.id.price_edit_text);
        numberEditText = (EditText) findViewById(R.id.numberofproduct_edit_text);
        mobileEditText = (EditText) findViewById(R.id.mobile_edit_text);
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        Button plusButton = (Button) findViewById(R.id.plus);
        Button minusButton = (Button) findViewById(R.id.minus);
        mImageView = findViewById(R.id.image_view);
        addProductButton = findViewById(R.id.add_product_btn);

        if (savedInstanceState != null) {
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mCurrentPhotoPath = savedInstanceState.getString(IMAGE_PATH_KEY);
            Glide.with(this).load(mCurrentPhotoPath).into(mImageView);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_image_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
//      Setting an on touch listener for all the EditText fields
//        so that we can notify the user that a change has been made when he preses the back button
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stocksChanged = true;
                return false;
            }
        };
        nameEditText.setOnTouchListener(onTouchListener);
        brandEditText.setOnTouchListener(onTouchListener);
        amountEditText.setOnTouchListener(onTouchListener);
        numberEditText.setOnTouchListener(onTouchListener);
        mobileEditText.setOnTouchListener(onTouchListener);
        emailEditText.setOnTouchListener(onTouchListener);
        plusButton.setOnTouchListener(onTouchListener);
        minusButton.setOnTouchListener(onTouchListener);

        //click listener for the plus button which increases the product count
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = numberEditText.getText().toString();
                int i = Integer.parseInt(quantity);
                if (i >= 0) {
                    i++;
                    numberEditText.setText(String.valueOf(i));
                }
            }
        });

        //click listener for the minus button which decreases the product count
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = numberEditText.getText().toString();
                int i = Integer.parseInt(quantity);
                if (i > 0) {
                    i--;
                    numberEditText.setText(String.valueOf(i));
                }
            }
        });

        //submit or update the product Product details
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call the method getContentValueFromUI()
                ContentValues values = getContentValueFromUI();
                boolean isValid = DataValidationUtils.validateData(values, view.getContext());
                if (isValid) {
                    if (mUri != null) {
                        getContentResolver().update(mUri, values, null, null);
                        UpdateWidgetIntentService.startActionUpdateProductWidget(view.getContext());
                    } else {
                        //insert the data into the database
                        //the inserted values's mUri is returned
                        Uri uri1 = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                        if (uri1 != null) {
                            Toast.makeText(EditProductActivity.this, R.string.inserted_txt, Toast.LENGTH_SHORT).show();
                            UpdateWidgetIntentService.startActionUpdateProductWidget(view.getContext());
                        } else {
                            Toast.makeText(EditProductActivity.this, R.string.no_insertion_txt, Toast.LENGTH_SHORT).show();
                        }
                    }
                    finish();
                }
            }
        });

        if (mUri != null) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    //launch an intent an take a photo
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(
                        EditProductActivity.this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(mCurrentPhotoPath).into(mImageView);
    }

    /**
     * creates a file to store the taken image from the camera
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IMAGE_PATH_KEY, mCurrentPhotoPath);
    }

    @Override
    public void onBackPressed() {
        if (!stocksChanged) {
            super.onBackPressed();
            return;
        }
        showDialogOnBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //check for any changes before going back home
                if (stocksChanged) {
                    //then show the warning dialog box
                    showDialogOnBackPressed();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

            case R.id.delete:
                showDeleteConformation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * gets all the data form the entered editText fields
     *
     * @return ContentValues
     */
    private ContentValues getContentValueFromUI() {
        String name = nameEditText.getText().toString().trim();
        String brand = brandEditText.getText().toString().trim();
        String price = amountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(price)) {
            price = "0.0";
        }
        String number = numberEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_BRAND, brand);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_NO_OF_PRODUCT, number);
        values.put(InventoryEntry.COLUMN_CONTACT_INFO, mobile);
        values.put(InventoryEntry.COLUMN_EMAIL_ID, email);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE_URL, mCurrentPhotoPath);
        return values;
    }

    /**
     * shows a alert dialog when the user deletes the item
     */
    private void showDeleteConformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_during_delete);
        builder.setPositiveButton(R.string.delete_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(mUri, null, null);
                setResult(ProductDetailActivity.DONT_COMEBACK_TO_THIS_ACTIVITY_CODE);
                UpdateWidgetIntentService.startActionUpdateProductWidget(EditProductActivity.this);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * shows a alert dialog when the user presses back without saving the item
     */
    private void showDialogOnBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_msg_when_back_btn_pressed);
        builder.setPositiveButton(R.string.keep_editing_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.discard_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            String nameString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
            String brandString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_BRAND));
            String priceString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
            String numberString = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NO_OF_PRODUCT));
            String mobileString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_CONTACT_INFO));
            String emailString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_EMAIL_ID));
            String imagePath = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE_URL));

            //Set the corresponding value to the editText fields
            nameEditText.setText(nameString);
            brandEditText.setText(brandString);
            amountEditText.setText(priceString);
            numberEditText.setText(numberString);
            mobileEditText.setText(mobileString);
            emailEditText.setText(emailString);
            addProductButton.setText(R.string.submit_changes_btn);

            mCurrentPhotoPath = imagePath;
            Glide.with(this).load(imagePath).into(mImageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
