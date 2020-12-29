package com.example.kanjicrush2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Declare object for the gridView
    private static GridView mGridView;
    private static com.example.kanjicrush2.CSVFile mCSVFile;
    public static final String msg = "Android : ";

    // Game-specific variables
    private static final int COLUMNS = 3;
    private static final int DIMENSIONS = COLUMNS * COLUMNS;

    // non-constant variables
    private static int mColumnWidth, mColumnHeight;
    private static int level;
    private static int mNumWords = 3;

    // Declaration of list of tiles
    private static String[] chipList;
    private static String[] mJukuList;
    private static String[] mKanjiList;
    private static String[] jukuList = {"合", "気", "道", "愛", "好", "家", "合", "言", "葉"};

    /** Makes an ArrayList of button-objects from context and sends to our Adapter */
    private static void display(Context context) {
        // This sends the view to the adapter which actually puts it on the screen
        mGridView.setAdapter(new com.example.kanjicrush2.CustomAdapter(getButtons(context), mColumnWidth, mColumnHeight));
    }

    /** Make and assign the attributes for the buttons, finally return button list */
    private static ArrayList<Button> getButtons(Context context) {
        ArrayList<Button> buttons = new ArrayList<>();
        Button button; // Declare button
        for (int i = 0; i < chipList.length; i++) {
            button = new Button(context);
            button.setId(i+1);
            String button_text = jukuList[i];
            String number = String.valueOf(i);
            button.setText(button_text);
            button.setTextSize(40);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(msg, "Click registered @ num " + number);
                    Toast.makeText(context, "This is " + number, Toast.LENGTH_SHORT).show();
                }
            });
            buttons.add(button);
        }
        return buttons;
    }

    /** Returns the list of the juku */
    private String[] getJukuList(int numWords) {
        InputStream inputStream = getResources().openRawResource(R.raw.jukugo);
        mCSVFile = new com.example.kanjicrush2.CSVFile(inputStream);

        // Get the list of words as string array
        List myList = mCSVFile.read();
        String[] myStringList = (String[]) myList.get(0);

        // Get 3 random words
        String[] mRandomList = new String[numWords];
        Random random = new Random();
        for (int i=0; i < numWords; i++) {
            mRandomList[i] = myStringList[random.nextInt(myStringList.length)];
            // TODO: check for duplicates in mRandomList when adding next random word
        }
        return mRandomList;
    }

    // TODO: make getKanjiList more elegant
    /** Breaks up the 3-char words into a list of single-char strings */
    private String[] getKanjiList(String[] jukuList) {
        mKanjiList = new String[jukuList.length*3];
        for (int i = 0; i < mKanjiList.length; i+=3) {
            for(int j = 0; j < 3; j++) {
                mKanjiList[i+j] = String.valueOf(jukuList[(i/3)%3].charAt(j));
            }
        }
        return mKanjiList;
    }

    /** Returns the height of the status bar */
    private int getStatusBarHeight(Context context){
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height",
                "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /** Assign the kanjiList and chipList*/
    private void initLists() {
        mJukuList = getJukuList(mNumWords);
        for( int i=0; i < mJukuList.length; i++){
            Log.d(msg, "Juku " + String.valueOf(i) + " is: " + mJukuList[i]);
        }

        mKanjiList = getKanjiList(mJukuList);
        for( int i=0; i < mKanjiList.length; i++){
            Log.d(msg, "Kanji " + String.valueOf(i) + " is: " + mKanjiList[i]);
        }

        chipList = new String[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            chipList[i] = String.valueOf(i);
        }
    }

    /** Assign the main GridView */
    private void initView() {
        // Init view
        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setNumColumns(COLUMNS);
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(msg, "The onCreate() event");

        initLists();
        initView();
        setDimensions(); // set mColumnWidth and mColumnHeight
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(msg, "The onResume() event");
    }

    /** Gives the dimensions we need for the buttons */
    private void setDimensions() {
        ViewTreeObserver vto = mGridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int displayWidth = mGridView.getMeasuredWidth();
                int displayHeight = mGridView.getMeasuredHeight();

                int statusbarHeight = getStatusBarHeight(getApplicationContext());
                int requiredHeight = displayHeight - statusbarHeight;

                mColumnWidth = displayWidth / COLUMNS;
                mColumnHeight = requiredHeight / COLUMNS;

                display(getApplicationContext());
            }
        });
    }
}
