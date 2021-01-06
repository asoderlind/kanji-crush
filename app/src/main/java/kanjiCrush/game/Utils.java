package kanjiCrush.game;

import android.content.res.Resources;
import android.content.Context;

import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Utils {
    /* Easy=0 is 0-200, Medium=1 is 0-3000, Hard=2 is 3000+ */
    public static final int[] levelBrackets = {200, 3000};

    /* TODO: check for duplicates */
    /** Returns a single string of random words from the list, number of words=numWords */
    public static String getRandomWords(int numWords, int difficulty, ArrayList<String[]> arrayList){
        // Get 3 random words
        StringBuilder randomWords = new StringBuilder();
        Random random = new Random();
        for (int i=0; i < numWords; i++) {
            int bound = (difficulty == 2) ? arrayList.size() : levelBrackets[difficulty];
            Log.d("Utils :", "the bound is: " + bound);
            String[] kanjiWord = arrayList.get(random.nextInt(bound));
            randomWords.append(kanjiWord[0]);
        }
        return randomWords.toString();
    }

    /** Print the list to log*/
    public static void logIntList(int[] list) {
        StringBuilder stateStr = new StringBuilder();
        for(int s : list){
            stateStr.append(s);
        }
        Log.d("Android :", stateStr.toString());
    }

    public static String replaceCharAt(String s, Character c, int index){
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    /** Scramble the string */
    public static String scrambledString(String s) {
        String[] scram = s.split("");
        List<String> letters = Arrays.asList(scram);
        Collections.shuffle(letters);
        StringBuilder sb = new StringBuilder(s.length());
        for (String c : letters) {
            sb.append(c);
        }
        return sb.toString();
    }

    /** Converts dp to pixels */
    public static int dpToPx(float dp, Context c){
        Resources r = c.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
