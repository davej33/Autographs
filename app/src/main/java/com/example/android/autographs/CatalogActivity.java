package com.example.android.autographs;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.autographs.data.InventoryContract;
import com.example.android.autographs.data.InventoryContract.Inventory;
import com.example.android.autographs.data.InventoryDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    InventoryCursorAdapter mCursorAdapter;

    private static final int CURSOR_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        ListView listview = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listview.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listview.setAdapter(mCursorAdapter);

    }


    public void insertItem() {
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues dummyItem = new ContentValues();
        dummyItem.put(
                Inventory.ITEM_NAME, "Test Item");
        dummyItem.put(
                Inventory.ITEM_SALE_PRICE, 12.30);
        dummyItem.put(
                Inventory.ITEM_QUANTITY, 5);
        dummyItem.put(
                Inventory.ITEM_SUPPLIER, "Test Supplier");

        long dummy = db.insert(
                Inventory.INV_TABLE_NAME, null, dummyItem);
        Log.e("CatalogActivity", "long dummy = " + dummy);


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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.dummy_insert) {
            insertItem();
            //displayDatabaseInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                Inventory._ID,
                Inventory.ITEM_NAME,
                Inventory.ITEM_SALE_PRICE,
                Inventory.ITEM_QUANTITY};

        return new CursorLoader(this,
                InventoryContract.INVENTORY_CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
