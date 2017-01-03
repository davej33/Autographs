package com.example.android.autographs;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

/**
 * Created by dnj on 1/3/17.
 */

public class PopUpActivity extends AppCompatActivity {

    Uri mCurrentUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .50), (int) ((height * .75)));

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        final EditText itemsSold = (EditText) findViewById(R.id.sale_quantity);


        Button okButton = (Button) findViewById(R.id.sale_purchase_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int itemsSoldInt = Integer.parseInt(itemsSold.getText().toString());

                int newInventory = DetailsActivity.mQuantity - itemsSoldInt;
                if (newInventory < 0) {
                    Toast.makeText(PopUpActivity.this, "Not enough inventory", Toast.LENGTH_SHORT).show();
                    //throw new IllegalArgumentException();
                    finish();
                }

                ContentValues values = new ContentValues();
                values.put(InventoryContract.Inventory.ITEM_QUANTITY, newInventory);

                int saleUpdate = getContentResolver().update(mCurrentUri, values, null, null);
                if(saleUpdate > 0){
                    Toast.makeText(PopUpActivity.this, "Successful Inventory Update", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PopUpActivity.this, "Failed Inventory Update", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }
}
