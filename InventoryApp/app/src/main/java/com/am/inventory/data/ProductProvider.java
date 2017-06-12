package com.am.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import com.am.inventory.data.ProductContract.ProductEntry;

/**
 * Created by amatanat on 09.06.17.
 */

public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = ProductProvider.class.getName();

    // URI matcher code for the content uri for the table "products"
    private static final int PRODUCTS = 1;

    //URI matcher code for the content uri for the specific row
    private static final int PRODUCTS_ID = 2;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
    }

    private ProductDbHelper mProductDbHelper;

    @Override
    public boolean onCreate() {
        mProductDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = mProductDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = mUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                // query the whole table
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection,selectionArgs,null,null,sortOrder);
                break;
            case PRODUCTS_ID:
                // query specific id indicated in the uri
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri,  ContentValues values) {
        int match = mUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri,values);
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){

        // check product name if null or not
        String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (productName == null){
            throw new IllegalArgumentException("Product requires a name");
        }

        //check product price if null or not
        Integer productPrice = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == null){
            throw new IllegalArgumentException("Product requires a price");
        }

        Integer productQuantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuantity == null){
            throw new IllegalArgumentException("Product quantity should be added");
        }

        SQLiteDatabase db = mProductDbHelper.getWritableDatabase();
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);

        if (id == -1){
            Log.e(LOG_TAG, "Error in inserting a new row");
        } else {
            Log.i(LOG_TAG, "Row is inserted");
        }

        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


}