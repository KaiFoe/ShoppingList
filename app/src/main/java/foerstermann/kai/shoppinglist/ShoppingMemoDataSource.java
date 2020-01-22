package foerstermann.kai.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShoppingMemoDataSource {

    private static final String LOG_TAG = ShoppingMemoDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;

    private String[] columns = {ShoppingMemoDbHelper.COLUMN_ID,
                                ShoppingMemoDbHelper.COLUMN_PRODUCT,
                                ShoppingMemoDbHelper.COLUMN_QUANTITY};

    public ShoppingMemoDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new ShoppingMemoDbHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbankreferenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public ShoppingMemo createShoppingMemo(String product, int quantity) {
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT, product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY, quantity);

        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST, null, values);

        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, ShoppingMemoDbHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();

        return  shoppingMemo;
    }

    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);

        String product = cursor.getString(idProduct);
        int quantity = cursor.getInt(idQuantity);
        long id = cursor.getLong(idIndex);

        ShoppingMemo shoppingMemo = new ShoppingMemo(product, quantity, id);

        return shoppingMemo;
    }

    public List<ShoppingMemo> getAllShoppingMemos() {
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();

        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, null, null, null, null, null);

        cursor.moveToFirst();

        ShoppingMemo shoppingMemo;

        while (!cursor.isAfterLast()) {
            shoppingMemo = cursorToShoppingMemo(cursor);
            shoppingMemoList.add(shoppingMemo);
            Log.d(LOG_TAG, "ID: " + shoppingMemo.getId() + ", Inhalt: " + shoppingMemo.toString());
            cursor.moveToNext();
        }
        cursor.close();

        return shoppingMemoList;
    }
}
