package com.example.kanjicrush2;

import android.util.Log;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Utils {

    /* TODO: check for duplicates */
    /** Returns a single string of random words from the list, number of words=numWords */
    public static String getRandomWords(int numWords, String[] stringList){
        // Get 3 random words
        StringBuilder randomWords = new StringBuilder();
        Random random = new Random();
        for (int i=0; i < numWords; i++) {
            randomWords.append(stringList[random.nextInt(stringList.length)]);
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
}
