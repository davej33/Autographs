package com.example.android.autographs;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // globals
    EditText mNameInsert;
    EditText mPriceInsert;
    EditText mQuantInsert;
    EditText mSupInsert;
    Uri mCurrentItemUri;
    private static final int CURSOR_LOADER_ID = 0;
    private boolean mItemChanged = false;
    private View.OnTouchListener mOnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri != null) {
            setTitle(R.string.detail_edit_item);
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        } else {
            setTitle(R.string.detail_insert);
            invalidateOptionsMenu();
            LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.item_inventory_buttons);
            buttonsLayout.setVisibility(View.GONE);
        }

        // get view objects
        mNameInsert = (EditText) findViewById(R.id.insert_item_name);
        mPriceInsert = (EditText) findViewById(R.id.insert_price);
        mQuantInsert = (EditText) findViewById(R.id.insert_quantity);
        mSupInsert = (EditText) findViewById(R.id.insert_supplier);

        // set onTouch listeners
        mNameInsert.setOnTouchListener(mOnTouch);
        mPriceInsert.setOnTouchListener(mOnTouch);
        mQuantInsert.setOnTouchListener(mOnTouch);
        mSupInsert.setOnTouchListener(mOnTouch);

    }

    public void saveItem() {

        // get values from objects
        String name = mNameInsert.getText().toString().trim();
        String price = mPriceInsert.getText().toString().trim();
        String quantity = mQuantInsert.getText().toString().trim();
        String supplier = mSupInsert.getText().toString().trim();

        // check for empty values
        if (name.isEmpty() && price.isEmpty() && quantity.isEmpty() && supplier.isEmpty()) {
            return;
        }

        // create content value object and populate
        ContentValues insertValues = new ContentValues();
        insertValues.put(InventoryContract.Inventory.ITEM_NAME, name);
        insertValues.put(InventoryContract.Inventory.ITEM_SUPPLIER, supplier);

        long priceSet = 0;
        if (!price.isEmpty()) {
            priceSet = Long.parseLong(price);
        }
        insertValues.put(InventoryContract.Inventory.ITEM_SALE_PRICE, priceSet);

        int quantSet = 0;
        if (!quantity.isEmpty()) {
            quantSet = Integer.parseInt(quantity);
        }
        insertValues.put(InventoryContract.Inventory.ITEM_QUANTITY, quantSet);


        // insert into DB or update
        if (mCurrentItemUri == null) {

            Uri insertItem = getContentResolver().insert(InventoryContract.INVENTORY_CONTENT_URI, insertValues);

            if (insertItem != null) {
                Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error Adding Item", Toast.LENGTH_SHORT).show();
            }
        } else {
            int updateItem = getContentResolver().update(mCurrentItemUri, insertValues, null, null);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentItemUri == null) {
            MenuItem item = menu.findItem(R.id.detail_item_delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item_save:
                saveItem();
                finish();
                break;
            case android.R.id.home:
                // navigate back if item unchanged
                if (!mItemChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                // if changes made, setup dialog
                DialogInterface.OnClickListener discard = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    }
                };

                showUnsavedChangesDialog(discard);
                return true;

            case R.id.detail_item_delete:
                DialogInterface.OnClickListener delete = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(mCurrentItemUri, InventoryContract.Inventory.INV_TABLE_NAME,
                                null);
                        NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    }
                };
                showDeleteItemDialog(delete);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.Inventory.ITEM_NAME,
                InventoryContract.Inventory.ITEM_SALE_PRICE,
                InventoryContract.Inventory.ITEM_QUANTITY,
                InventoryContract.Inventory.ITEM_SUPPLIER};

        return new CursorLoader(this,
                mCurrentItemUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {
            int nameCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_NAME);
            int priceCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SALE_PRICE);
            int quantCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_QUANTITY);
            int supCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SUPPLIER);

            String name = data.getString(nameCol);
            long price = data.getLong(priceCol);
            String priceString = String.valueOf(price);
            int quantity = data.getInt(quantCol);
            String quantString = String.valueOf(quantity);
            String supplier = data.getColumnName(supCol);

            mNameInsert.setText(name);
            mPriceInsert.setText(priceString);
            mQuantInsert.setText(quantString);
            mSupInsert.setText(supplier);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameInsert.setText("");
        mPriceInsert.setText("");
        mQuantInsert.setText("");
        mSupInsert.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // create DialogueBuilder and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_unsaved_message);
        builder.setPositiveButton(R.string.detail_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.detail_keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void showDeleteItemDialog(DialogInterface.OnClickListener deleteButtonClickListener) {

        // create DialogueBuilder and set values
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_delete_message);
        builder.setPositiveButton(R.string.detail_confirm_delete, deleteButtonClickListener);
        builder.setNegativeButton(R.string.detail_cancel_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show the AlertDialogue
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
