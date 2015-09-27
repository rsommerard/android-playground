package rsommerard.courtcounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int scoreTeamA = 0;
    private int scoreTeamB = 0;

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

    private void displayForTeamA(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_a_score);
        scoreView.setText(String.valueOf(score));
    }

    private void displayForTeamB(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_b_score);
        scoreView.setText(String.valueOf(score));
    }

    public void addThreeForTeamA(View view) {
        this.scoreTeamA += 3;
        this.displayForTeamA(this.scoreTeamA);
    }

    public void addThreeForTeamB(View view) {
        this.scoreTeamB += 3;
        this.displayForTeamB(this.scoreTeamB);
    }

    public void addTwoForTeamA(View view) {
        this.scoreTeamA += 2;
        this.displayForTeamA(this.scoreTeamA);
    }

    public void addTwoForTeamB(View view) {
        this.scoreTeamB += 2;
        this.displayForTeamB(this.scoreTeamB);
    }

    public void addOneForTeamA(View view) {
        this.scoreTeamA += 1;
        this.displayForTeamA(this.scoreTeamA);
    }

    public void addOneForTeamB(View view) {
        this.scoreTeamB += 1;
        this.displayForTeamB(this.scoreTeamB);
    }

    public void resetScore(View view) {
        this.scoreTeamA = 0;
        this.scoreTeamB = 0;
        this.displayForTeamA(this.scoreTeamA);
        this.displayForTeamB(this.scoreTeamB);
    }
}
