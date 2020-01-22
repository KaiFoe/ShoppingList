package foerstermann.kai.shoppinglist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    MainActivityListener mainActivityListener;
    EditText txtquantity, txtproduct;
    Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtquantity = findViewById(R.id.txtQuantity);
        txtproduct = findViewById(R.id.txtProduct);

        btnAddProduct = findViewById(R.id.btnAddProduct);

        mainActivityListener = new MainActivityListener(this);

        btnAddProduct.setOnClickListener(mainActivityListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActivityListener.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainActivityListener.onPause();
    }
}
