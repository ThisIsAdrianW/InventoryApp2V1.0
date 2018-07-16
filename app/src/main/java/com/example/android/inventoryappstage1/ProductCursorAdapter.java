package com.example.android.inventoryappstage1;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.ProductContract.ProductEntry;
import com.example.android.inventoryappstage1.data.ProductDbHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    static class ViewHolder {
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewQuantity;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button sellThis = (Button) view.findViewById(R.id.sellButton);
        // Find the columns of product attributes
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        int supplierPhoneIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
        final String productName = cursor.getString(nameColumnIndex);
        final String supplierName = cursor.getString(supplierColumnIndex);
        final String supplierPhone = cursor.getString(supplierPhoneIndex);
        final double productPrice = cursor.getDouble(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int myId = cursor.getInt(idColumnIndex);
        //Now we will format Double, because prices sometimes have comma in them. Or at least they should have
        NumberFormat nf = new DecimalFormat("##.###");
        String displayDouble = String.valueOf(nf.format(productPrice));
        // Update the TextViews
        nameTextView.setText(productName);
        summaryTextView.setText(displayDouble);
        quantityTextView.setText(String.valueOf(quantity));
        sellThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity >= 1) {
                    Log.v("Product Cursor Adapter", "Cliked !!!!");
                    quantityTextView.setText(String.valueOf(quantity));
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, myId);
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
                    values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                    values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
                    values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhone);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                    Log.v("Product Cursor Adapter", "Cliked !!!!" + currentProductUri + " And now int is " + quantity);
                } else {
                    quantityTextView.setText("You have no products. Contact Your supplier");
                }

            }
        });
    }
}
