package com.example.android.autographs.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dnj on 12/16/16.
 */

public class InventoryContract {

    // URI constants
    public static final String CONTENT_AUTHORITY = "com.example.android.autographs";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String INVENTORY_PATH = "inventory";
    public static final String UPDATES_PATH = "updates";
    public static final Uri INVENTORY_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVENTORY_PATH);
    public static final Uri UPDATES_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, UPDATES_PATH);

    // empty constructor
    public InventoryContract() {
    }

    public abstract static class Inventory implements BaseColumns {

        // table
        public static final String INV_TABLE_NAME = "inventory";

        // column headers
        public static final String _ID = BaseColumns._ID;
        public static final String ITEM_NAME = "name";
        public static final String ITEM_SALE_PRICE = "sale_price";
        public static final String ITEM_QUANTITY = "quantity";
        public static final String ITEM_SUPPLIER = "supplier";
        public static final String ITEM_IMAGE = "image";

    }

    public abstract static class InventoryUpdates implements BaseColumns {

        // table to track sales, purchases, and manual inventory updates
        public static final String UPD_TABLE_NAME = "updates";

        // column headers
        public static final String UPDATE_ID = BaseColumns._ID;
        public static final String UPDATE_ITEM_NAME = "name";
        public static final String UPDATE_ITEM_ID = "item_id";
        public static final String UPDATE_TYPE = "type";
        public static final String UPDATE_DATE = "date";
        public static final String PURCHASE_RECEIVED = "order_received";

    }

    public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/" + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/" + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;
}
