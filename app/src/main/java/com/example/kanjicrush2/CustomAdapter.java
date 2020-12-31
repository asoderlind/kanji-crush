package com.example.kanjicrush2;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private final ArrayList<Button> mButtons;
    private int mColumnWidth, mColumnHeight; //Declare local class-variables

    public CustomAdapter(ArrayList<Button> buttons, int columnWidth, int columnHeight) {
        mButtons = buttons;
        mColumnWidth = columnWidth;
        mColumnHeight = columnHeight;
    }

    @Override
    public int getCount() {
        return mButtons.size();
    }

    @Override
    public Object getItem(int position) {
        return (Object) mButtons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Button button;

        if(convertView==null){
            button = mButtons.get(position);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mColumnWidth, mColumnHeight);
            button.setLayoutParams(params);
        }else{
            button = (Button)convertView;
        }

        return button;
    }

}
