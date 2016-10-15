package fr.rsommerard.fragmentexploration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentLayout, MainFragment.newInstance("Je suis un fragment."))
                .commit();
    }

    @Override
    public void onTitleClicked() {
        Toast.makeText(this, "Tu as clické !", Toast.LENGTH_SHORT).show();
    }
}
