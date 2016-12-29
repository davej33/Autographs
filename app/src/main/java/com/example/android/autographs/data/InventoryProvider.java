package com.example.android.autographs.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.autographs.data.InventoryContract.Inventory;
import com.example.android.autographs.data.InventoryContract.InventoryUpdates;

/**
 * Created by dnj on 12/19/16.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    // URI matcher codes
    private static final int INVENTORY_TABLE = 100;
    private static final int INVENTORY_ID = 101;
    private static final int UPDATES_TABLE = 200;
    private static final int UPDATES_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH, INVENTORY_TABLE);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH + "/#", INVENTORY_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.UPDATES_PATH, UPDATES_TABLE);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.UPDATES_PATH + "/#", UPDATES_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                cursor = db.query(Inventory.INV_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(Inventory.INV_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case UPDATES_TABLE:
                cursor = db.query(InventoryUpdates.UPD_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case UPDATES_ID:
                selection = InventoryUpdates.UPD_TABLE_NAME + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(Inventory.INV_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Could not query URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ID:
                return InventoryContract.CONTENT_ITEM_TYPE;
            case INVENTORY_TABLE:
                return InventoryContract.CONTENT_LIST_TYPE;
            default:
                throw new IllegalArgumentException("No type match for: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {


        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                return insertItem(uri, values);
        }
        return null;
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        // empty name check
        String nameValidation = values.getAsString(Inventory.ITEM_NAME);
        if (nameValidation.isEmpty()) {
            Log.e(LOG_TAG, "Empty name");
            throw new IllegalArgumentException("Item requires a name");
        }

        // price check
        long priceCheck = values.getAsLong(Inventory.ITEM_SALE_PRICE);
        if (priceCheck <= 0 || priceCheck > 10000) {
            throw new IllegalArgumentException("Please enter valid price");
        }

        // quantity check
        int quantCheck = values.getAsInteger(Inventory.ITEM_QUANTITY);
        if (quantCheck <= 0 || quantCheck > 5000) {
            throw new IllegalArgumentException("Please enter valid quantity");
        }

        // provider check
        String provCheck = values.getAsString(Inventory.ITEM_SUPPLIER);
        if (provCheck.isEmpty()) {
            throw new IllegalArgumentException("Please enter supplier");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(Inventory.INV_TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row: " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int deleteID;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY_TABLE:
                deleteID = db.delete(Inventory.INV_TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deleteID = db.delete(Inventory.INV_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unable to delete using uri: " + uri);
        }
        if(deleteID != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleteID;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                return updateItem(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not available for: " + uri);
        }

    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // check if no values to update
        if (values.size() == 0) {
            return 0;
        }
        // empty name check
        if (values.containsKey(Inventory.ITEM_NAME)) {
            String nameCheck = values.getAsString(Inventory.ITEM_NAME);
            if (nameCheck.isEmpty()) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // price check
        if (values.containsKey(Inventory.ITEM_SALE_PRICE)) {
            long priceCheck = values.getAsLong(Inventory.ITEM_SALE_PRICE);
            if (priceCheck <= 0 || priceCheck > 10000) {
                throw new IllegalArgumentException("Please enter valid price");
            }
        }

        // quantity check
        if (values.containsKey(Inventory.ITEM_QUANTITY)) {
            int quantCheck = values.getAsInteger(Inventory.ITEM_QUANTITY);
            if (quantCheck <= 0 || quantCheck > 10000) {
                throw new IllegalArgumentException("Please enter valid quantity");
            }
        }

        // provider check
        if (values.containsKey(Inventory.ITEM_SUPPLIER)) {
            String provCheck = values.getAsString(Inventory.ITEM_SUPPLIER);
            if (provCheck.isEmpty()) {
                throw new IllegalArgumentException("Please enter supplier");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updateID = db.update(Inventory.INV_TABLE_NAME, values, selection, selectionArgs);

        if(updateID != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateID;
    }
}
