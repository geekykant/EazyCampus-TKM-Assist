package com.diyandroid.eazycampus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.model.AssignmentMarks;

import java.util.ArrayList;

public class AssignmentListAdapter extends RecyclerView.Adapter<AssignmentListAdapter.MyViewHolder> {

    private static final String TAG = "AssignmentListAdapter";

    private Context mContext;
    private int mResource;

    private ArrayList<AssignmentMarks> list_items;

    public AssignmentListAdapter(Context context, int resource, ArrayList<AssignmentMarks> objects) {
        this.mContext = context;
        this.mResource = resource;
        this.list_items = objects;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AssignmentMarks mark = list_items.get(position);

        holder.subjectName.setText(mark.getSubjects());

        holder.marks.setText(mark.getMarks());
        holder.maxMarks.setText(mark.getMax_mark());

        holder.assignment_no.setText(mark.getAssignment_no());
        
        // if not first item check if item above has the same header
        if (position > 0 && list_items.get(position - 1).getAssignment_no().equals(mark.getAssignment_no())) {
            holder.assignment_no.setVisibility(View.GONE);
        } else {
            holder.assignment_no.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list_items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView subjectName, marks, maxMarks, assignment_no;

        MyViewHolder(View view) {
            super(view);
            subjectName = view.findViewById(R.id.subject_name);
            marks = view.findViewById(R.id.marks);
            maxMarks = view.findViewById(R.id.max_marks);
            assignment_no = view.findViewById(R.id.assignment_no);
        }
    }
}




