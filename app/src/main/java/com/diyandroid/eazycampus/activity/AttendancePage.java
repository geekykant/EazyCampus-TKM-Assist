package com.diyandroid.eazycampus.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;
import com.diyandroid.eazycampus.adapter.AttendanceListAdapter;
import com.diyandroid.eazycampus.app.LogOutTimerUtil;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AttendancePage extends AppCompatActivity implements LogOutTimerUtil.LogOutListener, View.OnClickListener {

    Map<String, String> loginCookies;
    String jsonCookies, start_date, end_date;
    ListView mListView;
    ArrayList<SubjectAttendance> peopleList;

    EditText fromAttendance, toAttendance;

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_page);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        Toolbar toolbar = findViewById(R.id.toolbarAttendance);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Subjectwise Attendance");

        ((TextView) findViewById(R.id.minimum_attendance)).append("" + PreferenceManager.getDefaultSharedPreferences(this).getInt("ATTENDANCE_PERCENT", 75));

        jsonCookies = getIntent().getStringExtra("COOKIES");
        loginCookies = new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {
        }.getType());

        Button submit = findViewById(R.id.submitAttendance);

        fromAttendance = (EditText) findViewById(R.id.fromAttendance);
        toAttendance = (EditText) findViewById(R.id.toAttendance);

        fromAttendance.setText(mFirebaseRemoteConfig.getString("FROM_ATTENDANCE_DATE"));
        fromAttendance.setTag(mFirebaseRemoteConfig.getString("FROM_ATTENDANCE_DATE_TAG"));

        end_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        toAttendance.setText(end_date);
        toAttendance.setTag(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        fromAttendance.setOnClickListener(this);
        toAttendance.setOnClickListener(this);

        new getAttendance(this).execute();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new getAttendance(getApplicationContext()).execute();
            }
        });
    }

    private void setDate(final View view_txt) {
        // Get Current Date
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                ((EditText) view_txt).setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                ((EditText) view_txt).setTag(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private boolean parsingSuccessful;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toAttendance:
            case R.id.fromAttendance:
                setDate(view);
                break;
        }
    }

    private class getAttendance extends ExceptionHandlingAsyncTask<String, Void, Element> {

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressAttendance);
        Element table;
        Document llPage;

        Connection.Response response;

        public getAttendance(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            //Progress bar implementations

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
//                Document  homePage = Jsoup.connect("https://tkmce.linways.com/student/student.php?menu=home")
//                        .data("studentAccount", "170907")
//                        .data("studentPassword", "170907")
//                        .data("btnLogin", "Login")
//                        .userAgent("Mozilla")
//                        .followRedirects(true)
//                        .referrer("https://tkmce.linways.com/student/index.php")
//                        .cookies(loginCookies)
//                        .method(Connection.Method.POST)
//                        .timeout(120 * 1000)
//                        .execute().parse();


//                //todo: date change later!!
                response = Jsoup.connect("https://tkmce.linways.com/student/attendance/ajax/ajax_subjectwise_attendance.php?action=GET_REPORT&" +
                        "fromDate=" +
                        fromAttendance.getTag().toString() +
                        "&toDate=" +
                        toAttendance.getTag().toString()
                )
                        .ignoreContentType(true)
                        .cookies(loginCookies)
                        .referrer("https://tkmce.linways.com/student/student.php?menu=attendance&action=subjectwise")
//                        .data("ctl00$ContentPlaceHolder1$btnSearch", mFirebaseRemoteConfig.getString("ATTENDANCE_BTN_SEARCH"))
                        .method(Connection.Method.GET)
                        .userAgent("Mozilla")
                        .timeout(30 * 1000)
                        .execute();

                JsonElement element = new Gson().fromJson(response.body(), JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();

                if (jsonObj.get("success").toString().equals("true")) {
                    String raw_table = jsonObj.get("data").toString().replaceAll("\\\\n", "").replaceAll("\\\\", "");

                    table = Jsoup.parse(raw_table).select("tbody").first();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }

            return table;
        }

        @Override
        protected void onPostExecute2(Element element) {
            progressBar.setVisibility(View.GONE);

            if (parsingSuccessful && table != null && response.statusCode() == 200) {
                mListView = findViewById(R.id.listAtendance);

                CardView cardView = findViewById(R.id.cardView4);
                cardView.setVisibility(View.VISIBLE);

                //Add the SubjectAttendance objects to an ArrayList
                peopleList = new ArrayList<>();

                Elements row = table.getElementsByTag("tr");
                Elements details;

                peopleList.add(new SubjectAttendance("Subject Name", "Total Classes", "Attended", "% Attendance"));

                for (int i = 0; i < row.size(); i++) {
                    details = row.get(i).getElementsByTag("td");
                    peopleList.add(new SubjectAttendance(details.get(1).text(), details.get(3).text(), details.get(2).text(), details.get(4).text()));
                }

                AttendanceListAdapter adapter = new AttendanceListAdapter(AttendancePage.this, R.layout.adapter_attendance, peopleList);
                mListView.setAdapter(adapter);

            } else {
                Toast.makeText(AttendancePage.this, "Failed retrieving data! ", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attendance_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mBoostMenu:
                startActivity(new Intent(AttendancePage.this, BoosterAttendance.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void doLogout() {
        startActivity(new Intent(AttendancePage.this, SplashLoading.class));
        finish();
    }
}
