package kanjiCrush.game;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;

public class GamePlayActivity extends AppCompatActivity {
    public static final String msg = "GamePlayActivity : ";
    private GridView mGridView;
    private Board mBoard;
    private static int mLevel;
    private static int textSizeSp = 35;
    private static float buttonSizeDp;
    private static final float horizontalSpacingDp = 10f;
    private static final float verticalSpacingDp = 1f;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(msg, "The onCreate() event");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

        // Loads game attributes from shared preferences
        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        mLevel = sharedPref.getInt("myLevel", 0);

        // Loads button size from default preferences
        SharedPreferences defSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        buttonSizeDp = Float.parseFloat(defSharedPref.getString("list_preference_button_size", "55"));
        textSizeSp = Integer.parseInt(defSharedPref.getString("list_preference_text_size", "35"));
        Log.d(msg, "buttonSizeDp: " + buttonSizeDp + ", textSizeSp: " + textSizeSp);

        // Loading board
        mBoard = new Board(getApplicationContext(), mLevel);
        mBoard.load(sharedPref, mLevel);
        mBoard.log();

        initLevelText();
        initNextLevelButton();
        initView();
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

    private void advanceLevel(){
        Log.d(msg, "advanceLevel() called");

        // increase level if below level 5, else reset to 0
        mLevel = (mLevel < 4) ? mLevel + 1 : 0;

        // re-instantiate the board with the new level
        mBoard = new Board(getApplicationContext(), mLevel);
        mBoard.log();

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
            button.setTextSize(textSizeSp);
            button.setOnClickListener(myOnClickListener(i));

            //Setting initial bg
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

    /** Listens for clicks on the buttons */
    private View.OnClickListener myOnClickListener(int button_id){
        return v -> {
            Log.d(msg, "onClick() called");
            if (mBoard.getStateAt(button_id) == 0) mBoard.setState(button_id, 1);
            else mBoard.setState(button_id, 0);

            mBoard.log();

            if(mBoard.twoChipsAreSelected()) swapChips();
            else updateButtonBackgrounds();
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
        nextLevelButton.setOnClickListener(v -> advanceLevel());
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
        mGridView.setColumnWidth(Utils.dpToPx(buttonSizeDp, getApplicationContext()));
        mGridView.setHorizontalSpacing(Utils.dpToPx(horizontalSpacingDp, getApplicationContext()));
        mGridView.setVerticalSpacing(Utils.dpToPx(verticalSpacingDp, getApplicationContext()));
    }

    /** Sets dimensions with centered spacing and calls display()*/
    private void setDimensions() {
        Log.d(msg, "setDimensions() called");
        ViewTreeObserver vto = mGridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(msg, "onGlobalLayout called");
                mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int displayWidth = mGridView.getMeasuredWidth();
                int displayHeight = mGridView.getMeasuredHeight();

                int buttonSizePx = Utils.dpToPx(buttonSizeDp, getApplicationContext());
                int horizontalSpacingPx = Utils.dpToPx(horizontalSpacingDp, getApplicationContext());
                int verticalAdjustmentPx = Utils.dpToPx(30f, getApplicationContext());

                int horizontalPadding = (displayWidth - mBoard.getColumns()*buttonSizePx - horizontalSpacingPx*(mBoard.getColumns() - 1))/2;
                int verticalPadding = (mLevel > 0)
                        ? (displayHeight - (mBoard.getRows() + 1)*buttonSizePx)/2 - verticalAdjustmentPx
                        : (displayHeight - mBoard.getRows()*buttonSizePx)/2 - verticalAdjustmentPx;

                mGridView.setPadding(horizontalPadding, verticalPadding,0,0);
                display(getApplicationContext(), buttonSizePx, buttonSizePx);
            }
        });
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
                        swapAnimation(i,j);
                    }
                }
            }
        }
    }

    private void swapAnimation(int i, int j){
        Button button1 = findViewById(i);
        Button button2 = findViewById(j);
        final Animation animation1 = new TranslateAnimation(0, (button2.getX() - button1.getX()), 0, (button2.getY() - button1.getY()));
        final Animation animation2 = new TranslateAnimation(0, (button1.getX() - button2.getX()), 0, (button1.getY() - button2.getY()));
        animation1.setDuration(350);
        animation2.setDuration(350);
        button1.startAnimation(animation1);
        button2.startAnimation(animation2);

        // Setting up delayed run function to be called right when animations are about to end
        mGridView.postOnAnimationDelayed(() -> {
            // Prepare text before cancellation of animation
            button1.setText(mBoard.getKanjiAt(i));
            button2.setText(mBoard.getKanjiAt(j));

            // update board and backgrounds of buttons before cancellation of animation
            updateBoard();

            // Cancel animation to avoid potential flickering at the end of animation
            animation1.cancel();
            animation2.cancel();
        }, 350);
    }

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
