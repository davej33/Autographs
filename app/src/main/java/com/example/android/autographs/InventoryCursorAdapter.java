package com.example.android.autographs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.autographs.data.InventoryContract.Inventory;

/**
 * Created by dnj on 12/18/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View listItemView = view;

        // set name
        TextView nameView = (TextView) listItemView.findViewById(R.id.item_name);
        int nameCol = cursor.getColumnIndex(Inventory.ITEM_NAME);
        String name = cursor.getString(nameCol);
        nameView.setText(name);

        // set price
        TextView priceView = (TextView) listItemView.findViewById(R.id.price);
        int priceCol = cursor.getColumnIndex(Inventory.ITEM_SALE_PRICE);
        String priceString = Double.toString(cursor.getDouble(priceCol));
        priceView.setText(priceString);

        // set quantity
        TextView quantity = (TextView) listItemView.findViewById(R.id.quantity);
        int quantityCol = cursor.getColumnIndex(Inventory.ITEM_QUANTITY);
        String quantityVal = Integer.toString(cursor.getInt(quantityCol));
        quantity.setText(quantityVal);

        // set image
        ImageView image = (ImageView) listItemView.findViewById(R.id.image_view);


    }
}


