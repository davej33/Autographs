package com.example.android.autographs.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

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
        mDbHelper = new InventoryDbHelper(getContext()); // stopped here
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
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(Inventory.INV_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case UPDATES_TABLE:
                cursor = db.query(InventoryUpdates.UPD_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case UPDATES_ID:
                selection =  InventoryUpdates.UPD_TABLE_NAME + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(Inventory.INV_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Could not query URI: " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {


        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
