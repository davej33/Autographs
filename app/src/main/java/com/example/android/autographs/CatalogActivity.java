package com.example.android.autographs;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.autographs.data.InventoryContract.*;
import com.example.android.autographs.data.InventoryContract;
import com.example.android.autographs.data.InventoryDbHelper;

public class CatalogActivity extends AppCompatActivity {

    InventoryCursorAdapter mCursorAdapter;

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

        displayDatabaseInfo();

       /* ListView listview = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listview.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listview.setAdapter(mCursorAdapter);*/

    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        String[] projection = {
                Inventory._ID,
                Inventory.ITEM_NAME,
                Inventory.ITEM_SALE_PRICE,
                Inventory.ITEM_QUANTITY};

        Cursor cursor = getContentResolver().query(
                InventoryContract.INVENTORY_CONTENT_URI,
                projection,
                null, null, null);

        TextView displayView = (TextView) findViewById(R.id.inv_text_view);

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            int count = cursor.getCount();
            displayView.setText("Number of items: " + count + "\n\n");
            displayView.append(

                    Inventory._ID + " - "
                            +
                            Inventory.ITEM_NAME + " - "
                            +
                            Inventory.ITEM_SALE_PRICE + " - "
                            +
                            Inventory.ITEM_QUANTITY + "\n");

            // iterate through each item and display values
            while (cursor.moveToNext()) {
                // get column index
                int idCol = cursor.getColumnIndex(
                        Inventory._ID);
                int nameCol = cursor.getColumnIndex(
                        Inventory.ITEM_NAME);
                int priceCol = cursor.getColumnIndex(
                        Inventory.ITEM_SALE_PRICE);
                int quantityCol = cursor.getColumnIndex(
                        Inventory.ITEM_QUANTITY);

                // get values
                int id = cursor.getInt(idCol);
                String name = cursor.getString(nameCol);
                long price = cursor.getLong(priceCol);
                int quantity = cursor.getInt(quantityCol);

                // add to display view
                displayView.append(
                        id + " - " + name + " - $" + price + " - " + quantity + "\n");
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
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
            displayDatabaseInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
