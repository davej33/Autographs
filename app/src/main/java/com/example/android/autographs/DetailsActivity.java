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
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

import static com.example.android.autographs.R.layout.activity_details;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // globals
    private EditText mNameInsert;
    private EditText mPriceInsert;
    private EditText mQuantInsert;
    private EditText mSupInsert;

    public static int mQuantity;
    public static String mName;

    UpdatesCursorAdapter mUpdateCursorAdapter;

    Uri mCurrentItemUri;
    private static final int INV_CURSOR_LOADER_ID = 0;
    private static final int UPD_CURSOR_LOADER_ID = 1;
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
        setContentView(activity_details);

        ListView listView = (ListView) findViewById(R.id.item_transaction_history);
        mUpdateCursorAdapter = new UpdatesCursorAdapter(this, null);
        listView.setAdapter(mUpdateCursorAdapter);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri != null) {
            setTitle(R.string.detail_edit_item);
            getLoaderManager().initLoader(INV_CURSOR_LOADER_ID, null, this);
            getLoaderManager().initLoader(UPD_CURSOR_LOADER_ID, null, this);
        } else {
            setTitle(R.string.detail_insert);
            invalidateOptionsMenu();
            PercentRelativeLayout buttonsLayout = (PercentRelativeLayout) findViewById(R.id.item_inventory_buttons);
            buttonsLayout.setVisibility(View.GONE);
            PercentRelativeLayout listLayout = (PercentRelativeLayout) findViewById(R.id.item_inventory_list);
            listLayout.setVisibility(View.GONE);
        }

        // get item view objects
        mNameInsert = (EditText) findViewById(R.id.insert_item_name);
        mPriceInsert = (EditText) findViewById(R.id.insert_price);
        mQuantInsert = (EditText) findViewById(R.id.insert_quantity);
        mSupInsert = (EditText) findViewById(R.id.insert_supplier);

        // set onTouch listeners
        mNameInsert.setOnTouchListener(mOnTouch);
        mPriceInsert.setOnTouchListener(mOnTouch);
        mQuantInsert.setOnTouchListener(mOnTouch);
        mSupInsert.setOnTouchListener(mOnTouch);

        // buttons onClickListener and intent
        Button saleButton = (Button) findViewById(R.id.details_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, PopUpActivity.class);
                intent.setData(mCurrentItemUri);
                startActivity(intent);
            }
        });
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

        double priceSet = 0;
        if (!price.isEmpty()) {
            priceSet = Double.parseDouble(price);
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
            if (updateItem == 0) {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Successful update", Toast.LENGTH_SHORT).show();
            }
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
            case R.id.detail_item_delete:
                showDeleteConfirmDialog();
                return true;

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

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_delete_message);
        builder.setPositiveButton(R.string.detail_delete_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.detail_delete_no, new DialogInterface.OnClickListener() {
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

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowDeleted == 0) {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Delete successful", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader loader = null;
        if (id == 0) {
            String[] projection = {
                    InventoryContract.Inventory.ITEM_NAME,
                    InventoryContract.Inventory.ITEM_SALE_PRICE,
                    InventoryContract.Inventory.ITEM_QUANTITY,
                    InventoryContract.Inventory.ITEM_SUPPLIER};

            loader = new CursorLoader(this,
                    mCurrentItemUri, projection,
                    null, null, null);
        }
        if (id == 1) {
            String[] projection = {
                    InventoryContract.InventoryUpdates.UPDATE_ID,
                    InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY,
                    InventoryContract.InventoryUpdates.UPDATE_PURCH_QUANTITY,
                    InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED,
                    InventoryContract.InventoryUpdates.UPDATE_MANUAL_EDIT,
                    InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME
            };

            String selection = InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME + "=?";
            String[] selectionArgs = {"Test Item"};
            loader = new CursorLoader(this, InventoryContract.UPDATES_CONTENT_URI, projection,
                    selection, selectionArgs, null);
        }

        return loader;


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case 0:
                if (data.moveToFirst()) {
                    int nameCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_NAME);
                    int priceCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SALE_PRICE);
                    int quantCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_QUANTITY);
                    int supCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SUPPLIER);

                    String name = data.getString(nameCol);
                    Double price = data.getDouble(priceCol);
                    String priceString = String.valueOf(price);
                    int quantity = data.getInt(quantCol);
                    String quantString = String.valueOf(quantity);
                    String supplier = data.getColumnName(supCol);

                    mNameInsert.setText(mName);
                    mPriceInsert.setText(priceString);
                    mQuantInsert.setText(quantString);
                    mSupInsert.setText(supplier);

                    mQuantity = quantity;
                    mName = name;
                }
                break;
            case 1:
                mUpdateCursorAdapter.swapCursor(data);
                break;
            default:
                throw new IllegalArgumentException();
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
