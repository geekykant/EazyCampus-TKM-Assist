package com.diyandroid.eazycampus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.adapter.AssignmentListAdapter;
import com.diyandroid.eazycampus.helper.AssignmentHelper;
import com.diyandroid.eazycampus.helper.ProgressDialog;
import com.diyandroid.eazycampus.model.AssignmentMarks;
import com.diyandroid.eazycampus.util.TokenUser;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class AssignmentFragment extends Fragment implements AssignmentHelper.AssignmentListener {

    private final static String TAG = AssignmentFragment.class.getSimpleName();

    private Context context;
    private View view;

    private AssignmentListAdapter adapter;
    private AssignmentHelper assignmentHelper;
    private TokenUser tokenUser;

    private ArrayList<AssignmentMarks> assignmentMarks;
    private ProgressDialog dialogLoad;
    private MaterialCardView home_assignment_view;

    public AssignmentFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.assignment_items, container, false);

        tokenUser = new TokenUser(context);
        init();

        assignmentHelper = new AssignmentHelper(this);
        assignmentHelper.fetchAssignment(tokenUser.getUser());

        return view;
    }

    private void init() {
        home_assignment_view = view.findViewById(R.id.home_assignment_view);
        home_assignment_view.setVisibility(View.GONE);

        if (dialogLoad != null) dialogLoad.dismiss();
        dialogLoad = new ProgressDialog();
        dialogLoad.show(getChildFragmentManager(), "");

        assignmentMarks = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.homeListAssignment);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        adapter = new AssignmentListAdapter(context, R.layout.adapter_assignment, assignmentMarks);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onAssignmentSuccess(ArrayList<AssignmentMarks> assignments) {
        Log.i(TAG, "Attendance List: " + assignments.get(0).getAssignment_no());

        assignmentMarks.addAll(assignments);
        adapter.notifyDataSetChanged();

        home_assignment_view.setVisibility(View.VISIBLE);
        dialogLoad.dismiss();
    }

    @Override
    public void onAssignmentFailed(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        dialogLoad.dismiss();
    }
}
