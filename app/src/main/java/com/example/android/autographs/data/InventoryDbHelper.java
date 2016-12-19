package com.example.android.autographs.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.autographs.data.InventoryContract.ItemInventory;
/**
 * Created by dnj on 12/17/16.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String INVENTORY_DB_NAME = "Inventory";
    public static final int DB_VERSION = 1;

    public InventoryDbHelper (Context context){
        super(context, INVENTORY_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_TABLE_CREATE = "CREATE TABLE " + ItemInventory.TABLE_NAME + " ("
                + ItemInventory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemInventory.ITEM_NAME + " TEXT NOT NULL, "
                + ItemInventory.ITEM_SALE_PRICE + " REAL NOT NULL, "
                + ItemInventory.ITEM_SUPPLIER + " TEXT NOT NULL, "
                + ItemInventory.ITEM_IMAGE + " BLOB)";

        db.execSQL(SQL_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
