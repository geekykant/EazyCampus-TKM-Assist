package com.diyandroid.eazycampus.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.adapter.AttendanceListAdapter;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;
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

public class AttendancePage extends AppCompatActivity {

    Map<String, String> loginCookies;
    String jsonCookies, start_date, end_date;
    ListView mListView;
    ArrayList<SubjectAttendance> peopleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_page);

        Toolbar toolbar = findViewById(R.id.toolbarAttendance);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Subjectwise Attendance");

        jsonCookies = getIntent().getStringExtra("COOKIES");

        loginCookies =  new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {}.getType());
        Log.d("Attendance",loginCookies.toString());

        Button submit = findViewById(R.id.submitAttendance);

        String prev_themonth = String.valueOf(new DecimalFormat("00").format(Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date())) - 3));

        start_date = new SimpleDateFormat("dd/", Locale.getDefault()).format(new Date()) + prev_themonth + new SimpleDateFormat("/yyyy", Locale.getDefault()).format(new Date());
        end_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        ((TextView) findViewById(R.id.fromAttendance)).setText(start_date);
        ((TextView) findViewById(R.id.toAttendance)).setText(end_date);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new getAttendance().execute();
            }
        });

    }

    private boolean parsingSuccessful;

    private class getAttendance extends AsyncTask<Void, Void, Void> {

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressAttendance);
        Element table;
        Document llPage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            //Progress bar implementations

            Log.d("AttendancePage", "It begins on attendance page!");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document evaluationPage = Jsoup.connect("http://210.212.227.210/tkmce/Student/SubjectwiseAttendanceStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/index.aspx")
                        .followRedirects(true)
                        .userAgent("Mozilla")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                llPage = Jsoup.connect("http://210.212.227.210/tkmce/Student/SubjectwiseAttendanceStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/Student/SubjectwiseAttendanceStudent.aspx")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("X-MicrosoftAjax", "Delta=true")
                        .header("Cache-Control", "no-cache")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                        .data("ctl00$ScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel1|ctl00$ContentPlaceHolder1$btnSearch")
                        .data("ctl00$hdnisclose", "false")
                        .data("ctl00$ContentPlaceHolder1$txtFromdate", start_date)
                        .data("ctl00$ContentPlaceHolder1$txtToDate", end_date)
                        .data("ctl00$ContentPlaceHolder1$rbnView", "Subjectwise")
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
                        .execute().parse();

                table = llPage.getElementById("ctl00_ContentPlaceHolder1_tblStudentDetails").getElementsByTag("table").get(2);

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            if (parsingSuccessful && table!=null) {
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

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
