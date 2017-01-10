package com.example.android.autographs;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autographs.data.InventoryContract;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.android.autographs.CatalogActivity.mName;
import static com.example.android.autographs.R.layout.activity_details;
import static com.example.android.autographs.data.InventoryProvider.LOG_TAG;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // class vars
    private EditText mNameInsert;
    private EditText mPriceInsert;
    private EditText mQuantInsert;
    private EditText mSupInsert;
    private EditText mSupEmailInsert;
    private ImageView mImageInsert;
    private TextView mItemId;
    private String mTransactionTime;
    private String mSupplier;
    private String mSupplierEmail;
    private int mInStock;
    private byte[] mImage;
    private static final int SALE = 1;
    private static int RESULT_LOAD_IMAGE = 1;
    private int mInputTester;
    private DialogInterface.OnClickListener dialogueDismiss = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };
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

        // get intent info
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // flow statement to determine if item is being updated or inserted
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

        // set listview adapter
        ListView listView = (ListView) findViewById(R.id.item_transaction_history);
        mUpdateCursorAdapter = new UpdatesCursorAdapter(this, null);
        listView.setAdapter(mUpdateCursorAdapter);

        // get item view objects
        mNameInsert = (EditText) findViewById(R.id.insert_item_name);
        mPriceInsert = (EditText) findViewById(R.id.insert_price);
        mQuantInsert = (EditText) findViewById(R.id.insert_quantity);
        mSupInsert = (EditText) findViewById(R.id.insert_supplier);
        mSupEmailInsert = (EditText) findViewById(R.id.insert_supplier_email);
        mItemId = (TextView) findViewById(R.id.product_id);
        mImageInsert = (ImageView) findViewById(R.id.image_x);


        // set onTouch listeners
        mNameInsert.setOnTouchListener(mOnTouch);
        mPriceInsert.setOnTouchListener(mOnTouch);
        mQuantInsert.setOnTouchListener(mOnTouch);
        mSupInsert.setOnTouchListener(mOnTouch);
        mSupEmailInsert.setOnTouchListener(mOnTouch);
        mImageInsert.setOnTouchListener(mOnTouch);

        mImageInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        // increment inventory button
        Button plusButton = (Button) findViewById(R.id.quantity_plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get current value, initialize to zero if no current value
                String qPlus = mQuantInsert.getText().toString().trim();
                if(qPlus.isEmpty()){
                    qPlus = "0";
                }
                int qPlusInt = Integer.valueOf(qPlus) + 1;
                if (qPlusInt == 10001) {
                    Toast.makeText(DetailsActivity.this, "Inventory max 10000", Toast.LENGTH_SHORT).show();
                } else {
                    String qString = String.valueOf(qPlusInt);
                    mQuantInsert.setText(qString);
                }
            }
        });

        // decrement button for inventory
        Button minButton = (Button) findViewById(R.id.quantity_minus);
        minButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qMin = mQuantInsert.getText().toString().trim();
                if(qMin.isEmpty()){
                    qMin = "0";
                }
                int qMinInt = Integer.valueOf(qMin) - 1;
                if (qMinInt == -1) {
                    Toast.makeText(DetailsActivity.this, "Inventory cannot be negative", Toast.LENGTH_SHORT).show();
                } else {
                    String qString = String.valueOf(qMinInt);
                    mQuantInsert.setText(qString);
                }
            }
        });

        // order button
        final Button orderButton = (Button) findViewById(R.id.details_place_order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(DetailsActivity.this);
                View promptsView = li.inflate(R.layout.popup_layout, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailsActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.input_quantity);

                // set dialog message and onClick actions
                alertDialogBuilder
                        .setTitle(R.string.purchase_order)
                        .setMessage("Item: " + mName + "\n\nSupplier: " + mSupplier)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        // get transaction time
                                        mTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new Date());

                                        // get quantity entered by user
                                        String orderQuantity = userInput.getText().toString().trim();
                                        int orderInt;

                                        // if quantity is not empty and is between 1 - 1000
                                        if (!orderQuantity.isEmpty()) {
                                            orderInt = Integer.parseInt(orderQuantity);
                                            if (orderInt >= 1 && orderInt <= 1000) {

                                                // create content values and insert order into update db
                                                ContentValues valuesUpdateTable = new ContentValues();
                                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, mName);
                                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_PURCH_QUANTITY, orderQuantity);
                                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, mTransactionTime);

                                                Uri orderInsert = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, valuesUpdateTable);

                                                // if insert failed, toast and quit
                                                if (orderInsert == null) {
                                                    Toast.makeText(DetailsActivity.this, "DB input failed", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                } else {
                                                    // if insert successful, open email app and populate with order supplier and order info
                                                    final String emailBody = mSupplier + ",<br><br>We would like to make the following purchase:<br><br>-Item: " +
                                                            mName + "<br>-Quantity: " + orderQuantity + "<br><br>Thank You, <br><br>Dave<br>Autograph Kings";
                                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                    intent.setData(Uri.parse("mailto: " + mSupplierEmail));
                                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Order");
                                                    intent.putExtra(Intent.EXTRA_TEXT, emailBody);
                                                    startActivity(intent);
                                                }
                                                // send correct toast depending on singular or multiple item order
                                                if (orderInt == 1) {
                                                    Toast.makeText(DetailsActivity.this, orderQuantity + " item ordered", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(DetailsActivity.this, orderQuantity + " items ordered", Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                // if quantity not between 1-1000, toast and dismiss
                                                Toast.makeText(DetailsActivity.this, "Valid entries: 1 - 1000", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        } else {
                                            // if user doesn't enter a number, toast
                                            Toast.makeText(DetailsActivity.this, "No value provided", Toast.LENGTH_SHORT).show();
                                        }


                                    }

                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // order received button
        Button orderReceivedButton = (Button) findViewById(R.id.details_order_received);
        orderReceivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // inflate dialogue with layout res
                LayoutInflater layoutInflater = LayoutInflater.from(DetailsActivity.this);
                final View ordRecView = layoutInflater.inflate(R.layout.popup_layout, null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);
                dialog.setView(ordRecView);
                dialog.setTitle(R.string.order_recvd);
                dialog.setMessage("Item: " + mName + "\n\nSupplier: " + mSupplier);
                dialog.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // transaction time
                        mTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new Date());
                        EditText input = (EditText) ordRecView.findViewById(R.id.input_quantity);
                        String ordRecString = input.getText().toString().trim();
                        int ordRecInt;
                        if (!ordRecString.isEmpty()) {
                            ordRecInt = Integer.parseInt(ordRecString);
                            if (ordRecInt > 0 && ordRecInt <= 1000) {

                                // add quantity received to current inventory to get new inventory value
                                mInStock += ordRecInt;

                                // content values for inventory db
                                ContentValues valuesInventoryTable = new ContentValues();
                                valuesInventoryTable.put(InventoryContract.Inventory.ITEM_QUANTITY, mInStock);
                                String selection = InventoryContract.Inventory.ITEM_NAME + "=?";
                                String[] selectionArgs = {mName};

                                // content values for update db
                                ContentValues valuesUpdateTable = new ContentValues();
                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, mName);
                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED, ordRecString);
                                valuesUpdateTable.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, mTransactionTime);

                                // update inventory in inventory db
                                long inventoryTableUpdate = getContentResolver().update(InventoryContract.INVENTORY_CONTENT_URI, valuesInventoryTable,
                                        selection, selectionArgs);
                                if (inventoryTableUpdate == 0) {
                                    Toast.makeText(DetailsActivity.this, "Inventory update failed", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }

                                // insert into update db
                                Uri updateTableInsert = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, valuesUpdateTable);

                                // update insert fails, undo inventory update
                                if (updateTableInsert == null) {

                                    mInStock -= ordRecInt;

                                    ContentValues valuesInventoryTableUndo = new ContentValues();
                                    valuesInventoryTable.put(InventoryContract.Inventory.ITEM_QUANTITY, mInStock);

                                    long undoInventoryUpdate = getContentResolver().update(InventoryContract.INVENTORY_CONTENT_URI, valuesInventoryTableUndo,
                                            selection, selectionArgs);

                                }
                            } else {
                                Toast.makeText(DetailsActivity.this, "Valid entries: 1 - 1000", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(DetailsActivity.this, "No value entered", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }


                    }

                });
                dialog.setNegativeButton(R.string.popup_cancel, dialogueDismiss);
                // create alert dialog
                AlertDialog alertDialog = dialog.create();

                // show it
                alertDialog.show();
            }

        });


        // sale button
        Button saleButton = (Button) findViewById(R.id.details_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemSale();
            }
        });
    }// on create end

    public void saveItem() {

        // get values from objects
        String name = mNameInsert.getText().toString().trim();
        String price = mPriceInsert.getText().toString().trim();
        String quantity = mQuantInsert.getText().toString().trim();
        String supplier = mSupInsert.getText().toString().trim();
        String supEmail = mSupEmailInsert.getText().toString().trim();
        Bitmap bitmap = ((BitmapDrawable)mImageInsert.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();

        // used to verify all field populated
        mInputTester = 0;

        // create content value object and populate after data verified
        ContentValues insertValues = new ContentValues();

        if (!name.isEmpty()) {
            insertValues.put(InventoryContract.Inventory.ITEM_NAME, name);
            mInputTester += 1;
        }

        String priceSet = "";
        if (!price.isEmpty()) {
            double priceSetDouble = Double.valueOf(price);
            priceSet = formatCurrency(priceSetDouble);
            insertValues.put(InventoryContract.Inventory.ITEM_SALE_PRICE, priceSet);
            mInputTester += 1;
        }

        if (!supplier.isEmpty()) {
            insertValues.put(InventoryContract.Inventory.ITEM_SUPPLIER, supplier);
            mInputTester += 1;
        }

        if (!supEmail.isEmpty()) {
            insertValues.put(InventoryContract.Inventory.ITEM_SUPPLIER_EMAIL, supEmail);
            mInputTester += 1;
        }

        int quantSet = -1;
        if (!quantity.isEmpty()) {
            quantSet = Integer.parseInt(quantity);
            mInputTester += 1;
            if (quantSet < 0 || quantSet >= 10000) {
                Toast.makeText(this, "Inventory required (0 - 10000)", Toast.LENGTH_SHORT).show();
            }
        }

        int editChanged = 0;
        Uri insertTransaction;
        if (mInStock != quantSet) {
            editChanged = -1 * (mInStock - quantSet);
            insertValues.put(InventoryContract.Inventory.ITEM_QUANTITY, quantSet);
        }

        insertValues.put(InventoryContract.Inventory.ITEM_IMAGE, imageInByte);

        mTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());

        ContentValues transactionValues = new ContentValues();
        transactionValues.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, name);
        transactionValues.put(InventoryContract.InventoryUpdates.UPDATE_MANUAL_EDIT, editChanged);
        transactionValues.put(InventoryContract.InventoryUpdates.UPDATE_SUPPLIER, supplier);
        transactionValues.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, mTransactionTime);

        // if all data fields insert or update
        if (mInputTester == 5) {
            // insert into DB or update
            if (mCurrentItemUri == null) {
                if (uniqueNameChecker(name)) {
                    Uri insertItem = getContentResolver().insert(InventoryContract.INVENTORY_CONTENT_URI, insertValues);
                    insertTransaction = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, transactionValues);
                    if (insertItem != null && insertTransaction != null) {
                        Toast.makeText(this, "Item Successfully Added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error Adding Item", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Item name already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                int updateItem = getContentResolver().update(mCurrentItemUri, insertValues, null, null);
                insertTransaction = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, transactionValues);

                if (updateItem == 0 && insertTransaction == null) {
                    Toast.makeText(this, "Item Update Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Item Successfully Update", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
        }
    }

    // check if item name already exists
    private boolean uniqueNameChecker(String newName) {
        String selection = InventoryContract.Inventory.ITEM_NAME + "=?";
        String[] selectionArgs = {newName};
        Cursor query = getContentResolver().query(InventoryContract.INVENTORY_CONTENT_URI,
                null, selection, selectionArgs, null);
        if (query.moveToFirst()) {
            return false;
        } else {
            return true;

        }
    }

    // item sale method
    public void itemSale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
        builder.setMessage(R.string.finalize_sale);
        builder.setNegativeButton(R.string.no, dialogueDismiss);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTransactionTime = new SimpleDateFormat("MMM-dd-yy HH:mm", Locale.US).format(new java.util.Date());

                int newInventory = CatalogActivity.mQuantity - 1;

                if (newInventory < 0) {
                    Toast.makeText(DetailsActivity.this, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                    finish();
                } else {

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.Inventory.ITEM_QUANTITY, newInventory);
                    int saleUpdate = getContentResolver().update(mCurrentItemUri, values, null, null);


                    ContentValues updateTableValues = new ContentValues();
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME, CatalogActivity.mName);
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY, SALE);
                    updateTableValues.put(InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME, mTransactionTime);

                    Uri updTableReturnUri = getContentResolver().insert(InventoryContract.UPDATES_CONTENT_URI, updateTableValues);
                    long UriId = ContentUris.parseId(updTableReturnUri);
                    Log.e(LOG_TAG, "UriID: " + UriId);

                    if (saleUpdate > 0 && UriId > 0) {
                        Toast.makeText(DetailsActivity.this, "Sale Processed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailsActivity.this, "Failed Inventory Update", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(LOG_TAG, "mName # 9: " + mName);
                    finish();

                }
            }
        });
        // create and show the AlertDialogue
        AlertDialog alert = builder.create();
        alert.show();
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
                if (mInputTester == 5) {
                    finish();
                }
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
        if (id == INV_CURSOR_LOADER_ID) {
            String[] projection = {
                    InventoryContract.Inventory._ID,
                    InventoryContract.Inventory.ITEM_NAME,
                    InventoryContract.Inventory.ITEM_SALE_PRICE,
                    InventoryContract.Inventory.ITEM_QUANTITY,
                    InventoryContract.Inventory.ITEM_SUPPLIER,
                    InventoryContract.Inventory.ITEM_SUPPLIER_EMAIL,
                    InventoryContract.Inventory.ITEM_IMAGE};

            loader = new CursorLoader(this,
                    mCurrentItemUri, projection,
                    null, null, null);
        }
        if (id == UPD_CURSOR_LOADER_ID) {
            String[] projection = {
                    InventoryContract.InventoryUpdates.UPDATE_ID,
                    InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME,
                    InventoryContract.InventoryUpdates.UPDATE_SALE_QUANTITY,
                    InventoryContract.InventoryUpdates.UPDATE_PURCH_QUANTITY,
                    InventoryContract.InventoryUpdates.UPDATE_PURCHASE_RECEIVED,
                    InventoryContract.InventoryUpdates.UPDATE_MANUAL_EDIT,
                    InventoryContract.InventoryUpdates.UPDATE_TRANSACTION_DATETIME
            };

            String selection = InventoryContract.InventoryUpdates.UPDATE_ITEM_NAME + "=?";
            String[] selectionArgs = {mName};
            loader = new CursorLoader(this, InventoryContract.UPDATES_CONTENT_URI, projection,
                    selection, selectionArgs, null);
        }

        return loader;


    }

    public static String formatCurrency(Double d) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(d);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            mImageInsert.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            try {
                FileInputStream fis = new FileInputStream(picturePath);
                mImage = new byte[fis.available()];
                fis.read(mImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  BitmapFactory.decodeFile(picturePath);

            Log.e(LOG_TAG, "picture path string: " + picturePath);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case INV_CURSOR_LOADER_ID:
                if (data.moveToFirst()) {
                    int idCol = data.getColumnIndex(InventoryContract.Inventory._ID);
                    int nameCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_NAME);
                    int priceCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SALE_PRICE);
                    int quantCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_QUANTITY);
                    int supCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SUPPLIER);
                    int supEmCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_SUPPLIER_EMAIL);
                    int imgCol = data.getColumnIndex(InventoryContract.Inventory.ITEM_IMAGE);

                    String id = data.getString(idCol);
                    Log.e(LOG_TAG, "id: " + id);
                    String name = data.getString(nameCol);
                    Double price = data.getDouble(priceCol);
                    String priceString = formatCurrency(price);
                    int quantity = data.getInt(quantCol);
                    String quantString = String.valueOf(quantity);
                    String supplier = data.getString(supCol);
                    String supEmail = data.getString(supEmCol);
                    byte[] image = data.getBlob(imgCol);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

                    mItemId.setText(id);
                    mNameInsert.setText(name);
                    mPriceInsert.setText(priceString);
                    mQuantInsert.setText(quantString);
                    mSupInsert.setText(supplier);
                    mSupEmailInsert.setText(supEmail);
                    mImageInsert.setImageBitmap(bitmap);

                    CatalogActivity.mQuantity = quantity;
                    mName = name;
                    mSupplier = supplier;
                    mSupplierEmail = supEmail;
                    mInStock = quantity;
                    mImage = image;

                }
                break;
            case UPD_CURSOR_LOADER_ID:
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
