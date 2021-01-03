package com.example.kanjicrush2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;


public class GamePlayActivity extends AppCompatActivity {
    public static final String msg = "GamePlayActivity : ";
    private GridView mGridView;
    private Board mBoard;
    private static int mLevel;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(msg, "The onCreate() event");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        mLevel = sharedPref.getInt("myLevel", 0);

        // Loading board
        mBoard = new Board(getApplicationContext(), mLevel);
        mBoard.load(sharedPref, mLevel);
        mBoard.log();

        // these depend on the current activity
        initLevelText(); // OK
        initNextLevelButton(); // OK

        initView(); // OK
        setDimensions();
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        Log.d(msg, "The onResume() event");
        super.onResume();
        updateBoard();
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        Log.d(msg, "The onStop() event");
        super.onStop();
        saveSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        Log.d(msg, "The onBackPressed() event");
        super.onBackPressed();
        Intent menuIntent = new Intent(GamePlayActivity.this, MenuActivity.class);
        startActivity(menuIntent);
    }

    /** Calls all the methods needed to initialize a new level */
    private void advanceLevel(){
        Log.d(msg, "advanceLevel() called");

        // increase level if below level 5, else reset to 0 and reset board
        mLevel = (mLevel < 4) ? mLevel + 1 : 0;

        // re-instantiate the board
        mBoard = new Board(getApplicationContext(), mLevel);
        mBoard.log();

        // Update the level
        initView();
        initLevelText();
        initNextLevelButton();
        setDimensions();
    }

    private void display(Context context, int columnWidth, int columnHeight) {
        Log.d(msg,"display() called");
        CustomAdapter adapter = new CustomAdapter(getButtons(context), columnWidth, columnHeight);
        mGridView.setAdapter(adapter);
    }

    private ArrayList<Button> getButtons(Context context) {
        Log.d(msg,"getButtons() called");
        ArrayList<Button> buttons = new ArrayList<>();
        for (int i = 0; i < mBoard.getDimensions(); i++) {
            // Add extra row of empty invisible buttons for spacing purposes
            if (mBoard.getRows()/3 > 1) {
                if (i == mBoard.getDimensions()/2){
                    Log.d(msg,"i = " + i);
                    //Log.d(msg,"we are in the beginning of the middle row");
                    Button button= new Button(context);
                    button.setVisibility(Button.INVISIBLE);
                    button.setEnabled(false);
                    for(int j=0; j < mBoard.getColumns(); j++){
                        //Log.d(msg,"Adding button + " + j);
                        buttons.add(button);
                    }
                }
            }
            // Init button and set attributes
            final Button button= new Button(context);
            button.setId(i);
            button.setText(mBoard.getKanjiAt(i));
            button.setTypeface(ResourcesCompat.getFont(context, R.font.geneichikugomin));
            button.setTextColor(Color.parseColor("#FFFFFFFF"));
            button.setTextSize(35);
            button.setOnClickListener(myOnClickListener(i));

            //Setting initial bg in case of a loaded game
            if(mBoard.getStateAt(i) == 0) {
                button.setBackgroundResource(R.drawable.button_selector_default);
            }
            else if (mBoard.getStateAt(i) == 1) {
                button.setBackgroundResource(R.drawable.button_selector_selected);
            }
            else if (mBoard.getStateAt(i) == 2) {
                button.setBackgroundResource(R.drawable.button_selector_completed);
                button.setEnabled(false);
            }
            buttons.add(button);
        }
        return buttons;
    }

    /** Listens for clicks on the chips */
    private View.OnClickListener myOnClickListener(int button_id){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(msg, "onClick() called");
                if (mBoard.getStateAt(button_id) == 0) mBoard.setState(button_id, 1);
                else mBoard.setState(button_id, 0);

                mBoard.log();

                if(mBoard.twoChipsAreSelected()) swapChips();
                else updateButtonBackgrounds();
            }
        };
    }

    private void updateBoard(){
        Log.d(msg, "updateBoard() called");

        mBoard.updateStates();

        if(mBoard.boardIsComplete()){
            enableNextLevelButton();
        }

        updateButtonBackgrounds();
    }

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

    private void enableNextLevelButton() {
        Button nextLevelButton = findViewById(R.id.btnSubmit);
        if (mLevel >= 4) {
            nextLevelButton.setText(R.string.reset);
        }
        nextLevelButton.setEnabled(true);
    }

    private void initLevelText() {
        TextView levelTextView = findViewById(R.id.levelText);
        String levelAnnotation = "Level: " + (mLevel + 1) + "/5";
        levelTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
        levelTextView.setText(levelAnnotation);
    }

    private void initView() {
        Log.d(msg, "initView() called");
        mGridView = findViewById(R.id.gridView);
        mGridView.setNumColumns(mBoard.getColumns());
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
                int displayWidth = mGridView.getMeasuredWidth() - 100; // -100 to give extra side margins
                int displayHeight = mGridView.getMeasuredHeight();

                int statusbarHeight = getStatusBarHeight(getApplicationContext());
                int requiredHeight = displayHeight - statusbarHeight;

                int columnWidth = displayWidth / mBoard.getColumns();
                int columnHeight = (mLevel > 0) ? requiredHeight / (mBoard.getRows() + 1) : requiredHeight / mBoard.getRows();
                setPadding();

                Log.d(msg, "Column dimensions: " + (columnWidth) + "x" + (columnHeight));
                display(getApplicationContext(), columnWidth, columnHeight);
            }
        });
    }

    /** Sets extra padding for level 1 but not for other levels */
    private void setPadding() {
        if (mLevel == 0){
            // Converts 20 dip into its equivalent px and set padding
            float dip = 140f;
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            mGridView.setPadding(0, (int) px,0,0);
        } else {
            mGridView.setPadding(0, 0,0,0);
        }
    }

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

    private void saveSharedPreferences(){
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        mBoard.save(sharedPref, mLevel);
    }

    private void swapChips(){
        Log.d(msg, "swapChips() called");
        //Find first
        for(int i = 0; i < mBoard.getStateList().length; i++){
            if(mBoard.getStateAt(i) == 1){
                // Find second
                for(int j = i + 1; j < mBoard.getStateList().length; j++){
                    if(mBoard.getStateAt(j) == 1){
                        // Updating states and switching kanji
                        mBoard.setState(i,0);
                        mBoard.setState(j,0);
                        mBoard.switchKanji(i, j);
                        int k = i;
                        int l = j;

                        // Animation step
                        Button button1 = findViewById(i);
                        Button button2 = findViewById(j);
                        final Animation animation1 = new TranslateAnimation(0, (button2.getX() - button1.getX()), 0, (button2.getY() - button1.getY()));
                        final Animation animation2 = new TranslateAnimation(0, (button1.getX() - button2.getX()), 0, (button1.getY() - button2.getY()));
                        animation1.setDuration(350);
                        animation2.setDuration(350);
                        button1.startAnimation(animation1);
                        button2.startAnimation(animation2);

                        // Setting up delayed run function to be called right when animations are about to end
                        mGridView.postOnAnimationDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(msg, "run() called");

                                // Prepare text before cancellation of animation
                                button1.setText(mBoard.getKanjiAt(k));
                                button2.setText(mBoard.getKanjiAt(l));

                                // Prepare backgrounds of buttons before cancellation of animation
                                updateBoard();

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
        if(findViewById(0) != null){
            for (int i = 0; i < mBoard.getDimensions(); i++){
                Button uButton = findViewById(i);
                if(mBoard.getStateAt(i) == 0) {
                    uButton.setBackgroundResource(R.drawable.button_selector_default);
                }
                else if (mBoard.getStateAt(i) == 1) {
                    uButton.setBackgroundResource(R.drawable.button_selector_selected);
                }
                else if (mBoard.getStateAt(i) == 2) {
                    uButton.setBackgroundResource(R.drawable.button_selector_completed);
                    uButton.setEnabled(false);
                }
            }
        }
    }
}
