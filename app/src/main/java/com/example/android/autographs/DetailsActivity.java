package com.example.android.autographs;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

public class DetailsActivity extends AppCompatActivity {

    EditText mNameInsert;
    EditText mPriceInsert;
    EditText mQuantInsert;
    EditText mSupInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // get view objects
        mNameInsert = (EditText) findViewById(R.id.insert_item_name);
        mPriceInsert = (EditText) findViewById(R.id.insert_price);
        mQuantInsert = (EditText) findViewById(R.id.insert_quantity);
        mSupInsert = (EditText) findViewById(R.id.insert_supplier);

    }

    public void insertItem() {

        // get values from objects
        String name = mNameInsert.getText().toString().trim();
        String price = mPriceInsert.getText().toString().trim();
        String quantity = mQuantInsert.getText().toString().trim();
        String supplier = mSupInsert.getText().toString().trim();

        // create content value object and populate
        ContentValues insertValues = new ContentValues();
        insertValues.put(InventoryContract.Inventory.ITEM_NAME, name);
        insertValues.put(InventoryContract.Inventory.ITEM_SUPPLIER, supplier);

        long priceSet = 0;
        if(!price.isEmpty()){
            priceSet = Long.parseLong(price);
        }
        insertValues.put(InventoryContract.Inventory.ITEM_SALE_PRICE, priceSet);

        int quantSet = 0;
        if(!quantity.isEmpty()){
            quantSet = Integer.parseInt(quantity);
        }
        insertValues.put(InventoryContract.Inventory.ITEM_QUANTITY, quantSet);


        // insert into DB
        Uri insertItem = getContentResolver().insert(InventoryContract.INVENTORY_CONTENT_URI, insertValues);
        if (insertItem != null) {
            Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error Adding Item", Toast.LENGTH_SHORT).show();
        }

    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item_save:
                insertItem();
                finish();
                break;
            case R.id.add_item_cancel:
                // to do
        }
        return true;
    }
}
