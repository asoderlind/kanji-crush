package com.example.kanjicrush2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GridView mGridView;
    public static final String msg = "Android : ";

    // Game-specific variables
    private static final int[][] mLevelDimensions = {{3,4},{3,6},{6,4},{6,5},{6,6}};
    private static final int mNumWordsStart = 4;

    // Board dimensions
    private static int ROWS, COLUMNS, DIMENSIONS;
    private static int mColumnWidth, mColumnHeight;

    // level-specific variables
    private static int mLevel = 0;
    private static String mJukus;
    private static String mKanjis;
    private static int[] mButtonStateList;

    private static boolean mSwapAnimationRunning;

    /** Calls all the methods needed to initialize a new level */
    private void advanceLevel(){
        Log.d(msg, "advanceLevel() called");

        // increase level if below level 5, else reset to 0
        mLevel = (mLevel < 4) ? mLevel + 1 : 0;

        // Initialize new variables
        mJukus = getJukus(4 + mLevel*2);
        mKanjis = getScrambled(mJukus);
        ROWS = mLevelDimensions[mLevel][0];
        COLUMNS = mLevelDimensions[mLevel][1];
        DIMENSIONS = ROWS*COLUMNS;
        mButtonStateList = new int[DIMENSIONS];
        Log.d(msg, "New Jukus: " + mJukus);
        Log.d(msg, "New scrambled kanjis: " + mKanjis);

        // Update the level
        initView(); // New number of columns
        initLevelText(); // New level text
        initNextLevelButton();
        setDimensions();
    }

    /** Checks if a line of 3 kanjis is completed and if so calls checkCompleteBoard,
     * otherwise calls updateButtonBackgrounds in order to deselect buttons. */
    private void checkForCompletedJukugo(){
        Log.d(msg,"checkForCompletedJukugo() called");
        boolean lineCompleted = false;
        for (int i = 0; i < mKanjis.length()/3; i++){
            char char1;
            char char2;
            char char3;
            // If (i >= COLUMNS) it is the first row, otherwise it is the second row of we are comparing on
            char1 = (i >= COLUMNS) ? mKanjis.charAt(i + (2 * COLUMNS)) : mKanjis.charAt(i);
            char2 = (i >= COLUMNS) ? mKanjis.charAt(i + (3 * COLUMNS)) : mKanjis.charAt(i + COLUMNS);
            char3 = (i >= COLUMNS) ? mKanjis.charAt(i + (4 * COLUMNS)) : mKanjis.charAt(i + 2 * COLUMNS);
            String candidate = String.valueOf(char1) + char2 + char3;
            //Log.d(msg,candidate);

            for (int j=0; j < mJukus.length(); j += 3) {
                String juku = mJukus.substring(j, j + 3);
                if (candidate.equals(juku)) {
                    lineCompleted = true;
                    if (i >= COLUMNS) {
                        mButtonStateList[i + (2 * COLUMNS)] = 2;
                        mButtonStateList[i + (3 * COLUMNS)] = 2;
                        mButtonStateList[i + (4 * COLUMNS)] = 2;
                    } else {
                        mButtonStateList[i] = 2;
                        mButtonStateList[i + COLUMNS] = 2;
                        mButtonStateList[i + 2 * COLUMNS] = 2;
                    }
                    Log.d(msg,"Line completed, calling checkForCompletedBoard");
                    checkForCompleteBoard();

                }
            }
        }
        if(!lineCompleted){
            Log.d(msg,"no line completed, deselect buttons, calling UBG");
            updateButtonBackgrounds();
        }
    }

    /** Checks if all chips are completed, in that case advance level, otherwise
     * simply update the newly completed line by calling updateButtonBg function */
    private void checkForCompleteBoard() {
        Log.d(msg,"checkForCompletedBoard() called");
        int numCompleted = 0;

        for (int i = 0; i < DIMENSIONS; i++) {
            if (mButtonStateList[i] == 2) numCompleted++;
        }

        if (numCompleted == DIMENSIONS && !mSwapAnimationRunning){
            Button nextLevelButton = findViewById(R.id.btnSubmit);
            if (mLevel >= 5) {
                nextLevelButton.setText(R.string.reset);
            }
            updateButtonBackgrounds();
            nextLevelButton.setEnabled(true);
        } else {
            Log.d(msg,"no clear, update finished line, calling UBG");
            updateButtonBackgrounds(); // update finished line
        }
    }

    /** Checks for 2 or more selected chips, in that case swap, otherwise
     * select/deselect the button by calling updateButtonBackgrounds */
    private void checkForSelectedChips() {
        Log.d(msg, "checkForSelectedChips() called");
        int count = 0;
        for (int state : mButtonStateList) {
            if (state == 1) {
                count++;
                if (count > 1) {
                    swapChips();
                }
            }
        }

        if (count <= 1) {
            Log.d(msg,"no swap, select or deselect single button, calling UBG");
            updateButtonBackgrounds(); // select or deselect single button
        }
    }

    /** Init adapter with default buttons and set to mGridView. */
    private void display(Context context) {
        Log.d(msg,"display() called");
        CustomAdapter adapter = new CustomAdapter(getButtons(context), mColumnWidth, mColumnHeight);
        mGridView.setAdapter(adapter);
    }

    /** Initialize the buttons and set default attributes */
    private ArrayList<Button> getButtons(Context context) {
        Log.d(msg,"getButtons() called");
        ArrayList<Button> buttons = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS; i++) {
            // Add extra row of empty invisible buttons for spacing purposes
            if (ROWS/3 > 1) {
                if (i == DIMENSIONS/2){
                    Log.d(msg,"i = " + i);
                    //Log.d(msg,"we are in the beginning of the middle row");
                    Button button= new Button(context);
                    button.setVisibility(Button.INVISIBLE);
                    button.setEnabled(false);
                    for(int j=0; j < COLUMNS; j++){
                        //Log.d(msg,"Adding button + " + j);
                        buttons.add(button);
                    }
                }
            }

            // Init button and set attributes
            final Button button= new Button(context);
            button.setId(i);
            button.setText(String.valueOf(mKanjis.charAt(button.getId())));
            button.setTextColor(Color.parseColor("#FFFFFFFF"));
            button.setTextSize(35);
            button.setOnClickListener(myOnClickListener(i));

            //Setting initial bg in case of a loaded game
            if(mButtonStateList[i] == 0) {
                button.setBackgroundResource(R.drawable.button_selector_default);
            }
            else if (mButtonStateList[i] == 1) {
                button.setBackgroundResource(R.drawable.button_selector_selected);
            }
            else if (mButtonStateList[i] == 2) {
                button.setBackgroundResource(R.drawable.button_selector_completed);
                button.setEnabled(false);
            }

            buttons.add(button);
        }
        return buttons;
    }

    /** Returns the list of the juku */
    private String getJukus(int numWords) {
        InputStream inputStream = getResources().openRawResource(R.raw.jukugo);
        CSVFile mCSVFile = new CSVFile(inputStream);

        // Get the list of words as string array
        List myList = mCSVFile.read();
        String[] myStringList = (String[]) myList.get(0);

        // Get 3 random words
        StringBuilder mRandomJukus = new StringBuilder();
        Random random = new Random();
        for (int i=0; i < numWords; i++) {
            mRandomJukus.append(myStringList[random.nextInt(myStringList.length)]);
            /* TODO: check for duplicates in mRandomJukus when adding next random word */
        }
        return mRandomJukus.toString();
    }

    /** Scramble the string */
    public static String getScrambled(String s) {
        String[] scram = s.split("");
        List<String> letters = Arrays.asList(scram);
        Collections.shuffle(letters);
        StringBuilder sb = new StringBuilder(s.length());
        for (String c : letters) {
            sb.append(c);
        }
        return sb.toString();
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

    /** Button used mainly for debugging as of now */
    private void initNextLevelButton() {
        Button nextLevelButton;
        nextLevelButton = findViewById(R.id.btnSubmit);
        nextLevelButton.setText(R.string.lower_button_text);
        nextLevelButton.setEnabled(false);
        nextLevelButton.setBackgroundResource(R.drawable.button_selector_next);
        nextLevelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                advanceLevel();
            }
        });
    }

    /** Add text to the textView at the top */
    private void initLevelText() {
        TextView levelTextView = findViewById(R.id.levelText);
        String levelAnnotation = "Level: " + (mLevel + 1) + "/5";
        levelTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
        levelTextView.setText(levelAnnotation);
    }

    /** Assign the main GridView */
    private void initView() {
        Log.d(msg, "initView() called");
        mGridView = findViewById(R.id.gridView);
        mGridView.setNumColumns(COLUMNS);
    }

    /** Assign loaded data and return true if loaded successfully */
    private boolean loadSharedPreferences(){
        Log.d(msg, "loadSharedPreferences() called");
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        mLevel = sharedPref.getInt("myLevel", 0);
        mJukus = sharedPref.getString("myJukus", null);
        mKanjis = sharedPref.getString("myKanjis", null);
        String myStates = sharedPref.getString("myStates", null);
        ROWS = mLevelDimensions[mLevel][0];
        COLUMNS = mLevelDimensions[mLevel][1];
        DIMENSIONS = ROWS*COLUMNS;

        Log.d(msg, "Loaded myLevel: " + mLevel);
        Log.d(msg, "Loaded myJukus: " + mJukus);
        Log.d(msg, "Loaded myKanjis: " + mKanjis);
        Log.d(msg, "Loaded myStates:" + myStates);

        if (myStates != null){
            mButtonStateList = new int[myStates.length()];
            for(int i = 0; i < myStates.length(); i++) {
                mButtonStateList[i] = Character.getNumericValue(myStates.charAt(i));
            }
        }
        return ((mJukus != null) && (mKanjis != null) && (myStates != null));
    }

    /** Print the state list to log*/
    private void logStateList() {
        StringBuilder stateStr = new StringBuilder();
        for(int s : mButtonStateList){
            stateStr.append(s);
        }
        Log.d(msg, stateStr.toString());
    }

    /** Listener that is called when the animation is completely finished */
    private Animation.AnimationListener myAnimationListener(){
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // Calling for a second time, after animation has ended, to maybe load the next level
                Log.d(msg, "Calling checkForCompletedLine() from onAnimationEnd()");
                mSwapAnimationRunning = false;
                checkForCompletedJukugo();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    /** Listens for clicks on the chips */
    private View.OnClickListener myOnClickListener(int button_id){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(msg, "onClick() called @ id " + button_id);
                if (mButtonStateList[button_id] == 0) mButtonStateList[button_id] = 1;
                else mButtonStateList[button_id] = 0;
                logStateList();
                checkForSelectedChips();
            }
        };
    }

    @Override
    public void onBackPressed() {
        Log.d(msg, "The onBackPressed() event");
        super.onBackPressed();
        Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(menuIntent);
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(msg, "The onCreate() event");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!loadSharedPreferences()){
            mJukus = getJukus(mNumWordsStart);
            mKanjis = getScrambled(mJukus);
            ROWS = mLevelDimensions[mLevel][0];
            COLUMNS = mLevelDimensions[mLevel][1];
            DIMENSIONS = ROWS*COLUMNS;
            mButtonStateList = new int[DIMENSIONS];
            Log.d(msg, "New Jukus: " + mJukus);
            Log.d(msg, "New Scrambled kanjis: " + mKanjis);
            logStateList();
        }
        initLevelText();
        initNextLevelButton();
        initView();
        setDimensions();
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
        if(mButtonStateList != null){
            checkForCompleteBoard();
        }
    }

    /** Called when the activity is about to become visible. */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(msg, "The onStart() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        saveSharedPreferences();
        Log.d(msg, "The onStop() event");
    }

    private String replaceCharAt(String s, Character c, int index){
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    private void saveSharedPreferences(){
        Log.d(msg, "The saveSharedPreferences() method");
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Add the variables
        editor.putInt("myLevel", mLevel);
        editor.putString("myJukus", mJukus);
        editor.putString("myKanjis", mKanjis);
        StringBuilder myStates = new StringBuilder();
        for (int value : mButtonStateList) { myStates.append(value); }
        editor.putString("myStates", myStates.toString());

        /*Log.d(msg, "The myLevel variable is: " + mLevel);
        Log.d(msg, "The mJukus variable is: " + mJukus);
        Log.d(msg, "The mKanjis variable is: " + mKanjis);
        Log.d(msg, "The myStates variable is: " + myStates);*/

        editor.apply();
    }

    /** Sets dimensions buttons and calls display()*/
    private void setDimensions() {
        Log.d(msg, "setDimensions() called");
        ViewTreeObserver vto = mGridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(msg, "onGlobalLayout called");
                mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int displayWidth = mGridView.getMeasuredWidth() - 100; // -140 to account for style margins
                int displayHeight = mGridView.getMeasuredHeight();
                int statusbarHeight = getStatusBarHeight(getApplicationContext());
                int requiredHeight = displayHeight - statusbarHeight;
                mColumnWidth = displayWidth / COLUMNS;
                mColumnHeight = (mLevel > 1) ? requiredHeight / (ROWS + 1) : requiredHeight / ROWS;

                Log.d(msg, "Column dimensions: " + (mColumnWidth) + "x" + (mColumnHeight));
                display(getApplicationContext());
            }
        });
    }

    private void swapChips(){
        Log.d(msg, "swapChips() called");
        //Find first
        for(int i = 0; i < mButtonStateList.length; i++){
            if(mButtonStateList[i] == 1){
                Character tmp_char_i = mKanjis.charAt(i);
                // Find second
                for(int j = i + 1; j < mButtonStateList.length; j++){
                    if(mButtonStateList[j] == 1){
                        mButtonStateList[i] = 0;
                        mButtonStateList[j] = 0;

                        //OLD: mKanjiList[i] = mKanjiList[j];
                        mKanjis = replaceCharAt(mKanjis, mKanjis.charAt(j), i);
                        //OLD: mKanjiList[j] = tmp_str;
                        mKanjis = replaceCharAt(mKanjis, tmp_char_i, j);

                        int k = i; // Final integer variable for first kanji
                        int l = j; //Final integer variable for second kanji

                        Log.d(msg, "Starting animation");

                        // Animation step
                        Button button1 = (Button)findViewById(i);
                        Button button2 = (Button)findViewById(j);
                        final Animation animation1 = new TranslateAnimation(0, (button2.getX() - button1.getX()), 0, (button2.getY() - button1.getY()));
                        final Animation animation2 = new TranslateAnimation(0, (button1.getX() - button2.getX()), 0, (button1.getY() - button2.getY()));
                        animation1.setDuration(350);
                        animation2.setDuration(350);
                        button1.startAnimation(animation1);
                        button2.startAnimation(animation2);

                        mSwapAnimationRunning = true;

                        // We only need one animation listener
                        animation1.setAnimationListener(myAnimationListener());

                        // Setting up delayed function to be called right when animations are about to end
                        mGridView.postOnAnimationDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(msg, "run() called");

                                // Prepare text before cancellation of animation
                                button1.setText(String.valueOf(mKanjis.charAt(k)));
                                button2.setText(String.valueOf(mKanjis.charAt(l)));

                                // Calling for the first time, to prepare backgrounds of buttons before cancellation of animation
                                checkForCompletedJukugo();

                                // Cancel animation to avoid potential flickering @ end of animation
                                animation1.cancel();
                                animation2.cancel();
                            }
                        }, 350);
                    }
                }
            }
        }
    }

    // TODO: make more elegant solution to try-catch blocks
    private void updateButtonBackgrounds(){
        Log.d(msg, "updateButtonBackgrounds() called");
        // Check if the first button exists
        if(findViewById(0) != null){
            for (int i = 0; i < DIMENSIONS; i++){
                Button uButton = findViewById(i);
                if(mButtonStateList[i] == 0) {
                    uButton.setBackgroundResource(R.drawable.button_selector_default);
                }
                else if (mButtonStateList[i] == 1) {
                    uButton.setBackgroundResource(R.drawable.button_selector_selected);
                }
                else if (mButtonStateList[i] == 2) {
                    uButton.setBackgroundResource(R.drawable.button_selector_completed);
                    uButton.setEnabled(false);
                }
            }
        }
    }
}
