package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplashLoading extends AppCompatActivity {
    private SharedPreferences pref;

    String USR_NAME, PASS_NAME;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_loading);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(500);  //Delay of 1 seconds
                } catch (Exception e) {
                } finally {
                    pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    boolean FIRST_TIME = pref.getBoolean("FIRST_RUN", true);
                    if (!FIRST_TIME) {
                        USR_NAME = pref.getString("username", null);
                        PASS_NAME = pref.getString("password", null);

                        if (!TextUtils.isEmpty(USR_NAME) && !TextUtils.isEmpty(PASS_NAME)) {
                            new getWebsite(getApplicationContext()).execute();
                        }
                    } else {
                        startActivity(new Intent(SplashLoading.this, LoginPage.class));
                        finish();
                    }
                }
            }
        };

        //Force closing app after Exception to LoginPage Close..
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        if (isNetworkAvailable()) {
            welcomeThread.start();

            DilatingDotsProgressBar progress = (DilatingDotsProgressBar) findViewById(R.id.progress);
            progress.showNow();

        } else {
            startActivity(new Intent(SplashLoading.this, LoginPage.class));
            finish();
        }
    }

    private boolean parsingSuccessful;

    private class getWebsite extends ExceptionHandlingAsyncTask<String, Void, Element> {

        private boolean loginCheck = false;
        private Map<String, String> loginCookies = new HashMap<>();
        private String LoginName = "";
        private Connection.Response homePage, getName;

        public getWebsite(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            //Progress bar implementations
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                final String userAgent = "Firefox";

                Connection.Response loginForm = Jsoup.connect("https://tkmce.linways.com/student/index.php")
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .execute();

                Log.d("FacultyDirectory", "Scraped Login Page!");

//                Document loginPage = loginForm.parse(); //Same
                loginCookies = loginForm.cookies(); //Grabs all cookies

                homePage = Jsoup.connect("https://tkmce.linways.com/student/index.php")
                        .data("studentAccount", USR_NAME)
                        .data("studentPassword", PASS_NAME)
                        .data("btnLogin", "Login")
                        .userAgent(userAgent)
                        .followRedirects(true)
                        .referrer("https://tkmce.linways.com/student/index.php")
                        .cookies(loginCookies)
                        .method(Connection.Method.POST)
                        .timeout(120 * 1000)
                        .execute();

                getName = Jsoup.connect("https://tkmce.linways.com/student/student.php?menu=student_details&action=frm_edit")
                        .userAgent(userAgent)
                        .referrer("https://tkmce.linways.com/student/index.php")
                        .cookies(loginCookies)
                        .followRedirects(true)
                        .method(Connection.Method.GET)
                        .timeout(120 * 1000)
                        .execute();

//                LoginName = getName.select("table").first().select("tr").get(4).select("td").get(1).text();
                LoginName = getName.parse().select("table").first().select("tr").get(5).select("td").get(1).text().split(" ")[0];
                Log.i("gummi", "doInBackground2: " + LoginName);

            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }


            if (!TextUtils.isEmpty(LoginName)) {
                loginCheck = true;
                Log.d("FacultyDirectory", "Logged In!");
            } else {
                Log.d("FacultyDirectory", "Not Logged In!");
            }

            return null;
        }

        @Override
        protected void onPostExecute2(Element element) {
            if (loginCheck && parsingSuccessful) {
                Intent intent = new Intent(SplashLoading.this, HomePage.class);

                String jsonCookie = new Gson().toJson(loginCookies);

                intent.putExtra("COOKIES", jsonCookie);  //send cookies
                intent.putExtra("LOGIN_NAME", LoginName);
                startActivity(intent);
                finish();

                overridePendingTransition(R.anim.load_up_anim, 0);

            } else {
                startActivity(new Intent(SplashLoading.this, LoginPage.class));
                finish();
            }
        }
    }
}
