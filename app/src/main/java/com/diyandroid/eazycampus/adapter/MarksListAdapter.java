package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.diyandroid.eazycampus.Marks;
import com.diyandroid.eazycampus.R;

import java.util.ArrayList;

public class MarksListAdapter extends ArrayAdapter<Marks> {

    private static final String TAG = "MarksListAdapter";

    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView mark1;
        TextView mark2;
        TextView mark3;
        TextView mark4;
        TextView mark5;
        TextView mark6;
        TextView mark7;
        TextView mark8;
        TextView mark9;
        TextView mark10;
        TextView mark11;
        TextView mark12;
        TextView mark13;
        TextView mark14;
        TextView mark15;
        TextView mark16;
        TextView mark17;
        TextView mark18;
        TextView mark19;
        TextView mark20;
        TextView mark21;
        TextView mark22;
        TextView mark23;
        TextView mark24;
        TextView mark25;
    }

    public MarksListAdapter(Context context, int resource, ArrayList<Marks> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        ArrayList<String> marksArray = getItem(position).getMarksList();

        //Create the person object with the information
        Marks marksValue = new Marks(marksArray);

        //ViewHolder object
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            holder = new ViewHolder();

            holder.mark1 = (TextView) convertView.findViewById(R.id.mark1);
            holder.mark2 = (TextView) convertView.findViewById(R.id.mark2);
            holder.mark3 = (TextView) convertView.findViewById(R.id.mark3);
            holder.mark4 = (TextView) convertView.findViewById(R.id.mark4);
            holder.mark5 = (TextView) convertView.findViewById(R.id.mark5);
            holder.mark6 = (TextView) convertView.findViewById(R.id.mark6);
            holder.mark7 = (TextView) convertView.findViewById(R.id.mark7);
            holder.mark8 = (TextView) convertView.findViewById(R.id.mark8);
            holder.mark9 = (TextView) convertView.findViewById(R.id.mark9);
            holder.mark10 = (TextView) convertView.findViewById(R.id.mark10);
            holder.mark11 = (TextView) convertView.findViewById(R.id.mark11);
            holder.mark12 = (TextView) convertView.findViewById(R.id.mark12);
            holder.mark13 = (TextView) convertView.findViewById(R.id.mark13);
            holder.mark14 = (TextView) convertView.findViewById(R.id.mark14);
            holder.mark15 = (TextView) convertView.findViewById(R.id.mark15);
            holder.mark16 = (TextView) convertView.findViewById(R.id.mark16);
            holder.mark17 = (TextView) convertView.findViewById(R.id.mark17);
            holder.mark18 = (TextView) convertView.findViewById(R.id.mark18);
            holder.mark19 = (TextView) convertView.findViewById(R.id.mark19);
            holder.mark20 = (TextView) convertView.findViewById(R.id.mark20);
            holder.mark21 = (TextView) convertView.findViewById(R.id.mark21);
            holder.mark22 = (TextView) convertView.findViewById(R.id.mark22);
            holder.mark23 = (TextView) convertView.findViewById(R.id.mark23);
            holder.mark24 = (TextView) convertView.findViewById(R.id.mark24);
            holder.mark25 = (TextView) convertView.findViewById(R.id.mark25);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        int[] markid = {
                R.id.mark1,
                R.id.mark2,
                R.id.mark3,
                R.id.mark4,
                R.id.mark5,
                R.id.mark6,
                R.id.mark7,
                R.id.mark8,
                R.id.mark9,
                R.id.mark10,
                R.id.mark11,
                R.id.mark12,
                R.id.mark13,
                R.id.mark14,
                R.id.mark15,
                R.id.mark16,
                R.id.mark17,
                R.id.mark18,
                R.id.mark19,
                R.id.mark20,
                R.id.mark21,
                R.id.mark22,
                R.id.mark23,
                R.id.mark24,
                R.id.mark25
        };

        for (int i = 0; i < 25; i++) {
            if (i < marksValue.getMarksList().size()) {
                ((TextView) convertView.findViewById(markid[i])).setText(marksValue.getMarksList().get(i));
                ((TextView) convertView.findViewById(markid[i])).setTextColor(convertView.getResources().getColor(R.color.black));
            } else {
                ((TextView) convertView.findViewById(markid[i])).setVisibility(View.GONE);
            }
        }


        return convertView;
    }
}



