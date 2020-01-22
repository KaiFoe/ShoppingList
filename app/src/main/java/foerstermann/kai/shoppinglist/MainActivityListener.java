package foerstermann.kai.shoppinglist;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.w3c.dom.Text;

import java.util.List;

class MainActivityListener implements View.OnClickListener {

    MainActivity mainActivity;
    private ShoppingMemoDataSource dataSource;

    public static final String LOG_TAG = MainActivityListener.class.getSimpleName();

    public MainActivityListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        dataSource = new ShoppingMemoDataSource(mainActivity);
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden.");
        ShowAllEntries();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnAddProduct) {

            String quantityString = mainActivity.txtquantity.getText().toString();
            String product = mainActivity.txtproduct.getText().toString();

            if (TextUtils.isEmpty(quantityString)) {
                mainActivity.txtquantity.setError(mainActivity.getString(R.string.editText_errorMessage));
                return;
            }
            if (TextUtils.isEmpty(product)) {
                mainActivity.txtproduct.setError(mainActivity.getString(R.string.editText_errorMessage));
                return;
            }
            int quantity = Integer.parseInt(quantityString);
            mainActivity.txtquantity.setText("");
            mainActivity.txtproduct.setText("");

            dataSource.createShoppingMemo(product, quantity);

            InputMethodManager inputMethodManager;
            inputMethodManager = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mainActivity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(mainActivity.getCurrentFocus().getWindowToken(),0);
            }
            ShowAllEntries();
        }

    }

    private void ShowAllEntries() {
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();

        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_multiple_choice,
                shoppingMemoList);

        ListView shoppingMemoListView = (ListView) mainActivity.findViewById(R.id.lvShoppingMemos);
        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);
    }

    public void onResume() {
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        ShowAllEntries();
    }

    public void onPause() {
        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }
}
