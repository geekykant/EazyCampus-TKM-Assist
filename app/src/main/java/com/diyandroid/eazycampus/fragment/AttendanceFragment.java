package com.diyandroid.eazycampus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.diyandroid.eazycampus.helper.ProgressDialog;
import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.util.TokenUser;

import net.colindodd.gradientlayout.GradientRelativeLayout;

import java.util.ArrayList;

import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;

public class AttendanceFragment extends Fragment implements AttendanceHelper.AttendanceListener {

    private final static String TAG = AttendanceFragment.class.getSimpleName();

    private Context context;
    private View view;

    private ArrayList<SubjectAttendance> attendanceListMain;
    private AttendanceListAdapter adapter;
    private AttendanceHelper attendanceHelper;

    private TokenUser tokenUser;
    private ProgressDialog dialogLoad;

    public AttendanceFragment(Context context, ArrayList<SubjectAttendance> list) {
        this.context = context;
        this.attendanceListMain = list;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_items, container, false);

        tokenUser = new TokenUser(context);
        init();

        attendanceHelper = new AttendanceHelper(this);

        if (attendanceListMain == null) {
            attendanceListMain = new ArrayList<>();
            attendanceHelper.fetchAttendance(tokenUser.getUser());
        } else {
            checkAttendanceDrop();
            dialogLoad.dismiss();
        }

        return view;
    }

    private void init() {
        if (dialogLoad != null) dialogLoad.dismiss();
        dialogLoad = new ProgressDialog();
        dialogLoad.show(getChildFragmentManager(), "");

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

            if (loginName.split(" ").length != 1) {
                loginName = loginName.split(" ")[0];
            }

            welName.setText("Hi " + loginName + "!");
        }
    }


    @Override
    public void onAttendanceSuccess(ArrayList<SubjectAttendance> attendanceList) {
        Log.i(TAG, "Attendance List: " + attendanceList.get(3).getSubjectName());

        attendanceListMain = new ArrayList<>();
        attendanceListMain.addAll(attendanceList);
        adapter.notifyDataSetChanged();

        dialogLoad.dismiss();
    }

    @Override
    public void onAttendanceFailed(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        dialogLoad.dismiss();
    }

    public void checkAttendanceDrop() {
        int counter = 0;
        int ATTENDANCE_PERCENT = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("ATTENDANCE_PERCENT", 75);

        for (int i = 1; i < attendanceListMain.size(); i++) {
            int attended = Integer.parseInt(attendanceListMain.get(i).getTotalAttended());
            int total_classes = Integer.parseInt(attendanceListMain.get(i).getTotalClasses());

            float percentAttendance = (attended * 100.0f) / total_classes;
            if (percentAttendance < ATTENDANCE_PERCENT) {
                counter++;
            }
        }

        if (counter == 0) return;

        TextView attendance_status = view.findViewById(R.id.attendance_status);
        GradientRelativeLayout gradientRelativeLayout = view.findViewById(R.id.gradient_box);

        gradientRelativeLayout.setGradientBackgroundConfig(getResources().getColor(R.color.gradientOrange),
                getResources().getColor(R.color.cpb_red), RIGHT_LEFT);
        if (counter == 1)
            attendance_status.setText("Manh! You are under in " + counter + " subject!");
        else
            attendance_status.setText("Manh! You are under in " + counter + " subjects!");
    }
}
