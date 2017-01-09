package com.example.android.autographs;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;
import com.example.android.autographs.data.InventoryContract.Inventory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.android.autographs.data.InventoryProvider.LOG_TAG;

/**
 * Created by dnj on 12/18/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {


    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final View listItemView = view;

        // get id
        int idCol = cursor.getColumnIndex(Inventory._ID);
        int id = Integer.parseInt(cursor.getString(idCol));
        final Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.INVENTORY_CONTENT_URI, id);

        // set name
        TextView nameView = (TextView) listItemView.findViewById(R.id.item_name);
        int nameCol = cursor.getColumnIndex(Inventory.ITEM_NAME);
        final String name = cursor.getString(nameCol);
        nameView.setText(name);

        // set price
        TextView priceView = (TextView) listItemView.findViewById(R.id.price);
        int priceCol = cursor.getColumnIndex(Inventory.ITEM_SALE_PRICE);
        double price = cursor.getDouble(priceCol);
        String priceString = "$" + DetailsActivity.formatCurrency(price);
        priceView.setText(priceString);

        // set quantity
        TextView quantity = (TextView) listItemView.findViewById(R.id.quantity);
        int quantityCol = cursor.getColumnIndex(Inventory.ITEM_QUANTITY);
        final int quantityInt = cursor.getInt(quantityCol);
        String quantityVal = Integer.toString(quantityInt) + " In-Stock";
        quantity.setText(quantityVal);

        // set image
        ImageView image = (ImageView) listItemView.findViewById(R.id.image_view);
        int imgCol = cursor.getColumnIndex(Inventory.ITEM_IMAGE);
        byte[] imgByte = cursor.getBlob(imgCol);
        Bitmap img = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        image.setImageBitmap(img);

        // Set Button onClick
        Button button = (Button) listItemView.findViewById(R.id.catalog_sale_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.finalize_sale);
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String transactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());

                        int newInventory = quantityInt - 1;

                        if (newInventory < 0) {
                            Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } else {

                            ContentValues values = new ContentValues();
                            values.put(InventoryContract.Inventory.ITEM_QUANTITY, newInventory);
                            int saleUpdate = context.getContentResolver().update(currentItemUri, values, null, null);


                            ContentValues updateTableValues = new ContentValues();
                            updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, name);
                            updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY, 1);
                            updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, transactionTime);

                            Uri updTableReturnUri = context.getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, updateTableValues);


                            if (saleUpdate > 0 && updTableReturnUri != null) {
                                Toast.makeText(context, "Sale Processed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed Inventory Update", Toast.LENGTH_SHORT).show();
                            }
                            Log.e(LOG_TAG, "mName # 9: " + name);
                            dialog.dismiss();

                        }
                    }
                });
                // create and show the AlertDialogue
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


    }

}




