package foerstermann.kai.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class ShoppingMemoDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ShoppingMemoDbHelper.class.getSimpleName();
    private static final String DB_NAME = "ShoppingMemo.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_SHOPPING_LIST = "shoppingList";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCT = "product";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_CHECKED = "checked";

    public ShoppingMemoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE =
                "CREATE TABLE " + TABLE_SHOPPING_LIST +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                      COLUMN_PRODUCT + " TEXT NOT NULL, " +
                      COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                      COLUMN_CHECKED + " BOOLEAN NOT NULL DEFAULT 0);";
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            db.execSQL(SQL_CREATE);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Die Tabelle mit Versionsnummer " + oldVersion + " wird entfernt.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPPING_LIST);
        onCreate(db);
    }


}
