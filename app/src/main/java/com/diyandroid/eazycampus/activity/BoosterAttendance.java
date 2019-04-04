package com.diyandroid.eazycampus.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.diyandroid.eazycampus.R;

public class BoosterAttendance extends AppCompatActivity {

    private int classesTotal, classesAttended;
    private TextView totalClasses, attendedClasses, perAtteda;
    private float percentAttendance;

    private int ATTENDANCE_PERCENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booster_attendance);

        //toolbar the fancy stuff
        Toolbar toolbar = findViewById(R.id.toolbarAttendanceBooster);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ATTENDANCE_PERCENT = PreferenceManager.getDefaultSharedPreferences(this).getInt("ATTENDANCE_PERCENT", 75);
        ((TextView) findViewById(R.id.minimum_attendance_booster)).append("" + ATTENDANCE_PERCENT);

        Button submit = (Button) findViewById(R.id.submitAttendanceBooster);
        attendedClasses = (TextView) findViewById(R.id.classesAttended);
        totalClasses = (TextView) findViewById(R.id.totalClasses);

        perAtteda = (TextView) findViewById(R.id.percentAttendance);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (totalClasses.getText() != "0" && !totalClasses.getText().toString().isEmpty() && attendedClasses.getText() != "0" && !attendedClasses.getText().toString().isEmpty()) {
                    classesAttended = Integer.valueOf(attendedClasses.getText().toString());
                    classesTotal = Integer.valueOf(totalClasses.getText().toString());

                    percentAttendance = ((classesAttended * 100.0f) / classesTotal);
                    TextView decisionBooster = (TextView) findViewById(R.id.decisionBooster);

                    if (percentAttendance <= 100) {
                        perAtteda.setText(String.valueOf(Math.round(percentAttendance)));

                        if (percentAttendance == ATTENDANCE_PERCENT) {
                            decisionBooster.setText(R.string.border_line_attendance);
                        } else {
                            decisionBooster.setText(getPFAttendance(classesAttended, classesTotal, percentAttendance));
                        }
                        decisionBooster.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    String getPFAttendance(int classesAttended, int classesTotal, float percentAttendance) {
        int flag = 0;
        float percent;
        if (percentAttendance >= ATTENDANCE_PERCENT) {
            do {
                flag += 1;
                percent = ((classesAttended) * 100.0f) / (classesTotal + flag);
            } while (percent >= ATTENDANCE_PERCENT);

            return "You are already ahead by " + (flag - 1) + " classes. You are good!";
        } else {
            do {
                flag += 1;
                percent = ((classesAttended + flag) * 100.0f) / (classesTotal + flag);
            } while (percent < ATTENDANCE_PERCENT);

            return "You've to sit in " + flag + " more classes to be eligible to write exams!";
        }
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
