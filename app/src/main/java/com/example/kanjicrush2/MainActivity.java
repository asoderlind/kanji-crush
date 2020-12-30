package com.example.kanjicrush2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private static final int[][] mLevelDimensions = {{3,4},{3,6},{6,4},{6,5},{6,6}};
    private static int ROWS;
    private static int COLUMNS;
    private static int DIMENSIONS;

    // non-constant variables
    private static int mColumnWidth, mColumnHeight;
    private static int mLevel = 0;
    private static int mNumWords = 4 + mLevel*2;

    // Declaration the local lists
    private static String[] mJukuList;
    private static String[] mKanjiList;
    private static int[] mButtonStateList;

    /** Checks for completed lines and changes status */
    private void check_for_completed_line(){
        Log.d(msg,"Checking possible candidates:");
        for (int i = 0; i < mKanjiList.length/3; i++){
            String candidate;
            if (i >= COLUMNS){
                Log.d(msg,"Second row");
                candidate = mKanjiList[i+(2*COLUMNS)] + mKanjiList[i+(3*COLUMNS)] + mKanjiList[i+(4*COLUMNS)];
            } else {
                Log.d(msg,"First row");
                candidate = mKanjiList[i] + mKanjiList[i+COLUMNS] + mKanjiList[i+2*COLUMNS];
            }
            Log.d(msg,candidate);
            for(int j = 0; j < mJukuList.length; j++){
                if(candidate.equals(mJukuList[j])){
                    if (i >= COLUMNS){
                        mButtonStateList[i+(2*COLUMNS)] = 2;
                        mButtonStateList[i+(3*COLUMNS)] = 2;
                        mButtonStateList[i+(4*COLUMNS)] = 2;
                    } else {
                        mButtonStateList[i] = 2;
                        mButtonStateList[i+COLUMNS] = 2;
                        mButtonStateList[i+2*COLUMNS] = 2;
                    }

                }
            }
        }
    }

    /** Makes an ArrayList of button-objects from context and sends to our Adapter */
    private void display(Context context) {
        Log.d(msg,"display() called");
        // This sends the view to the adapter which actually puts it on the screen
        mGridView.setAdapter(new com.example.kanjicrush2.CustomAdapter(getButtons(context), mColumnWidth, mColumnHeight));
    }

    /** Initialize the buttons and put them in a list, which is returned */
    private ArrayList<Button> getButtons(Context context) {
        Log.d(msg,"getButtons() called");
        ArrayList<Button> buttons = new ArrayList<>();
        Button button;
        for (int i = 0; i < DIMENSIONS; i++) {
            // Set id
            int j = i;
            button = new Button(context);
            button.setId(j);

            // Set default text
            String button_text = mKanjiList[j];
            button.setText(button_text);
            button.setTextSize(40);

            // set default bg
            button.setBackgroundResource(R.drawable.chip);

            // TODO: make the listener a separate class
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(msg, "Click registered @ num " + String.valueOf(j));

                    // Toggle the state between selected (1) and deselected (0)
                    if (mButtonStateList[j] == 0) {
                        mButtonStateList[j] = 1;
                    } else {
                        mButtonStateList[j] = 0;
                    }

                    // Refresh buttons
                    updateButtons();
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
            for(int j = 0; j < jukuList[0].length(); j++) {
                mKanjiList[i+j] = String.valueOf(jukuList[i/jukuList[0].length()].charAt(j));
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

        // Set all states to zero at the start
        mButtonStateList = new int[DIMENSIONS];
        for( int i=0; i < DIMENSIONS; i++) {
            mButtonStateList[i] = 0;
        }

        scramble_kanjiList();
    }

    /** Assign the main GridView */
    private void initView() {
        // Init view
        //TODO: Cast as our custom gridview
        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setNumColumns(COLUMNS);
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(msg, "The onCreate() event");

        setRowsAndColumns();
        initLists();
        initView();
        setDimensions(); // set COLUMNS, ROWS, DIMENSIONS, mColumnWidth and mColumnHeight

    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(msg, "The onDestroy() event");
    }


    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(msg, "The onPause() event");
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(msg, "The onResume() event");
    }


    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(msg, "The onStop() event");
    }

    /** Scramble the list */
    private void scramble_kanjiList() {
        int index;
        String temp;
        Random random = new Random();

        for (int i = mKanjiList.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            temp = mKanjiList[index];
            mKanjiList[index] = mKanjiList[i];
            mKanjiList[i] = temp;
        }
    }

    /** Gives the dimensions we need for the buttons */
    private void setDimensions() {
        ViewTreeObserver vto = mGridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(msg, "onGlobalLayout called");
                mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int displayWidth = mGridView.getMeasuredWidth();
                int displayHeight = mGridView.getMeasuredHeight();

                int statusbarHeight = getStatusBarHeight(getApplicationContext());
                int requiredHeight = displayHeight - statusbarHeight;

                mColumnWidth = displayWidth / COLUMNS;
                mColumnHeight = requiredHeight / (ROWS + 1); // +1 for a bit of extra space
                Log.d(msg, "Column dimensions: " + String.valueOf(mColumnWidth) + "x" + String.valueOf(mColumnHeight));

                display(getApplicationContext());
            }
        });
    }

    private void setRowsAndColumns() {
        ROWS = mLevelDimensions[mLevel][0];
        COLUMNS = mLevelDimensions[mLevel][1];
        DIMENSIONS = ROWS*COLUMNS;
    }

    private void swap_chips(){
        Log.d(msg, "Swapping chips");
        //Find first
        for(int i = 0; i < mButtonStateList.length; i++){
            if(mButtonStateList[i] == 1){
                String tmp_str = mKanjiList[i];
                // Find second
                for(int j = i + 1; j < mButtonStateList.length; j++){
                    if(mButtonStateList[j] == 1){
                        mKanjiList[i] = mKanjiList[j];
                        mKanjiList[j] = tmp_str;
                        mButtonStateList[i] = 0;
                        mButtonStateList[j] = 0;
                    }
                }
            }
        }
    }

    private boolean two_chips_selected(int[] statusList){
        int count = 0;
        for(int i = 0; i < statusList.length; i++){
            if (statusList[i] == 1){
                count++;
                if (count > 1) return true;
            }
        }
        return false;
    }

    /** This function finds the buttons by their id and updates the style.
     * Also checks for board conditions and updates accordingly. */
    private void updateButtons(){
        Log.d(msg, "updateButtons() called");

        // Check for swap and completed line conditions
        if(two_chips_selected(mButtonStateList)) {
            swap_chips();
            check_for_completed_line();
        }

        // Check state and update background resource
        for (int i = 0; i < DIMENSIONS; i++){
            Button uButton = (Button) findViewById(i); //local var for updating
            if(mButtonStateList[i] == 0) {
                uButton.setBackgroundResource(R.drawable.chip);
            }
            else if (mButtonStateList[i] == 1) {
                uButton.setBackgroundResource(R.drawable.chip_green);
            }
            else if (mButtonStateList[i] == 2) {
                uButton.setBackgroundResource(R.drawable.chip_cross);
                uButton.setEnabled(false);
            }

            // Set text
            String button_text = mKanjiList[i];
            uButton.setText(button_text);
        }
    }
}
