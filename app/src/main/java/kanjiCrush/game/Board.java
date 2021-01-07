package kanjiCrush.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;


public class Board {
    private final String msg = "Board :";
    private final int ROWS;
    private final int COLUMNS;
    private final int DIMENSIONS;
    private final int mJukuLength;
    private static final int[][] mLevelDimensionsSanji = {{3,4},{6,3},{6,4},{6,5},{6,6}};
    private static final int[][] mLevelDimensionsYoji = {{4,4},{8,3},{8,4},{8,5},{8,6}};
    private final Context mContext;
    private final String mJukus;
    private String mKanji;
    private final int[] mButtonStateList;

    /** Constructor that loads pre-existing board configs
     * If no config found, constructs new board config */
    public Board(Context c, int level, int difficulty, int jukuLength, SharedPreferences sharedPref){
        Log.d(msg, "Board constructor called");
        mContext = c;
        mJukuLength = jukuLength;
        mJukus = (sharedPref.getString("myJukus", null) != null) ? sharedPref.getString("myJukus", null) : genJukus(4 + level*2, difficulty);
        mKanji = (sharedPref.getString("myKanjis", null) != null) ? sharedPref.getString("myKanjis", null) : Utils.scrambledString(mJukus);
        ROWS = (mJukuLength == 3) ? mLevelDimensionsSanji[level][0] : mLevelDimensionsYoji[level][0];
        COLUMNS = (mJukuLength == 3) ? mLevelDimensionsSanji[level][1] : mLevelDimensionsYoji[level][1];
        DIMENSIONS = ROWS * COLUMNS;

        String myStates = sharedPref.getString("myStates", null);
        if (myStates != null){
            mButtonStateList = new int[myStates.length()];
            for(int i = 0; i < myStates.length(); i++) {
                mButtonStateList[i] = Character.getNumericValue(myStates.charAt(i));
            }
        } else mButtonStateList = new int[DIMENSIONS];
    }

    /** Constructor that creates new board config */
    public Board(Context c, int level, int difficulty, int jukuLength){
        Log.d(msg, "Board constructor called");
        mContext = c;
        mJukuLength = jukuLength;
        mJukus = genJukus(4 + level*2, difficulty);
        mKanji = Utils.scrambledString(mJukus);
        ROWS = (mJukuLength == 3) ? mLevelDimensionsSanji[level][0] : mLevelDimensionsYoji[level][0];
        COLUMNS = (mJukuLength == 3) ? mLevelDimensionsSanji[level][1] : mLevelDimensionsYoji[level][1];
        DIMENSIONS = ROWS * COLUMNS;
        mButtonStateList = new int[DIMENSIONS];
    }

    public String getKanjiAt(int i){
        return String.valueOf(mKanji.charAt(i));
    }

    public int[] getStateList(){
        return mButtonStateList;
    }

    public int getStateAt(int index){
        return mButtonStateList[index];
    }

    public int getRows(){
        return ROWS;
    }

    public int getColumns(){
        return COLUMNS;
    }

    public int getDimensions(){
        return DIMENSIONS;
    }

    public void setState(int index, int value){
        mButtonStateList[index] = value;
    }

    /** Returns continuous String with the words from the list*/
    private String genJukus(int numWords, int difficulty) {
        InputStream inputStream = (mJukuLength == 3) ? mContext.getResources().openRawResource(R.raw.sanji_list) : mContext.getResources().openRawResource(R.raw.yoji_list) ;
        CSVFile mCSVFile = new CSVFile(inputStream);
        ArrayList<String[]> myStringList = mCSVFile.read(); //The whole line is at index 0
        return Utils.getRandomWords(numWords, difficulty, myStringList);
    }

    /** Switch place of single kanji at i and j in mKanji string*/
    public void switchKanji(int i, int j){
        Character tmp_char_i = mKanji.charAt(i);
        mKanji = Utils.replaceCharAt(mKanji, mKanji.charAt(j), i);
        mKanji = Utils.replaceCharAt(mKanji, tmp_char_i, j);
    }

    /** Puts the current board config into shared preferences
     *  'boardConfig'
     * @param mLevel current level
     * @param sharedPref the preference object reference
     */
    public void save(int mLevel, SharedPreferences sharedPref){
        Log.d(msg, "saving board data to 'boardConfig'...");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("myLevel", mLevel);
        editor.putString("myJukus", mJukus);
        editor.putString("myKanjis", mKanji);
        StringBuilder myStates = new StringBuilder();
        for (int value : mButtonStateList) { myStates.append(value); }
        editor.putString("myStates", myStates.toString());
        editor.apply();
    }

    public boolean twoChipsAreSelected(){
        //Log.d(msg, "checkForSelectedChips() called");
        int count = 0;
        for (int state : mButtonStateList) {
            if (state == 1) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        //Log.d(msg,"no swap => select or deselect single button");
        return false;
    }

    // TODO: fix for yojijukugo case
    public void updateStates(){
        //Log.d(msg,"updateStates() called");
        for (int i = 0; i < mKanji.length()/ mJukuLength; i++){
            char char1;
            char char2;
            char char3;
            // If (i >= COLUMNS) it is the first row, otherwise it is the second row of we are comparing on
            char1 = (i >= COLUMNS) ? mKanji.charAt(i + (2 * COLUMNS)) : mKanji.charAt(i);
            char2 = (i >= COLUMNS) ? mKanji.charAt(i + (3 * COLUMNS)) : mKanji.charAt(i + COLUMNS);
            char3 = (i >= COLUMNS) ? mKanji.charAt(i + (4 * COLUMNS)) : mKanji.charAt(i + 2 * COLUMNS);
            String candidate = String.valueOf(char1) + char2 + char3;
            //Log.d(msg,"candidate: " + candidate);
            for (int j=0; j < mJukus.length(); j += mJukuLength) {
                String juku = mJukus.substring(j, j + mJukuLength);
                //Log.d(msg,"juku: " + juku);
                if (candidate.equals(juku)) {
                    if (i >= COLUMNS) {
                        mButtonStateList[i + (2 * COLUMNS)] = 2;
                        mButtonStateList[i + (3 * COLUMNS)] = 2;
                        mButtonStateList[i + (4 * COLUMNS)] = 2;
                    } else {
                        mButtonStateList[i] = 2;
                        mButtonStateList[i + COLUMNS] = 2;
                        mButtonStateList[i + 2 * COLUMNS] = 2;
                    }
                    //Log.d(msg,"Line was completed");
                }
            }
        }
        //Log.d(msg,"No line was completed");
    }

    public boolean boardIsComplete(){
        int numCompleted = 0;

        for (int i = 0; i < DIMENSIONS; i++) {
            if (mButtonStateList[i] == 2) numCompleted++;
        }

        return numCompleted == DIMENSIONS;
    }

    public void log(){
        Log.d(msg, " ");
        Log.d(msg, "BOARD LOG ---------------");
        Log.d(msg, "Jukus:");
        for(int i = 0; i < mJukus.length(); i += mJukuLength){
            Log.d(msg, mJukus.substring(i, i + mJukuLength));
        }
        Log.d(msg, "Kanjis:");
        for(int i = 0; i < DIMENSIONS; i += COLUMNS){
            Log.d(msg, mKanji.substring(i, i + COLUMNS));
        }
        for(int i = 0; i < DIMENSIONS; i += COLUMNS){
            Log.d(msg, i + Arrays.toString(mButtonStateList).replaceAll("\\[|\\]|,|\\s", "").substring(i, i + COLUMNS));
        }
        Utils.logIntList(mButtonStateList);
        Log.d(msg, "Rows: " + ROWS);
        Log.d(msg, "Columns: " + COLUMNS);
        Log.d(msg, "-------------------------");
    }

}
