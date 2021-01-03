package com.example.kanjicrush2;

public class Board {
    // Game-specific variables
    private static final int[][] mLevelDimensions = {{3,4},{3,6},{6,4},{6,5},{6,6}};
    private static final int mNumWordsStart = 4;

    // Board dimensions
    private static int ROWS, COLUMNS, DIMENSIONS;

    // level-specific variables
    private static int mLevel = 0;
    private static String mJukus;
    private static String mKanjis;
    private static int[] mButtonStateList;

    public Board(){

    }

}
