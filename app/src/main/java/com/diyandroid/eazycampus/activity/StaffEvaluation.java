package com.diyandroid.eazycampus.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StaffEvaluation extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button submit;
    private Map<String, String> loginCookies;
    private String href;
    private Document doc = null;
    private boolean evalCheck = false;
    private TextView progressNo, staffList;
    private int Count = 0, staffno = 0;
    private String[] newHref;
    private Elements links;
    private List<String> hreffs = new ArrayList<String>();

    private final String userAgent = "Firefox";
    private final String urlEvaluation = "http://210.212.227.210/tkmce/Student%20Evaluation/StudSubjectStaffsList.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_evaluation);

        String jsonCookies = getIntent().getStringExtra("COOKIES");

        Toolbar toolbar = findViewById(R.id.toolbarEvaluation);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Staff Evaluation");


        loginCookies =  new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {}.getType());

        submit = (Button) findViewById(R.id.submit);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        progressBar.setVisibility(View.INVISIBLE);
        new submitEvaluation().execute();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                startEvaluation(newHref[staffno]);
            }
        });

    }

    private boolean parsingSuccessful;

    //This function is process
    private class submitEvaluation extends AsyncTask<Void, Void, Void> {

        Document homePage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            progressBar.setVisibility(View.VISIBLE);
            submit.setClickable(false);
            //Progress bar implementations
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.d("StaffEvaluation", "Reached Evaluation Page!");

            try {
                homePage = (Jsoup.connect("http://210.212.227.210/tkmce/Common/Home/Home.aspx")
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .cookies(loginCookies)
                        .referrer("http://210.212.227.210/tkmce/Common/Home/Home.aspx")
                        .execute()).parse();

                Log.d("FacultyDirectory", "Scraped Staff Evaluation Page!");

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            submit.setClickable(true);

            if (parsingSuccessful) {

                TextView progressNo = (TextView) findViewById(R.id.progressNo);

                if (homePage.select("table#ctl00_ContentPlaceHolder1_dlAlertLIst_dlAlertDisplay").size() != 0) {
                    evalCheck = true;
                }

                if (evalCheck) {
                    Toast.makeText(StaffEvaluation.this, "You have Staff Evaluation!!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                    submit.setEnabled(true);
                    getEvaluation();
                } else {
                    submit.setClickable(false);
                    progressNo.setText("No Staff Evaluation!");
                    Toast.makeText(StaffEvaluation.this, "No Staff Evaluation!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(StaffEvaluation.this, "Failed retrieving data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getEvaluation() {
        staffList = (TextView) findViewById(R.id.staffList);
        progressNo = (TextView) findViewById(R.id.progressNo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Connection.Response loginForm = Jsoup.connect(urlEvaluation)
                            .method(Connection.Method.GET)
                            .userAgent(userAgent)
                            .cookies(loginCookies)
                            .timeout(10 * 1000)
                            .referrer("http://210.212.227.210/tkmce/Common/Home/Home.aspx")
                            .execute();

                    Log.d("FacultyDirectory", "Scraped Staff Evaluation Page!");

                    doc = loginForm.parse();

                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Count = 0;
                        StringBuilder staffs = new StringBuilder();
                        links = doc.select("table#ctl00_ContentPlaceHolder1_gvStudSubjectStaffsList").select("a");
                        for (Element link : links) {
                            String href = link.attr("abs:href");
                            if (!(href.equals("") || link.text().equals(""))) {
//                                staffs+= Count + " : " + href + " : " + link.text() + "\n";
                                Count++;
                                staffs.append("").append(link.text().toUpperCase()).append("\n\n");
                            }
                        }

                        staffList.setText(staffs.toString());
                        progressNo.setText(Count + " Staffs Remaining");

                        for (Element link : links) {
                            href = link.attr("abs:href");
                            if (!(href.equals("") || link.text().equals(""))) {
                                hreffs.add(href);
                            }
                            newHref = new String[hreffs.size()];
                            newHref = hreffs.toArray(newHref);
                        }

                        CardView cardView = findViewById(R.id.cardView6);
                        cardView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void startEvaluation(final String href) {
        Log.d("EvaluationPage", "Link: " + href);

        staffList = (TextView) findViewById(R.id.staffList);
        progressNo = (TextView) findViewById(R.id.progressNo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                Connection.Response loginForm = null;

                try {
                    Connection.Response getForm = Jsoup.connect(href)
                            .method(Connection.Method.GET)
                            .userAgent(userAgent)
                            .cookies(loginCookies)
                            .referrer(urlEvaluation)
                            .header("Upgrade-Insecure-Requests", "1")
                            .followRedirects(false)
                            .execute();

                    Document evalData = getForm.parse();

                    loginForm = Jsoup.connect(href)
                            .method(Connection.Method.POST)
                            .userAgent(userAgent)
                            .cookies(loginCookies)
                            .referrer(href)
                            .data("__EVENTTARGET", "")
                            .data("__EVENTARGUMENT", "")
                            .data("__VIEWSTATE", evalData.getElementById("__VIEWSTATE").val())
                            .data("__VIEWSTATEGENERATOR", evalData.getElementById("__VIEWSTATEGENERATOR").val())
                            .data("__EVENTVALIDATION", evalData.getElementById("__EVENTVALIDATION").val())
                            .data("ctl00$hdnisclose", "false")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl02$rbtn_Choice_1", "17.7500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl03$rbtn_Choice_2", "14.5000")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl04$rbtn_Choice_3", "7.5000")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl05$rbtn_Choice_4", "5.8500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl06$rbtn_Choice_5", "4.0000")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl07$rbtn_Choice_6", "9.2500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl08$rbtn_Choice_7", "4.7500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl09$rbtn_Choice_8", "6.8750")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl10$rbtn_Choice_9", "6.7500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl11$rbtn_Choice_10", "11.5000")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl12$rbtn_Choice_11", "11.2500")
                            .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl13$txt_Choice_12", "")
                            .data("ctl00$ContentPlaceHolder1$btnSave", "Save")
                            .data("ctl00$HiddenField1", "")
                            .followRedirects(true)
                            .execute();

                    Log.d("FacultyDirectory", "I guess evaluation done!");

                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("n");
                }

                if (loginForm.url().toExternalForm().equals(urlEvaluation)) {
                    Log.d("StaffEvaluation", "Evaluation Completed!!");
                    staffno++;
                } else {
                    Log.d("StaffEvaluation", "Not Completed In!");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Count--;
                        String words[] = staffList.getText().toString().split("\n");
                        List<String> list = new ArrayList<String>(Arrays.asList(words));
                        list.remove(0);
                        words = list.toArray(new String[0]);
                        staffList.setText(StringUtil.join(words, "\n"));
                        if (Count > 0) {
                            progressNo.setText(Count + " Staffs Remaining");
                        } else {
                            progressNo.setText("Evaluation Successfully Completed!");
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}


