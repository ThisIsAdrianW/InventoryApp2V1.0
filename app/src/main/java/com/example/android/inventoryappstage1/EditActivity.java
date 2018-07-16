package com.example.android.inventoryappstage1;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.ProductContract;
import com.example.android.inventoryappstage1.data.ProductContract.ProductEntry;
import com.example.android.inventoryappstage1.data.ProductDbHelper;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText nameOfProduct;
    private EditText priceOfProduct;
    private EditText quantityOfProduct;
    private EditText supplierOfProduct;
    private EditText supplierPhoneProduct;
    private Uri mCurrentProductUri;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //reciving data from other activity if item is clicked
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        // If the intent DOES NOT contain a product uri then show this activity as edit
        if (mCurrentProductUri == null) {
            //Using label for adding product
            setTitle(getString(R.string.addAct));
            invalidateOptionsMenu();
        } else {
            // Setting title for "Edit Activity"
            setTitle(getString(R.string.editAct));

            // Initialize a loader to read  data from the database
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        nameOfProduct = findViewById(R.id.nameOfProduct);
        priceOfProduct = findViewById(R.id.priceOfProduct);
        quantityOfProduct = findViewById(R.id.quantityOfProduct);
        supplierOfProduct = findViewById(R.id.supplierOfProduct);
        supplierPhoneProduct = findViewById(R.id.supnumberOfProduct);
        startEditTextListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit.xml file.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    private void insertProduct() {
        // Read from input fields
        String nameString = getTextFromUI(nameOfProduct);
        String priceString = getTextFromUI(priceOfProduct);
        String quantityString = getTextFromUI(quantityOfProduct);
        String supplierString = getTextFromUI(supplierOfProduct);
        String supplierPhoneString = getTextFromUI(supplierPhoneProduct);
        //It dosent matter if this is new entry or existing, all fields should be filled.
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierPhoneString)
                || TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, getString(R.string.edit_fail_text),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            priceString = "0";
        }
        if (TextUtils.isEmpty(quantityString)) {
            quantityString = "0";
        }
        double productPrice = Double.parseDouble(priceString);
        int productQuantity = Integer.parseInt(quantityString);
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhoneString);
        //Checking if this will be new product or user want to edit existing
        if (mCurrentProductUri == null) {
            // This is a NEW product
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            Log.v("Edit Actibity", "New Uri is" + newUri);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Otherwise this is an EXISTING product
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            Log.v("Edit Actibity", "New Uri is" + mCurrentProductUri);
            Log.v("Edit Actibity", "Row updated" + rowsAffected);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Method to hide "delete" option from menu if this is new item on list
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //  hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertProduct();
                // After finish this will go back to main screen, CatalogActivity.
                return true;
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
            // Find the columns of attributes
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
            nameOfProduct.setText(name);
            priceOfProduct.setText(Double.toString(price));
            quantityOfProduct.setText(Integer.toString(quantity));
            supplierOfProduct.setText(supplierName);
            supplierPhoneProduct.setText(supplierPhone);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameOfProduct.setText("");
        priceOfProduct.setText("");
        quantityOfProduct.setText("");
        supplierOfProduct.setText("");
        supplierPhoneProduct.setText("");
    }

    //Helper method that extracts trimmed text fom EditText and return String
    public String getTextFromUI(EditText getFromThis) {
        return getFromThis.getText().toString().trim();
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Method that "slims" onCreate. Listeners for all EditText fields.
    //If someone will touch EditText they will set @param mProductHasChanged to "true"
    private void startEditTextListeners() {
        nameOfProduct.setOnTouchListener(mTouchListener);
        priceOfProduct.setOnTouchListener(mTouchListener);
        quantityOfProduct.setOnTouchListener(mTouchListener);
        supplierOfProduct.setOnTouchListener(mTouchListener);
        supplierPhoneProduct.setOnTouchListener(mTouchListener);
    }
}

