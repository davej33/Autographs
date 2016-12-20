package com.example.android.autographs;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.example.android.autographs.data.InventoryContract;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        EditText nameInsert = (EditText) findViewById(R.id.insert_item_name);
        String nameString = nameInsert.getText().toString();

        EditText priceInsert = (EditText) findViewById(R.id.insert_price);
        long price = Long.getLong(priceInsert.getText().toString());

        ContentValues insertValues = new ContentValues();
        insertValues.put(InventoryContract.Inventory.ITEM_NAME, nameString);

    }
}
