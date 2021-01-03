package com.example.kanjicrush2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.example.kanjicrush2.MainActivity.msg;

public class MenuActivity extends AppCompatActivity {
    String msg = "Android :";
    private long pressedTime;
    private long pressedTimeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(msg,"onCreate() (MENU) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button startButton = (Button)findViewById(R.id.startGameButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(msg, "The onClick() (mainActivity) event");
                Intent mainIntent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        Button settingsButton = (Button)findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(msg, "The onClick() (cleardata) event");
                if (pressedTimeData + 2000 > System.currentTimeMillis()) {
                    Toast.makeText(getBaseContext(), "Progress reset", Toast.LENGTH_SHORT).show();
                    deleteSharedPreferences();
                } else {
                    Toast.makeText(getBaseContext(), "Press again to reset progress", Toast.LENGTH_SHORT).show();
                }
                pressedTimeData = System.currentTimeMillis();
            }
        });

    }

    public void deleteSharedPreferences(){
        Log.d(msg, "The deleteSharedPreferences() method");
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        Log.d(msg, "The onBackPressed() (MENU) event");
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            Log.d(msg, "The finishAffinity() event");
            this.finishAffinity();
        } else {
            Toast.makeText(getBaseContext(), "Press back again exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
}