package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.app.LogOutTimerUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaffEvaluation extends AppCompatActivity implements LogOutTimerUtil.LogOutListener {

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
    private List<String> hreffs = new ArrayList<>();

    private final String userAgent = "Firefox";

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    String evalValues[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_evaluation);

        progressNo = (TextView) findViewById(R.id.progressNo);
        String jsonCookies = getIntent().getStringExtra("COOKIES");

        Toolbar toolbar = findViewById(R.id.toolbarEvaluation);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Staff Evaluation");

        loginCookies = new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {
        }.getType());

        submit = (Button) findViewById(R.id.submit);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        progressBar.setVisibility(View.INVISIBLE);
        new submitEvaluation(this).execute();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable() && newHref.length != 0) {
                    progressBar.setVisibility(View.VISIBLE);

                    if (((RadioButton) findViewById(R.id.negative)).isChecked()) {
                        evalValues = new String[]{"3.5313", "2.0125", "1.1250", "5.8500", "0.6738", "2.5000", "1.1913", "0.7375", "1.2788", "5.0638", "1.5625"};
                    } else {
                        evalValues = new String[]{"17.7500", "14.5000", "7.5000", "7.5000", "5.8500", "4.0000", "9.2500", "4.7500", "6.8750", "6.7500", "11.5000", "11.2500"};
                    }
                    
                    try {
                        new startEvaluation(getApplicationContext(), newHref[staffno]).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(StaffEvaluation.this, "Network Problem!", Toast.LENGTH_SHORT).show();
                    progressNo.setText(R.string.network_problem);
                }
            }
        });
    }

    private boolean parsingSuccessful;

    //This function is process
    private class submitEvaluation extends ExceptionHandlingAsyncTask<String, Void, Element> {
        Document homePage = null;
        Connection.Response response;

        public submitEvaluation(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            progressBar.setVisibility(View.VISIBLE);
            submit.setClickable(false);
            //Progress bar implementations
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                response = (Jsoup.connect(getString(R.string.tkmce_home))
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_home))
                        .execute());

                homePage = response.parse();

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }

            return homePage;
        }

        @Override
        protected void onPostExecute2(Element element) {
            progressBar.setVisibility(View.GONE);

            if (parsingSuccessful && isNetworkAvailable()) {
                if (homePage.select(getString(R.string.staff_evaluation_id)).size() != 0) {
                    evalCheck = true;
                }

                if (evalCheck) {
                    Toast.makeText(StaffEvaluation.this, "You have Staff Evaluation!!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                    new getEvaluation(getApplicationContext()).execute();

                } else {
                    submit.setClickable(false);
                    progressNo.setText(R.string.no_staff_evaluation);
                    Toast.makeText(StaffEvaluation.this, "No Staff Evaluation!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(StaffEvaluation.this, "Failed retrieving data!", Toast.LENGTH_SHORT).show();
                progressNo.setText(R.string.network_problem);
            }
        }
    }

    class getEvaluation extends ExceptionHandlingAsyncTask<String, Void, Element> {
        public getEvaluation(Context context) {
            super(context);
        }

        Connection.Response loginForm = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            staffList = (TextView) findViewById(R.id.staffList);
            progressNo = (TextView) findViewById(R.id.progressNo);
            parsingSuccessful = false;
        }

        @Override
        protected Element doInBackground2(String... strings) {
            final StringBuilder builder = new StringBuilder();

            try {
                loginForm = Jsoup.connect(getString(R.string.tkmce_evaluation_url))
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .cookies(loginCookies)
                        .timeout(10 * 1000)
                        .referrer(getString(R.string.tkmce_home))
                        .execute();

                doc = loginForm.parse();
                parsingSuccessful = true;
            } catch (Exception e) {
                builder.append("Error : ").append(e.getMessage()).append("n");
                parsingSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute2(Element element) {
            if (isNetworkAvailable() && parsingSuccessful && loginForm.statusCode() == 200 && doc != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Count = 0;
                        StringBuilder staffs = new StringBuilder();
                        links = doc.select(getString(R.string.evaluation_staffs_table_id)).select("a");
                        for (Element link : links) {
                            String href = link.attr("abs:href");
                            if (!(href.equals("") || link.text().equals(""))) {
                                Count++;
                                staffs.append("").append(link.text().toUpperCase()).append("\n\n");
                            }
                        }

                        if (Count == 0) {
                            submit.setEnabled(false);
                            progressNo.setText("All done!");

                        } else {
                            staffList.setText(staffs.toString());
                            submit.setEnabled(true);
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
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    class startEvaluation extends ExceptionHandlingAsyncTask<String, Void, Element> {
        public startEvaluation(Context context, String link_href) {
            super(context);
            href = link_href;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("EvaluationPage", "Link: " + href);
            staffList = (TextView) findViewById(R.id.staffList);
            parsingSuccessful = false;
        }

        final StringBuilder builder = new StringBuilder();
        Connection.Response loginForm = null;
        Document evalData = null;

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                Connection.Response getForm = Jsoup.connect(href)
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_evaluation_url))
                        .header("Upgrade-Insecure-Requests", "1")
                        .followRedirects(false)
                        .execute();

                evalData = getForm.parse();

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
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl02$rbtn_Choice_1", evalValues[0])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl03$rbtn_Choice_2", evalValues[1])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl04$rbtn_Choice_3", evalValues[2])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl05$rbtn_Choice_4", evalValues[3])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl06$rbtn_Choice_5", evalValues[4])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl07$rbtn_Choice_6", evalValues[5])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl08$rbtn_Choice_7", evalValues[6])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl09$rbtn_Choice_8", evalValues[7])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl10$rbtn_Choice_9", evalValues[8])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl11$rbtn_Choice_10", evalValues[9])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl12$rbtn_Choice_11", evalValues[10])
                        .data("ctl00$ContentPlaceHolder1$gvQuestionsList$ctl13$txt_Choice_12", "")
                        .data("ctl00$ContentPlaceHolder1$btnSave", "Save")
                        .data("ctl00$HiddenField1", "")
                        .followRedirects(true)
                        .execute();

                parsingSuccessful = true;
            } catch (Exception e) {
                builder.append("Error : ").append(e.getMessage()).append("n");
                parsingSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute2(Element element) {
            if (isNetworkAvailable() && parsingSuccessful && loginForm.statusCode() == 200 && loginForm != null) {
                if (loginForm.url().toExternalForm().equals(getString(R.string.tkmce_evaluation_url))) {
                    staffno++;
                    new getEvaluation(getApplicationContext()).execute();
                }
            } else {
                progressNo.setText(R.string.network_problem);
            }
        }
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
        startActivity(new Intent(StaffEvaluation.this, SplashLoading.class));
        finish();
    }

}


