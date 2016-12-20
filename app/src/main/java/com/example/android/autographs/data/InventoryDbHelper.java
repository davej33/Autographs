package com.example.android.autographs.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.autographs.data.InventoryContract.Inventory;

/**
 * Created by dnj on 12/17/16.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String INVENTORY_DB_NAME = "inventoryDb";
    public static final int DB_VERSION = 1;

    public InventoryDbHelper (Context context){
        super(context, INVENTORY_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_TABLE_CREATE = "CREATE TABLE " + Inventory.INV_TABLE_NAME + " ("
                + Inventory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Inventory.ITEM_NAME + " TEXT NOT NULL, "
                + Inventory.ITEM_SALE_PRICE + " REAL NOT NULL, "
                + Inventory.ITEM_QUANTITY + " INTEGER NOT NULL, "
                + Inventory.ITEM_SUPPLIER + " TEXT NOT NULL, "
                + Inventory.ITEM_IMAGE + " BLOB)";

        db.execSQL(SQL_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
