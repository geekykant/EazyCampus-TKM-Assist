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
import android.widget.RadioButton;
import android.widget.Toast;

import com.diyandroid.eazycampus.adapter.MarksListAdapter;
import com.diyandroid.eazycampus.Marks;
import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MarksPage extends AppCompatActivity implements View.OnClickListener {

    RadioButton internalMaks, sessionalMarks;
    Map<String, String> loginCookies;
    String jsonCookies;
    Element markTable;

    ListView mListView;
    ArrayList<Marks> peopleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks_page);

        Button submit = findViewById(R.id.submitMarks);

        Toolbar toolbar = findViewById(R.id.toolbarMarks);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Evaluation Marks");

        jsonCookies = getIntent().getStringExtra("COOKIES");

        loginCookies =  new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {}.getType());

        internalMaks = findViewById(R.id.internal);
        sessionalMarks = findViewById(R.id.sessional);

        internalMaks.setOnClickListener(this);
        sessionalMarks.setOnClickListener(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internalMaks.isChecked()) {
                    sessionalMarks.setChecked(false);
                    new getInternalMarks().execute();
                }

                if (sessionalMarks.isChecked()) {
                    internalMaks.setChecked(false);
                    new getSessionalMarks().execute();
                }
            }
        });

    }

    private boolean parsingSuccessful;

    private class getInternalMarks extends AsyncTask<Void, Void, Void> {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressMarks);
        Elements row;
        int column;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document evaluationPage = Jsoup.connect("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/index.aspx")
                        .followRedirects(true)
                        .data("Upgrade-Insecure-Requests", "1")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                Document evalPage = Jsoup.connect("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .data("__EVENTTARGET", "")
                        .data("__EVENTARGUMENT", "")
                        .data("__LASTFOCUS", "")
                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
                        .data("ctl00$hdnisclose", evaluationPage.getElementById("ctl00_hdnisclose").val())
                        // .data("ctl00$ContentPlaceHolder1$ddlClass", "1723")
                        .data("ctl00$ContentPlaceHolder1$rbtnReportType", "0")
                        .data("ctl00$ContentPlaceHolder1$btnView", "View")
                        .data("ctl00$HiddenField1", evaluationPage.getElementById("ctl00_HiddenField1").val())
                        .followRedirects(true)
                        .data("Upgrade-Insecure-Requests", "1")
                        .method(Connection.Method.POST)
                        .timeout(30 * 1000)
                        .execute().parse();

                markTable = evalPage.getElementById("ctl00_ContentPlaceHolder1_tblStudentMarks");

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            if (parsingSuccessful && markTable!=null){
                markTable = markTable.getElementsByTag("table").get(1);

                mListView = findViewById(R.id.listAtendance);

                CardView cardView = findViewById(R.id.cardView5);
                cardView.setVisibility(View.VISIBLE);

                peopleList = new ArrayList<>();


                row = markTable.getElementsByTag("tr"); //count no of rows
                column = markTable.getElementsByTag("tr").get(0).getElementsByTag("td").size(); //count no of columns

                Elements details;
                ArrayList<String> marksList;

                for (int i = 0; i < row.size(); i++) {
                    marksList = new ArrayList<>();

                    details = row.get(i).getElementsByTag("td");
                    for (int j = 0; j < column; j++) {
                        marksList.add(details.get(j).text());
                    }
                    peopleList.add(new Marks(marksList));
                }
                MarksListAdapter adapter = new MarksListAdapter(MarksPage.this, R.layout.adapter_marks, peopleList);
                mListView.setAdapter(adapter);
            }else{
                Toast.makeText(MarksPage.this, "Failed retrieving data! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class getSessionalMarks extends AsyncTask<Void, Void, Void> {

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressMarks);
        Elements row;
        int column;
        Document evalPage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            //Progress bar implementations
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document evaluationPage = Jsoup.connect("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/index.aspx")
                        .followRedirects(true)
                        .data("Upgrade-Insecure-Requests", "1")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                Log.i("FacultyDirectory", "Grabbing Marks In!");
                Log.i("FacultyDirectory", "I am here!");

                evalPage = Jsoup.connect("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/ExamManagement/AutoMarkStatusofStudent.aspx")
                        .data("__EVENTTARGET", "")
                        .data("__EVENTARGUMENT", "")
                        .data("__LASTFOCUS", "")
                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
                        .data("ctl00$hdnisclose", evaluationPage.getElementById("ctl00_hdnisclose").val())
//                         .data("ctl00$ContentPlaceHolder1$ddlClass", "1723")
                        .data("ctl00$ContentPlaceHolder1$rbtnReportType", "1")
                        .data("ctl00$ContentPlaceHolder1$btnView", "View")
                        .data("ctl00$HiddenField1", evaluationPage.getElementById("ctl00_HiddenField1").val())
                        .followRedirects(true)
                        .data("Upgrade-Insecure-Requests", "1")
                        .method(Connection.Method.POST)
                        .timeout(30 * 1000)
                        .execute().parse();

                markTable = evalPage.getElementById("ctl00_ContentPlaceHolder1_tblStudentMarks");

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            if (parsingSuccessful && markTable!=null) {
                row = evalPage.getElementById("ctl00_ContentPlaceHolder1_tblStudentMarks").
                        getElementsByTag("table").get(1).getElementsByTag("tr").get(0).getElementsByTag("td");

                markTable = markTable.getElementsByTag("table").get(1);

                mListView = findViewById(R.id.listAtendance);

                CardView cardView = findViewById(R.id.cardView5);
                cardView.setVisibility(View.VISIBLE);

                peopleList = new ArrayList<>();


                row = markTable.getElementsByTag("tr"); //count no of rows
                column = markTable.getElementsByTag("tr").get(0).getElementsByTag("td").size(); //count no of columns

                Elements details;
                ArrayList<String> marksList;

                for (int i = 0; i < row.size(); i++) {
                    marksList = new ArrayList<>();

                    details = row.get(i).getElementsByTag("td");
                    for (int j = 0; j < column; j++) {
                        marksList.add(details.get(j).text());
                    }
                    peopleList.add(new Marks(marksList));
                }

                MarksListAdapter adapter = new MarksListAdapter(MarksPage.this, R.layout.adapter_marks, peopleList);
                mListView.setAdapter(adapter);
            }else{
                Toast.makeText(MarksPage.this, "Failed retrieving data! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Menu bar item click
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.internal:
                sessionalMarks.setChecked(false);
                break;
            case R.id.sessional:
                internalMaks.setChecked(false);
                break;
        }
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
