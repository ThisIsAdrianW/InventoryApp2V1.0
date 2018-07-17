package com.example.android.inventoryappstage1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.ProductContract.ProductEntry;

import java.util.ArrayList;
import java.util.List;


public class ProductDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    ContentValues values;
    private TextView productName;
    private TextView productPrice;
    private TextView productQuantity;
    private TextView productSupplier;
    private TextView productPhone;
    private Button sellButton;
    private Button addButton;
    private Button callButton;
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        productName = findViewById(R.id.displayName);
        productPrice = findViewById(R.id.displayPrice);
        productQuantity = findViewById(R.id.displayQuantity);
        productSupplier = findViewById(R.id.displaySupplier);
        productPhone = findViewById(R.id.displayPhone);
        sellButton = findViewById(R.id.buttonSell);
        addButton = findViewById(R.id.buttonAdd);
        callButton = findViewById(R.id.callSupplier);
        Log.v("Product Activity", "Intent is: " + mCurrentProductUri);
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        if (mCurrentProductUri != null) {
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellItem(1);
                }
            });
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellItem(2);
                }
            });
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callSupplier();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_display.xml file.
        getMenuInflater().inflate(R.menu.menu_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns  attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            // Update the views on the screen with the values from the database
            productName.setText(name);
            productPrice.setText(Double.toString(price));
            productQuantity.setText(Integer.toString(quantity));
            productSupplier.setText(supplierName);
            productPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        productPrice.setText("");
        productQuantity.setText("");
        productSupplier.setText("");
        productPhone.setText("");
    }

    private void sellItem(int sellOrBuy) {
        List<String> valueList = readValues(productName, productPrice, productQuantity, productSupplier, productPhone);
        double productPrice = Double.parseDouble(valueList.get(1));
        int productQuantity = Integer.parseInt(valueList.get(2));
        if (productQuantity == 0 && sellOrBuy == 2) {
            Log.v("Product Detail", "Fatal error");
            productQuantity = 0;
            valueList.add(2, String.valueOf(productQuantity));
            fillValuesUpdate(valueList.get(0), valueList.get(1), valueList.get(2), valueList.get(3), valueList.get(4), sellOrBuy);
        } else if (productQuantity == 0 && sellOrBuy == 1) {
            Toast.makeText(this, getString(R.string.failedSell),
                    Toast.LENGTH_SHORT).show();
        } else {
            fillValuesUpdate(valueList.get(0), valueList.get(1), valueList.get(2), valueList.get(3), valueList.get(4), sellOrBuy);
        }

    }

    private void fillValuesUpdate(String name, String price, String quantity, String supplier, String phone, int buyOrSell) {
        int productQuantity;
        double productPrice = Double.parseDouble(price);
        productQuantity = Integer.parseInt(quantity);
        if (buyOrSell == 1) {
            productQuantity--;
        } else if (buyOrSell == 2) {
            productQuantity++;
        } else {
            return;
        }
        Log.v("Fill Values", "Now product quantity is: " + productQuantity);
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        getContentResolver().update(mCurrentProductUri, values, null, null);
    }

    private List<String> readValues(TextView name, TextView price, TextView quantity, TextView supplier, TextView phone) {
        List<String> valueList = new ArrayList<String>();
        valueList.add(name.getText().toString().trim());
        valueList.add(price.getText().toString().trim());
        valueList.add(quantity.getText().toString().trim());
        valueList.add(supplier.getText().toString().trim());
        valueList.add(phone.getText().toString().trim());
        if (valueList.get(1).isEmpty()) {
            valueList.add(1, "0");
        }
        if (valueList.get(2).isEmpty()) {
            valueList.add(2, "0");
        }
        Log.v("Product Detail", "Values to put: " + valueList.get(2));
        return valueList;
    }

    /**
     * Prompt the user to confirm that they want to delete item
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Deleting product
    private void deleteProduct() {
        // Only perform the delete if this is an existing product
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    //Method that will start intent to call supplier if phone number is valid
    private void callSupplier() {
        String phoneNumber = productPhone.getText().toString().trim();
        Log.v("Call Supplier", "Phone is" + phoneNumber);
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.equals("0")) {
            Toast.makeText(this, getString(R.string.call_fail),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        }
    }
}
