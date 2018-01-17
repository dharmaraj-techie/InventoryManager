package com.example.dharmaraj.inventorymanager.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dharmaraj.inventorymanager.R;
import com.example.dharmaraj.inventorymanager.adapters.InventoryAdapter;
import com.example.dharmaraj.inventorymanager.data.InventoryContract.InventoryEntry;
import com.example.dharmaraj.inventorymanager.widget.UpdateWidgetIntentService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private InventoryAdapter mInventoryAdapter;
    private boolean isInSearchMode = false;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catlog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handleIntent(getIntent());
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, " ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        emptyStateTextView = findViewById(R.id.empty_state_text_view);
        ListView mListView = (ListView) findViewById(R.id.list_view);
        mListView.setEmptyView(findViewById(R.id.empty_container));

        mInventoryAdapter = new InventoryAdapter(this, null, true);
        mListView.setAdapter(mInventoryAdapter);

        getLoaderManager().initLoader(1, null, this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(CatalogActivity.this,
                                view.findViewById(R.id.image_view_listitem),
                                "product");

                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);

                Uri uri = InventoryEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(l)).build();
                intent.setData(uri);
                startActivity(intent, options.toBundle());
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProductActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        //when back key is pressed in after searching something
        //it should not close the app, it should go back to the previous state
        if (isInSearchMode) {
            getLoaderManager().initLoader(1, null, this);
            isInSearchMode = false;
            emptyStateTextView.setText(getText(R.string.empty_state_text1));
        } else {
            super.onBackPressed();
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            isInSearchMode = true;
            String query = intent.getStringExtra(SearchManager.QUERY);
            emptyStateTextView.setText(getText(R.string.empty_state_text2));
            new SearchAsyncTask().execute(query);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catlog, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//         Handle action bar item clicks here. The action bar will
//         automatically handle clicks on the Home/Up button, so long
//         as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.delete_all:
                showDeleteConformation();
                break;
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry.COLUMN_ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_NO_OF_PRODUCT,
                InventoryEntry.COLUMN_PRODUCT_IMAGE_URL
        };
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryAdapter.swapCursor(null);
    }


    private void showDeleteConformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_conformation);
        builder.setPositiveButton(R.string.delete_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
                UpdateWidgetIntentService.startActionUpdateProductWidget(CatalogActivity.this);
            }
        });
        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public class SearchAsyncTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... strings) {

            String[] projection = {
                    InventoryEntry.COLUMN_ID,
                    InventoryEntry.COLUMN_PRODUCT_NAME,
                    InventoryEntry.COLUMN_PRICE,
                    InventoryEntry.COLUMN_NO_OF_PRODUCT,
                    InventoryEntry.COLUMN_PRODUCT_IMAGE_URL
            };

            return getContentResolver().query(
                    InventoryEntry.CONTENT_URI,
                    projection,
                    InventoryEntry.COLUMN_PRODUCT_NAME + "= ?",
                    strings,
                    null
            );
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mInventoryAdapter.swapCursor(cursor);
        }
    }
}
