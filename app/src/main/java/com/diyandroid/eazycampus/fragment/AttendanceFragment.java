package com.diyandroid.eazycampus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.adapter.AttendanceListAdapter;
import com.diyandroid.eazycampus.helper.AttendanceHelper;
import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.util.TokenUser;

import java.util.ArrayList;

public class AttendanceFragment extends Fragment implements AttendanceHelper.AttendanceListener {

    private final static String TAG = AttendanceFragment.class.getSimpleName();

    private Context context;
    private View view;

    private ArrayList<SubjectAttendance> attendanceListMain;
    private AttendanceListAdapter adapter;
    private AttendanceHelper attendanceHelper;

    private TokenUser tokenUser;

    public AttendanceFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_items, container, false);

        init();

        tokenUser = new TokenUser(context);
        attendanceHelper.fetchAttendance(tokenUser.getUser());

        return view;
    }

    private void init() {
        attendanceListMain = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.homelistAtendance);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        adapter = new AttendanceListAdapter(context, R.layout.adapter_attendance, attendanceListMain);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        String loginName = tokenUser.getPrefLoginName();

        TextView welName = view.findViewById(R.id.welName);
        if (!TextUtils.isEmpty(loginName)) {
            loginName = loginName.substring(0, 1).toUpperCase() + loginName.substring(1).toLowerCase();
            welName.setText("Hi " + loginName + "!");
        }
    }


    @Override
    public void onAttendanceSuccess(ArrayList<SubjectAttendance> attendanceList) {
        Log.i(TAG, "Attendance List: " + attendanceList.get(3).getSubjectName());

        attendanceListMain.clear();
        attendanceListMain.addAll(attendanceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttendanceFailed(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
