package com.example.android.autographs;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;
import com.example.android.autographs.data.InventoryContract.Inventory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.android.autographs.data.InventoryProvider.LOG_TAG;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static int mQuantity;
    public static String mName;
    private String mPosition;

    InventoryCursorAdapter mCursorAdapter;

    private static final int CURSOR_LOADER_ID = 0;
    private static final int GET_NAME_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        Log.e(LOG_TAG, "mName # 1: " + mName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });


        ListView listview = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listview.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listview.setAdapter(mCursorAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.INVENTORY_CONTENT_URI, id);
                intent.setData(currentItemUri);

                mPosition = String.valueOf(position + 1);
                Log.e(LOG_TAG, "mPosition String: " + mPosition);

                getLoaderManager().initLoader(GET_NAME_LOADER_ID, null, CatalogActivity.this);
                Log.e(LOG_TAG, "### 1.75: " + mName);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }


    public void insertItem() {

        String testName = "Test Item";
        double testSalePrice = 12.50;
        int testQuant = 5;
        String testSup = "Test Supplier";
        double testPurchasePrice = 10.00;
        String testTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());


        // insert values into Inventory
        ContentValues dummyItem = new ContentValues();
        dummyItem.put(Inventory.ITEM_NAME, testName);
        dummyItem.put(Inventory.ITEM_SALE_PRICE, testSalePrice);
        dummyItem.put(Inventory.ITEM_QUANTITY, testQuant);
        dummyItem.put(Inventory.ITEM_SUPPLIER, testSup);

        Uri dummyUri = getContentResolver().insert(
                InventoryContract.INVENTORY_CONTENT_URI, dummyItem);

        Log.e("Catalog Activity", "Dummy Inventory Insert: " + dummyUri);
        long testItemIdLong = ContentUris.parseId(dummyUri);


        // insert values into Updates
        ContentValues dummyInsertUpdates = new ContentValues();
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, testName);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_PURCH_PRICE, testPurchasePrice);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED, testQuant);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, testTransactionTime);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_SUPPLIER, testSup);

        Uri dummyUriUpdate = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, dummyInsertUpdates);
        Log.e("Catalog Activity", "Dummy Update Insert Return Value: " + dummyUriUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.dummy_insert:
                insertItem();
                return true;
            case R.id.delete_all:
                deleteAll();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        int deletedRows = getContentResolver().delete(InventoryContract.INVENTORY_CONTENT_URI, null, null);
        if (deletedRows == 0) {
            Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Successful Deletion", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader loader = null;

        if (id == CURSOR_LOADER_ID) {
            String[] projection = {
                    Inventory._ID,
                    Inventory.ITEM_NAME,
                    Inventory.ITEM_SALE_PRICE,
                    Inventory.ITEM_QUANTITY};

            loader = new CursorLoader(this,
                    InventoryContract.INVENTORY_CONTENT_URI,
                    projection, null, null, null);
        }

        if (id == GET_NAME_LOADER_ID) {
            String selection = Inventory._ID + "=?";
            String[] selectionArgs = {mPosition};

            String[] projection = {
                    Inventory._ID,
                    Inventory.ITEM_NAME,
                    Inventory.ITEM_SALE_PRICE};

            loader = new CursorLoader(this, InventoryContract.INVENTORY_CONTENT_URI,
                    projection, selection, selectionArgs, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case GET_NAME_LOADER_ID:
                if (data.moveToFirst()) {
                    int priceCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SALE_PRICE);
                    int idCol = data.getColumnIndex(Inventory._ID);
                    int nameCol = data.getColumnIndex(Inventory.ITEM_NAME);

                    double p = data.getDouble(priceCol);
                    int id = data.getInt(idCol);
                    mName = data.getString(nameCol);
                    Log.e(LOG_TAG, "id: " + id + " price: " + p + " mName # 1.5: " + mName);
                }
                break;
            case CURSOR_LOADER_ID:
                mCursorAdapter.swapCursor(data);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
