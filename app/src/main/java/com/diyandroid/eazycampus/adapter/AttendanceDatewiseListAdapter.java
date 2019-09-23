package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;

import java.util.ArrayList;

public class AttendanceDatewiseListAdapter extends ArrayAdapter<SubjectAttendance> {

    private static final String TAG = "AttendanceListAdapter";

    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView subjectName, totalClasses, totalAttended, attendancePerct;
    }

    public AttendanceDatewiseListAdapter(Context context, int resource, ArrayList<SubjectAttendance> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Create the person object with the information
        SubjectAttendance person = getItem(position);

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            holder = new ViewHolder();

            holder.subjectName = (TextView) convertView.findViewById(R.id.subname);
            holder.totalClasses = (TextView) convertView.findViewById(R.id.totalsub);
            holder.totalAttended = (TextView) convertView.findViewById(R.id.attdsub);
            holder.attendancePerct = (TextView) convertView.findViewById(R.id.percentageattnd);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.subjectName.setText(person.getSubjectName());
        holder.totalClasses.setText(person.getTotalClasses());
        holder.totalAttended.setText(person.getTotalAttended());
        holder.attendancePerct.setText(person.getAttendancePercent());

        return convertView;
    }
}




