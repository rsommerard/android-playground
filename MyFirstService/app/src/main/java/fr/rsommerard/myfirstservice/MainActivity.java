package fr.rsommerard.myfirstservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
        startService(new Intent(this, MyService.class));

        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    }

    public void stopService(View view) {
        stopService(new Intent(this, MyService.class));

        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

}
