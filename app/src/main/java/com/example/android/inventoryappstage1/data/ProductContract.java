package com.example.android.inventoryappstage1.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {
    //Private constructor of class
    private ProductContract() {
    }

    //Variables for other classes to use.
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage1";
    public static final String PATH_PRODUCTS = "products";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static abstract class ProductEntry implements BaseColumns {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public final static String _ID = BaseColumns._ID;
        public final static String TABLE_NAME = "products";
        public final static String COLUMN_PRODUCT_NAME = "Name";
        public final static String COLUMN_PRODUCT_PRICE = "Price";
        public final static String COLUMN_PRODUCT_QUANTITY = "Quantity";
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "Supplier";
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER = "Phone";
    }
}
