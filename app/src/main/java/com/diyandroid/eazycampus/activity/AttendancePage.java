package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;
import com.diyandroid.eazycampus.adapter.AttendanceDatewiseListAdapter;
import com.diyandroid.eazycampus.adapter.AttendanceListAdapter;
import com.diyandroid.eazycampus.app.LogOutTimerUtil;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AttendancePage extends AppCompatActivity implements LogOutTimerUtil.LogOutListener {

    Map<String, String> loginCookies;
    String jsonCookies, start_date, end_date;
    ListView mListView;
    ArrayList<SubjectAttendance> peopleList;

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

        jsonCookies = getIntent().getStringExtra("COOKIES");
        loginCookies = new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {
        }.getType());

        Button submit = findViewById(R.id.submitAttendance);

        String prev_themonth = String.valueOf(new DecimalFormat("00").format(Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date())) - 3));

        start_date = new SimpleDateFormat("dd/", Locale.getDefault()).format(new Date()) + prev_themonth + new SimpleDateFormat("/yyyy", Locale.getDefault()).format(new Date());
        end_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        ((TextView) findViewById(R.id.fromAttendance)).setText(start_date);
        ((TextView) findViewById(R.id.toAttendance)).setText(end_date);

        new getAttendance(this).execute();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new getAttendance(getApplicationContext()).execute();
//                new getDatewiseAttendance(getApplicationContext()).execute();
            }
        });
    }

    private boolean parsingSuccessful;

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
                Document evaluationPage = Jsoup.connect(getString(R.string.tkmce_attendance_url))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_index_url))
                        .followRedirects(true)
                        .userAgent("Mozilla")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                response = Jsoup.connect(getString(R.string.tkmce_attendance_subjectwise_url))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_attendance_subjectwise_url))
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("X-MicrosoftAjax", "Delta=true")
                        .header("Cache-Control", "no-cache")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                        .data("ctl00$ScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel1|ctl00$ContentPlaceHolder1$btnSearch")
                        .data("ctl00$hdnisclose", "false")
                        .data("ctl00$ContentPlaceHolder1$txtFromdate", start_date)
                        .data("ctl00$ContentPlaceHolder1$txtToDate", end_date)
                        .data("ctl00$ContentPlaceHolder1$rbnView", mFirebaseRemoteConfig.getString("ATTENDANCE_CONTENT_PLACEHOLDER"))
                        .data("ctl00$HiddenField1", "")
                        .data("__EVENTTARGET", mFirebaseRemoteConfig.getString("ATTENDANCE_EVENTTARGET"))
                        .data("__EVENTARGUMENT", "")
                        .data("__LASTFOCUS", "")
                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
//                        .data(mFirebaseRemoteConfig.getString("ATTENDANCE_EXTRA_FIELD1"), mFirebaseRemoteConfig.getString("ATTENDANCE_EXTRA_FIELD2"))
                        .data("__ASYNCPOST", "true")
                        .data("ctl00$ContentPlaceHolder1$btnSearch", mFirebaseRemoteConfig.getString("ATTENDANCE_BTN_SEARCH"))
                        .followRedirects(false)
                        .method(Connection.Method.POST)
                        .userAgent("Mozilla")
                        .timeout(30 * 1000)
                        .execute();

                llPage = response.parse();
                table = llPage.getElementById(getString(R.string.attendance_table_id)).getElementsByTag("table").get(2);

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

                for (int i = 0; i < row.size(); i++) {
                    details = row.get(i).getElementsByTag("td");
                    peopleList.add(new SubjectAttendance(details.get(0).text(), details.get(1).text(), details.get(2).text(), details.get(3).text()));
                }

                AttendanceListAdapter adapter = new AttendanceListAdapter(AttendancePage.this, R.layout.adapter_attendance, peopleList);
                mListView.setAdapter(adapter);

            } else {
                Toast.makeText(AttendancePage.this, "Failed retrieving data! ", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class getDatewiseAttendance extends ExceptionHandlingAsyncTask<String, Void, Element> {

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressAttendance);
        Element table;
        Document llPage;

        Connection.Response response;
        int row, column;

        public getDatewiseAttendance(Context context) {
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
                Document evaluationPage = Jsoup.connect(getString(R.string.tkmce_attendance_url))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_index_url))
                        .followRedirects(true)
                        .userAgent("Mozilla")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                response = Jsoup.connect(getString(R.string.tkmce_attendance_subjectwise_url))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_attendance_subjectwise_url))
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("X-MicrosoftAjax", "Delta=true")
                        .header("Cache-Control", "no-cache")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                        .data("ctl00$ScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel1|ctl00$ContentPlaceHolder1$btnSearch")
                        .data("ctl00$hdnisclose", "false")
                        .data("ctl00$ContentPlaceHolder1$txtFromdate", start_date)
                        .data("ctl00$ContentPlaceHolder1$txtToDate", end_date)
                        .data("ctl00$ContentPlaceHolder1$rbnView", "Datewise")
                        .data("ctl00$HiddenField1", "")
                        .data("__EVENTTARGET", "ctl00$ContentPlaceHolder1$rbnView$0")
                        .data("__EVENTARGUMENT", "")
                        .data("__LASTFOCUS", "")
                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
                        .data("__ASYNCPOST", "true")
                        .data("ctl00$ContentPlaceHolder1$btnSearch", "View")
                        .followRedirects(false)
                        .method(Connection.Method.POST)
                        .userAgent("Mozilla")
                        .timeout(30 * 1000)
                        .execute();

                llPage = response.parse();
                table = llPage.getElementById(getString(R.string.attendance_table_id)).getElementsByTag("table").get(2);
                System.out.print(table);

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

                Elements row = table.getElementsByTag("tr");
                column = row.get(0).getElementsByTag("td").size();

                CardView cardView = findViewById(R.id.cardView4);
                cardView.setVisibility(View.VISIBLE);
                Elements details = null;

                //Add the SubjectAttendance objects to an ArrayList
                peopleList = new ArrayList<>();

                Elements details1 = row.get(0).getElementsByTag("td");
                Elements details2 = row.get(1).getElementsByTag("td");
                Elements details3 = row.get(2).getElementsByTag("td");
                Elements details4 = row.get(3).getElementsByTag("td");

                ArrayList<String> heading = null;

                for (int i = 0; i < column; i++) {
                    heading.add(details3.get(i).text() + "/" + details2.get(i).text() + "/" + details1.get(i).text() + "\n(" + details4.get(i).text() + " Period)");
                }

                //TODO: Create new multi-string class for date-wise attendance

                for (int i = 4; i < row.size(); i++) {
                    details = row.get(i).getElementsByTag("td");
                    peopleList.add(new SubjectAttendance(details.get(0).text(), details.get(1).text(), details.get(2).text(), details.get(3).text()));
                }

                AttendanceDatewiseListAdapter adapter = new AttendanceDatewiseListAdapter(AttendancePage.this, R.layout.adapter_attendance_datewise, peopleList);
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
