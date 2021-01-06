package kanjiCrush.game;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    String msg = "Android :";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}