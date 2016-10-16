package fr.rsommerard.devicedetection;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        final TextView orientation = (TextView) findViewById(R.id.orientation);
        final TextView resolution = (TextView) findViewById(R.id.resolution);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Display display = getWindowManager().getDefaultDisplay();

                orientation.setText("" + display.getRotation());

                Point xy = new Point();
                display.getSize(xy);
                resolution.setText("{ x: " + xy.x + ", y: " + xy.y + " }");
            }
        });
    }
}
