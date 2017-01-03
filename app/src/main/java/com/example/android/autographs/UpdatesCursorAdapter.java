package com.example.android.autographs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.autographs.data.InventoryContract;

/**
 * Created by dnj on 1/2/17.
 */

public class UpdatesCursorAdapter extends CursorAdapter {

    public UpdatesCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.updates_item_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View listItemView = view;

        // set id
        TextView transactionId = (TextView) listItemView.findViewById(R.id.transaction_id);
        int transactionCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_ID);
        String id = cursor.getString(transactionCol);
        transactionId.setText(id);

        // set manual edit
        TextView editView = (TextView) listItemView.findViewById(R.id.edits);
        int editCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_MANUAL_EDIT);
        String editString = Integer.toString(cursor.getInt(editCol));
        editView.setText(editString);

        // set item sales
        TextView saleView = (TextView) listItemView.findViewById(R.id.sale_quant);
        int priceCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY);
        String saleString = Integer.toString(cursor.getInt(priceCol));
        saleView.setText(saleString);

        // set item order
        TextView orderView = (TextView) listItemView.findViewById(R.id.order_quant);
        int orderCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_PURCH_QUANTITY);
        String orderString = Integer.toString(cursor.getInt(orderCol));
        orderView.setText(orderString);

        // set order received
        TextView orderRecView = (TextView) listItemView.findViewById(R.id.order_received);
        int orderRecCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED);
        String orderRecString = Integer.toString(cursor.getInt(orderRecCol));
        orderRecView.setText(orderRecString);

        // set data and time of transaction
        TextView dateView = (TextView) listItemView.findViewById(R.id.date);
        int dateCol = cursor.getColumnIndex(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME);
        String dateString = cursor.getString(dateCol);
        dateView.setText(dateString);

    }
}
