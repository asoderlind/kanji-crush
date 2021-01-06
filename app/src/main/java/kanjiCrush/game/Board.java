package kanjiCrush.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.InputStream;

public class Board {
    private final String msg = "Board :";
    private int ROWS;
    private int COLUMNS;
    private int DIMENSIONS;
    private static final int[][] mLevelDimensions = {{3,4},{6,3},{6,4},{6,5},{6,6}};
    private final Context mContext;
    private String mJukus;
    private String mKanji;
    private int[] mButtonStateList;

    public Board(Context c, int level){
        Log.d(msg, "Board constructor called");
        mContext = c;
        mJukus = genJukus(4 + level*2);
        mKanji = Utils.scrambledString(mJukus);
        ROWS = mLevelDimensions[level][0];
        COLUMNS = mLevelDimensions[level][1];
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

    /** Returns String with the 3-character-words */
    private String genJukus(int numWords) {
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.jukugo);
        CSVFile mCSVFile = new CSVFile(inputStream);
        String[] myStringList = (String[]) mCSVFile.read().get(0); //The whole line is at index 0
        return Utils.getRandomWords(numWords, myStringList);
    }

    /** Switch place of single kanji at i and j in mKanji string*/
    public void switchKanji(int i, int j){
        Character tmp_char_i = mKanji.charAt(i);
        mKanji = Utils.replaceCharAt(mKanji, mKanji.charAt(j), i);
        mKanji = Utils.replaceCharAt(mKanji, tmp_char_i, j);
    }

    public void save(SharedPreferences sharedPref, int mLevel){
        Log.d(msg, "saving board data...");
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("myLevel", mLevel);
        editor.putString("myJukus", mJukus);
        editor.putString("myKanjis", mKanji);
        StringBuilder myStates = new StringBuilder();
        for (int value : mButtonStateList) { myStates.append(value); }
        editor.putString("myStates", myStates.toString());
        editor.apply();
    }

    public void load(SharedPreferences sharedPref, int mLevel){
        Log.d(msg, "loading board data...");
        ROWS = mLevelDimensions[mLevel][0];
        COLUMNS = mLevelDimensions[mLevel][1];
        DIMENSIONS = ROWS*COLUMNS;

        if(sharedPref.getString("myJukus", null) != null){
            mJukus = sharedPref.getString("myJukus", null);
        }

        if(sharedPref.getString("myKanjis", null) != null){
            mKanji = sharedPref.getString("myKanjis", null);
        }

        String myStates = sharedPref.getString("myStates", null);
        if (myStates != null){
            mButtonStateList = new int[myStates.length()];
            for(int i = 0; i < myStates.length(); i++) {
                mButtonStateList[i] = Character.getNumericValue(myStates.charAt(i));
            }
        }
    }

    public boolean twoChipsAreSelected(){
        Log.d(msg, "checkForSelectedChips() called");
        int count = 0;
        for (int state : mButtonStateList) {
            if (state == 1) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        Log.d(msg,"no swap => select or deselect single button");
        return false;
    }

    public void updateStates(){
        Log.d(msg,"updateStates() called");
        for (int i = 0; i < mKanji.length()/3; i++){
            char char1;
            char char2;
            char char3;
            // If (i >= COLUMNS) it is the first row, otherwise it is the second row of we are comparing on
            char1 = (i >= COLUMNS) ? mKanji.charAt(i + (2 * COLUMNS)) : mKanji.charAt(i);
            char2 = (i >= COLUMNS) ? mKanji.charAt(i + (3 * COLUMNS)) : mKanji.charAt(i + COLUMNS);
            char3 = (i >= COLUMNS) ? mKanji.charAt(i + (4 * COLUMNS)) : mKanji.charAt(i + 2 * COLUMNS);
            String candidate = String.valueOf(char1) + char2 + char3;
            //Log.d(msg,candidate);

            for (int j=0; j < mJukus.length(); j += 3) {
                String juku = mJukus.substring(j, j + 3);

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
        Log.d(msg, "Jukus: " + mJukus);
        Log.d(msg, "Kanji: " + mKanji);
        Utils.logIntList(mButtonStateList);
        Log.d(msg, "Rows: " + ROWS);
        Log.d(msg, "Columns: " + COLUMNS);
        Log.d(msg, "-------------------------");
    }

}
