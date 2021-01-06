package kanjiCrush.game;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVFile {
    InputStream mInputStream;

    /** Constructor */
    public CSVFile(InputStream inputStream){
        mInputStream = inputStream;
    }

    public ArrayList<String[]> read(){
        ArrayList<String[]> mResultList = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                mResultList.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                mInputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return mResultList;
    }
}

