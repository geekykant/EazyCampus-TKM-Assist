package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.model.SubjectAttendance;

import java.util.ArrayList;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.MyViewHolder> {

    private static final String TAG = "AttendanceListAdapter";

    private Context mContext;
    private int mResource;

    private ArrayList<SubjectAttendance> list_items;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SubjectAttendance person = list_items.get(position);

        holder.subjectName.setText(person.getSubjectName());
        holder.totalClasses.setText(person.getTotalClasses());
        holder.totalAttended.setText(person.getTotalAttended() + "/" + person.getTotalClasses());

        holder.totalClasses.setVisibility(View.GONE);

        holder.attendancePerct.setText(person.getAttendancePercent());

        //May crash if integer not received
        if (position != 0) {
            holder.bunksub.setText(getPFAttendance(Integer.parseInt(person.getTotalAttended()), Integer.parseInt(person.getTotalClasses()), holder));
        } else {
            holder.bunksub.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return list_items.size();
    }

    public AttendanceListAdapter(Context context, int resource, ArrayList<SubjectAttendance> objects) {
        this.mContext = context;
        this.mResource = resource;
        this.list_items = objects;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName, totalClasses, totalAttended, attendancePerct, bunksub;

        public MyViewHolder(View view) {
            super(view);
            subjectName = view.findViewById(R.id.subname);
            totalClasses = view.findViewById(R.id.totalsub);
            totalAttended = view.findViewById(R.id.attdsub);
            attendancePerct = view.findViewById(R.id.percentageattnd);
            bunksub = view.findViewById(R.id.bunksub);
        }
    }

    private String getPFAttendance(int classesAttended, int classesTotal, MyViewHolder holder) {
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




