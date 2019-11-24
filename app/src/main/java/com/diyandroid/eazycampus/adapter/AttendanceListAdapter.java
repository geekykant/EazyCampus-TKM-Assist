package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;

import java.util.ArrayList;

public class AttendanceListAdapter extends ArrayAdapter<SubjectAttendance> {

    private static final String TAG = "AttendanceListAdapter";

    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView subjectName, totalClasses, totalAttended, attendancePerct, bunksub;
    }

    public AttendanceListAdapter(Context context, int resource, ArrayList<SubjectAttendance> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private ViewHolder holder;

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Create the person object with the information
        SubjectAttendance person = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            holder = new ViewHolder();

            holder.subjectName = convertView.findViewById(R.id.subname);
            holder.totalClasses = convertView.findViewById(R.id.totalsub);
            holder.totalAttended = convertView.findViewById(R.id.attdsub);
            holder.attendancePerct = convertView.findViewById(R.id.percentageattnd);
            holder.bunksub = convertView.findViewById(R.id.bunksub);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.subjectName.setText(person.getSubjectName());
        holder.totalClasses.setText(person.getTotalClasses());
        holder.totalAttended.setText(person.getTotalAttended() + "/" + person.getTotalClasses());

        holder.totalClasses.setVisibility(View.GONE);

        holder.attendancePerct.setText(person.getAttendancePercent());

        //May crash if integer not received
        if (position != 0) {
            holder.bunksub.setText(getPFAttendance(Integer.parseInt(person.getTotalAttended()), Integer.parseInt(person.getTotalClasses())));
        } else {
            holder.bunksub.setText("");
        }

        return convertView;
    }

    String getPFAttendance(int classesAttended, int classesTotal) {
        int ATTENDANCE_PERCENT = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("ATTENDANCE_PERCENT", 75);

        if (classesTotal == 0) {
            holder.bunksub.setTextColor(Color.GRAY);
            return "(0)";
        }

        float percentAttendance = ((classesAttended * 100.0f) / classesTotal);

        int flag = 0;
        float percent;
        if (percentAttendance == 0) {
            return "(0)";
        } else if (percentAttendance >= ATTENDANCE_PERCENT) {
            do {
                flag += 1;
                percent = ((classesAttended) * 100.0f) / (classesTotal + flag);
            } while (percent >= ATTENDANCE_PERCENT);

//            return "You are already ahead by " + (flag - 1) + " classes. You are good!";
            holder.bunksub.setTextColor(Color.parseColor("#33CC66"));
            return "(+" + (flag - 1) + ")";
        } else {
            do {
                flag += 1;
                percent = ((classesAttended + flag) * 100.0f) / (classesTotal + flag);
            } while (percent < ATTENDANCE_PERCENT);

//            return "You've to sit in " + flag + " more classes to be eligible to write exams!";
            holder.bunksub.setTextColor(Color.RED);
            return "(-" + flag + ")";
        }
    }
}




