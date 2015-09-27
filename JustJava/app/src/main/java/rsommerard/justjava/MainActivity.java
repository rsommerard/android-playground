package rsommerard.justjava;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private int quantity = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submitOrder(View view) {
        CheckBox whippedCreamChbx = (CheckBox) findViewById(R.id.whipped_cream_check_box);
        CheckBox chocolateChbx = (CheckBox) findViewById(R.id.chocolate_check_box);

        boolean hasWhippedCream = whippedCreamChbx.isChecked();
        boolean hasChocolate = chocolateChbx.isChecked();

        EditText nameEditText = (EditText) findViewById(R.id.name_edit_text);
        String name = nameEditText.getText().toString();

        int price = this.calculatePrice(hasWhippedCream, hasChocolate);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:mail@mail.mail"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "JustJava order for " + name);
        intent.putExtra(Intent.EXTRA_TEXT, this.createOrderSummary(price, name, hasWhippedCream, hasChocolate));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private String createOrderSummary(int price, String name, boolean hasWhippedCream, boolean hasChocolate) {
        String str = "Name: " + name;
        str += "\nAdd whipped cream? " + hasWhippedCream;
        str += "\nAdd chocolate? " + hasChocolate;
        str += "\nQuantity: " + this.quantity;
        str += "\nTotal: $" + price;
        str += "\n" + getString(R.string.thank_you, name);

        return str;
    }

    private int calculatePrice(boolean hasWhippedCream, boolean hasChocolate) {
        int basePrice = 5;

        if (hasWhippedCream) {
            basePrice += 1;
        }

        if (hasChocolate) {
            basePrice += 2;
        }

        return this.quantity * basePrice;
    }

    private void displayQuantity(int numberOfCoffees) {
        TextView quantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        quantityTextView.setText("" + numberOfCoffees);
    }

    public void increment(View view) {
        if(this.quantity == 100) {
            Toast toast = Toast.makeText(this, "You cannot have more than 100 cups", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        this.quantity++;
        displayQuantity(this.quantity);
    }

    public void decrement(View view) {
        if(this.quantity == 1)
            return;

        this.quantity--;
        displayQuantity(this.quantity);
    }

}
