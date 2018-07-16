package com.example.android.inventoryappstage1;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventoryappstage1.data.ProductDbHelper;
import com.example.android.inventoryappstage1.data.ProductContract.ProductEntry;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ProductDbHelper mDbHelper;
    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        mDbHelper = new ProductDbHelper(this);
        ListView productListView = (ListView) findViewById(R.id.listView);
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
        //Starting loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        //Using adapter on list view
        productListView.setAdapter(mCursorAdapter);
        //On item click listener interface
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                selectActivityDialog(id);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.addNewProduct:
                Intent intent = new Intent(CatalogActivity.this, EditActivity.class);
                startActivity(intent);
                return true;
            case R.id.delData:
                deleteEverything();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Shorter projection for tests
        String projection[] = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(
                this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void deleteEverything() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    private void selectActivityDialog(final long number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alertTitle);
        builder.setMessage(R.string.alertText);
        builder.setPositiveButton(R.string.alertEdit,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(CatalogActivity.this, EditActivity.class);
                        //Uri with ID of product. ID from click listener.
                        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, number);
                        //adding this Uri to intent
                        intent.setData(currentProductUri);
                        //and now go
                        startActivity(intent);
                    }
                });
        builder.setNeutralButton(R.string.alertExit,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton(R.string.alertDetail,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(CatalogActivity.this, ProductDetails.class);
                        //Uri with ID of product. ID from click listener.
                        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, number);
                        //adding this Uri to intent
                        intent.setData(currentProductUri);
                        //and now go
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }
}
