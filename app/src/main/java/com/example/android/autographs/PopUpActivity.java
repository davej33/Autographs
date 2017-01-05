package com.example.android.autographs;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.android.autographs.CatalogActivity.mName;
import static com.example.android.autographs.data.InventoryProvider.LOG_TAG;

/**
 * Created by dnj on 1/3/17.
 */

public class PopUpActivity extends AppCompatActivity {

    private Uri mCurrentUri;
    private String mTransactionTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        Log.e(LOG_TAG, "mName # 7: " + mName);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .50), (int) ((height * .40)));

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        final EditText itemsSold = (EditText) findViewById(R.id.sale_quantity);

        TextView itemName = (TextView) findViewById(R.id.sale_item_title);
        itemName.setText(CatalogActivity.mName);


        Button okButton = (Button) findViewById(R.id.sale_purchase_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());

                int newInventory = CatalogActivity.mQuantity + 1;

                if (newInventory < 0) {
                    Toast.makeText(PopUpActivity.this, "Not enough inventory", Toast.LENGTH_SHORT).show();
                    finish();
                } else {

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.Inventory.ITEM_QUANTITY, newInventory);
                    int saleUpdate = getContentResolver().update(mCurrentUri, values, null, null);


                    ContentValues updateTableValues = new ContentValues();
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, CatalogActivity.mName);
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY, 1);
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, mTransactionTime);

                    Uri updTableReturnUri = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, updateTableValues);
                    long UriId = ContentUris.parseId(updTableReturnUri);
                    Log.e(LOG_TAG, "UriID: " + UriId);

                    if (saleUpdate > 0 && UriId > 0) {
                        Toast.makeText(PopUpActivity.this, "Sale Processed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PopUpActivity.this, "Failed Inventory Update", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(LOG_TAG, "mName # 9: " + mName);
                    finish();
                }
            }
        });
    }


}

