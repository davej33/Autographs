package com.example.android.autographs;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;
import com.example.android.autographs.data.InventoryContract.Inventory;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.android.autographs.data.InventoryProvider.LOG_TAG;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static int mQuantity;
    public static String mName;
    private String mPosition;

    InventoryCursorAdapter mCursorAdapter;

    private static final int CURSOR_LOADER_ID = 0;
    private static final int GET_NAME_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        Log.e(LOG_TAG, "mName # 1: " + mName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });


        ListView listview = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listview.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listview.setAdapter(mCursorAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.INVENTORY_CONTENT_URI, id);
                intent.setData(currentItemUri);

                mPosition = String.valueOf(position + 1);
                Log.e(LOG_TAG, "mPosition String: " + mPosition);

                getLoaderManager().initLoader(GET_NAME_LOADER_ID, null, CatalogActivity.this);
                Log.e(LOG_TAG, "### 1.75: " + mName);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void insertItem() {

        String testName = "Kyle Schwarber Signed Bat";
        double testSalePrice = 400.00;
        int testQuant = 3;
        String testEmail = "davidlgc33@gmail.com";
        String testSup = "MLB Shop";
        double testPurchasePrice = 10.00;
        String testTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());
        Bitmap tempBMP = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
        tempBMP.compress(Bitmap.CompressFormat.JPEG, 100, arrayOutput);
        byte[] img = arrayOutput.toByteArray();

        // insert values into Inventory
        ContentValues dummyItem = new ContentValues();
        dummyItem.put(Inventory.ITEM_NAME, testName);
        dummyItem.put(Inventory.ITEM_SALE_PRICE, testSalePrice);
        dummyItem.put(Inventory.ITEM_QUANTITY, testQuant);
        dummyItem.put(Inventory.ITEM_SUPPLIER, testSup);
        dummyItem.put(Inventory.ITEM_SUPPLIER_EMAIL, testEmail);
        dummyItem.put(Inventory.ITEM_IMAGE, img);

        Uri dummyUri = getContentResolver().insert(
                InventoryContract.INVENTORY_CONTENT_URI, dummyItem);

//        long testItemIdLong = ContentUris.parseId(dummyUri);

        // insert values into Updates
        ContentValues dummyInsertUpdates = new ContentValues();
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, testName);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_PURCH_PRICE, testPurchasePrice);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED, testQuant);
        dummyInsertUpdates.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, testTransactionTime);

        Uri dummyUriUpdate = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, dummyInsertUpdates);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.dummy_insert:
                insertItem();
                return true;
            case R.id.delete_all:
                deleteAll();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        int deletedRows = getContentResolver().delete(InventoryContract.INVENTORY_CONTENT_URI, null, null);
        if (deletedRows == 0) {
            Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Successful Deletion", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader loader = null;

        if (id == CURSOR_LOADER_ID) {
            String[] projection = {
                    Inventory._ID,
                    Inventory.ITEM_NAME,
                    Inventory.ITEM_SALE_PRICE,
                    Inventory.ITEM_QUANTITY,
                    Inventory.ITEM_IMAGE};

            loader = new CursorLoader(this,
                    InventoryContract.INVENTORY_CONTENT_URI,
                    projection, null, null, null);
        }

        if (id == GET_NAME_LOADER_ID) {
            String selection = Inventory._ID + "=?";
            String[] selectionArgs = {mPosition};

            String[] projection = {
                    Inventory._ID,
                    Inventory.ITEM_NAME,
                    Inventory.ITEM_SALE_PRICE};

            loader = new CursorLoader(this, InventoryContract.INVENTORY_CONTENT_URI,
                    projection, selection, selectionArgs, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case GET_NAME_LOADER_ID:
                if (data.moveToFirst()) {
                    int priceCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SALE_PRICE);
                    int idCol = data.getColumnIndex(Inventory._ID);
                    int nameCol = data.getColumnIndex(Inventory.ITEM_NAME);

                    double p = data.getDouble(priceCol);
                    int id = data.getInt(idCol);
                    mName = data.getString(nameCol);
                    Log.e(LOG_TAG, "id: " + id + " price: " + p + " mName # 1.5: " + mName);
                }
                break;
            case CURSOR_LOADER_ID:
                mCursorAdapter.swapCursor(data);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
