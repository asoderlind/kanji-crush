package com.example.kanjicrush2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {
    String msg = "Android :";
    private long pressedTime;
    private long pressedTimeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(msg,"onCreate() (MENU) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button startButton = findViewById(R.id.startGameButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(msg, "The onClick() (mainActivity) event");
                Intent mainIntent = new Intent(MenuActivity.this, GamePlayActivity.class);
                startActivity(mainIntent);
            }
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(MenuActivity.this)
                        .setTitle("Erasing local data").setMessage("Are you sure you want to erase all game progress?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSharedPreferences();
                            }
                        }).setNegativeButton("No", null).show();
            }
        });

    }

    public void deleteSharedPreferences(){
        Log.d(msg, "The deleteSharedPreferences() method");
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        Toast.makeText(getBaseContext(), "Data reset", Toast.LENGTH_SHORT).show();
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