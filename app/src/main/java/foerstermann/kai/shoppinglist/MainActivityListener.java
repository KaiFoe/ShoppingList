package foerstermann.kai.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class MainActivityListener implements View.OnClickListener {

    MainActivity mainActivity;
    private ShoppingMemoDataSource dataSource;

    public static final String LOG_TAG = MainActivityListener.class.getSimpleName();

    public MainActivityListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        dataSource = new ShoppingMemoDataSource(mainActivity);
        initializeShoppingMemoListView();
        initializeContextualActionBar();

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden.");
        showAllEntries();

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
            showAllEntries();
            initializeContextualActionBar();
        }

    }

    private void showAllEntries() {
        List<ShoppingMemo> shoppingMemoList = dataSource.getAllShoppingMemos();

        ListView shoppingMemoListView = mainActivity.findViewById(R.id.lvShoppingMemos);
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = (ArrayAdapter<ShoppingMemo>) shoppingMemoListView.getAdapter();
        shoppingMemoArrayAdapter.clear();
        shoppingMemoArrayAdapter.addAll(shoppingMemoList);
        shoppingMemoArrayAdapter.notifyDataSetChanged();
    }

    public void onResume() {
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        showAllEntries();
    }

    public void onPause() {
        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

    private void initializeContextualActionBar() {
        final ListView shoppingMemoListView = (ListView) mainActivity.findViewById(R.id.lvShoppingMemos);
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            //Zählen der ausgewählten Listeneinträge
            //Fordern einer Aktualisierung der ContextualActionBar
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + mainActivity.getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            //Anlegen der Menüeinträge
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mainActivity.getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }


            //Reaktion auf invalidate()
            //Edit-Symbol verschwindet wenn mehr als 1 Eintrag ausgewählt wurde
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1)
                    item.setVisible(true);
                else
                    item.setVisible(false);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedShoppingMemosPositions = shoppingMemoListView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    case R.id.cab_delete:

                        for (int i=0; i < touchedShoppingMemosPositions.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPositions.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);
                                Log.d(LOG_TAG, "Position in ListView: " + positionInListView + " Inhalt: " + shoppingMemo.toString());
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }
                        showAllEntries();
                        mode.finish();
                        break;
                    case R.id.cab_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
                        for (int i = 0; i < touchedShoppingMemosPositions.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPositions.keyAt(i);
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);
                                AlertDialog editShoppingMemoDialog = createEditShoppingMemoDialog(shoppingMemo);
                                editShoppingMemoDialog.show();
                            }
                        }
                        mode.finish();
                        break;
                    default:
                        returnValue = false;
                        break;
                }
                return returnValue;
            }

            //Reaktion auf das Schließen der ContextualActionBar
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }

    private void initializeShoppingMemoListView() {
        List<ShoppingMemo> emptyListForInitialization = new ArrayList<>();
        final ListView shoppingMemoListView = (ListView) mainActivity.findViewById(R.id.lvShoppingMemos);

        //Erstellen des ArrayAdapters für unsere ListView
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(
                mainActivity,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {

            //Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnet
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                ShoppingMemo memo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(position);

                //Hier prüfen wir, ob der Eintrag abgehakt ist
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175, 175, 175));
                } else {
                    textView.setPaintFlags(textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }
                return view;
            }
        };
        
        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);

        shoppingMemoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingMemo memo  = (ShoppingMemo) parent.getItemAtPosition(position);

                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedShoppingMemo.toString() + " ist: " + updatedShoppingMemo.isChecked());
                showAllEntries();
            }
        });

    }

    private AlertDialog createEditShoppingMemoDialog(final ShoppingMemo shoppingMemo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_shopping_memo, null);

        final EditText txtNewQuantity = (EditText) dialogView.findViewById(R.id.txtnewQuantity);
        txtNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));

        final EditText txtNewProduct = (EditText) dialogView.findViewById(R.id.txtnewProduct);
        txtNewProduct.setText(shoppingMemo.getProduct());

        builder.setView(dialogView);
        builder.setTitle(R.string.dialog_title);
        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String quantityString = txtNewQuantity.getText().toString();
                String product = txtNewProduct.getText().toString();

                if ((TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(product))) {
                    Log.d(LOG_TAG, "Ein Eintrag enthält keinen Text. Daher Abbruch der Änderung.");
                    return;
                }

                int quantity = Integer.parseInt(quantityString);

                //An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(shoppingMemo.getId(), product, quantity, false);

                Log.d(LOG_TAG, "Alter Eintrag - ID: " + shoppingMemo.getId() + " Inhalt: " + shoppingMemo.toString());
                Log.d(LOG_TAG, "Neuer Eintrag - ID : " + updatedShoppingMemo.getId() + " Inhalt: " + updatedShoppingMemo.toString());

                showAllEntries();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }
}
