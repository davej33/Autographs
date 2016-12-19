package com.example.android.autographs.data;

import android.provider.BaseColumns;

/**
 * Created by dnj on 12/16/16.
 */

public class InventoryContract {

    public InventoryContract(){}

    public abstract static class ItemInventory implements BaseColumns{

        // table
        public static final String TABLE_NAME = "Items";

        // column headers
        public static final String _ID = BaseColumns._ID;
        public static final String ITEM_NAME = "name";
        public static final String ITEM_SALE_PRICE = "sale_price";
        public static final String ITEM_SUPPLIER = "supplier";
        public static final String ITEM_IMAGE = "image";

    }

    public abstract static class ItemInventoryUpdates implements BaseColumns{

        // table for sales, purchases, and manual invetory updates
        public static final String TABLE_NAME = "InventoryUpdates";

        // column headers
        public static final String UPDATE_ID = BaseColumns._ID;
        public static final String UPDATE_ITEM_NAME = "name";
        public static final String UPDATE_ITEM_ID = "item_id";
        public static final String UPDATE_TYPE = "type";
        public static final String UPDATE_DATE = "date";
        public static final String PURCHASE_RECEIVED = "order_received";

    }
}
