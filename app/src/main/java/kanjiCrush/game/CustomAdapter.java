package kanjiCrush.game;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;

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
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = mColumnWidth;
            params.height = mColumnHeight;
            button.setLayoutParams(params);
        }else{
            button = (Button)convertView;
        }

        return button;
    }

}
