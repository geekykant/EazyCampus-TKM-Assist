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
import android.util.Log;

import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplashLoading extends AppCompatActivity {
    private SharedPreferences pref;

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
                        new getWebsite(getApplicationContext()).execute();
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
        private Connection.Response homePage = null;

        String LOGIN_USERNAME = pref.getString("username", null);
        String LOGIN_PASSWORD = pref.getString("password", null);

        public getWebsite(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Progress bar implementations
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                final String userAgent = "Firefox";

                Connection.Response loginForm = Jsoup.connect(getString(R.string.tkmce_index_url))
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .execute();

                Log.d("SplashScreen", "Scraped Login Page!");

                Document loginPage = loginForm.parse(); //Same
                loginCookies = loginForm.cookies(); //Grabs all cookies

                homePage = Jsoup.connect(getString(R.string.tkmce_index_url))
                        .data("__LASTFOCUS", "")
                        .data("__EVENTTARGET", "")
                        .data("__EVENTARGUMENT", "")
                        .data("__VIEWSTATEGENERATOR", loginPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__VIEWSTATE", loginPage.getElementById("__VIEWSTATE").val())
                        .data("__EVENTVALIDATION", loginPage.getElementById("__EVENTVALIDATION").val())
                        .data("hdnstatus", "0")
                        .data("hdnstatus0", "0")
                        .data("txtUserName", LOGIN_USERNAME)
                        .data("txtPassword", LOGIN_PASSWORD)
                        .data("btnLogin", "Login")
                        .userAgent(userAgent)
                        .followRedirects(true)
                        .referrer(getString(R.string.tkmce_index_url))
                        .cookies(loginCookies)
                        .method(Connection.Method.POST)
                        .timeout(120 * 1000)
                        .execute();

                LoginName = homePage.parse().select("span#ctl00_lblFirstName").text();
                // Evaluator = homePage.parse().select("table#ctl00_ContentPlaceHolder1_dlAlertLIst_dlAlertDisplay").toString();

                parsingSuccessful = true;

            } catch (IOException e) {
                e.printStackTrace();
                parsingSuccessful = false;
            }

            if (homePage.url().toExternalForm().equals(getString(R.string.tkmce_home))) {
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
                String jsonCookie = new Gson().toJson(loginCookies);

                Intent intent = new Intent(SplashLoading.this, HomePage.class);
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


//    private class getWebsite extends AsyncTask<Void, Void, Void> {
//
//        private boolean loginCheck = false;
//        private Map<String, String> loginCookies = new HashMap<>();
//        private String LoginName = "";
//        private Connection.Response homePage = null;
//
//        String LOGIN_USERNAME = pref.getString("username", null);
//        String LOGIN_PASSWORD = pref.getString("password", null);
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            //Progress bar implementations
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                final String userAgent = "Firefox";
//
//                Connection.Response loginForm = Jsoup.connect(getString(R.string.tkmce_index_url))
//                        .method(Connection.Method.GET)
//                        .userAgent(userAgent)
//                        .execute();
//
//                Log.d("SplashScreen", "Scraped Login Page!");
//
//                Document loginPage = loginForm.parse(); //Same
//                loginCookies = loginForm.cookies(); //Grabs all cookies
//
//                homePage = Jsoup.connect(getString(R.string.tkmce_index_url))
//                        .data("__LASTFOCUS", "")
//                        .data("__EVENTTARGET", "")
//                        .data("__EVENTARGUMENT", "")
//                        .data("__VIEWSTATEGENERATOR", loginPage.getElementById("__VIEWSTATEGENERATOR").val())
//                        .data("__VIEWSTATE", loginPage.getElementById("__VIEWSTATE").val())
//                        .data("__EVENTVALIDATION", loginPage.getElementById("__EVENTVALIDATION").val())
//                        .data("hdnstatus", "0")
//                        .data("hdnstatus0", "0")
//                        .data("txtUserName", LOGIN_USERNAME)
//                        .data("txtPassword", LOGIN_PASSWORD)
//                        .data("btnLogin", "Login")
//                        .userAgent(userAgent)
//                        .followRedirects(true)
//                        .referrer(getString(R.string.tkmce_index_url))
//                        .cookies(loginCookies)
//                        .method(Connection.Method.POST)
//                        .timeout(120 * 1000)
//                        .execute();
//
//                LoginName = homePage.parse().select("span#ctl00_lblFirstName").text();
//                // Evaluator = homePage.parse().select("table#ctl00_ContentPlaceHolder1_dlAlertLIst_dlAlertDisplay").toString();
//
//                parsingSuccessful = true;
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                parsingSuccessful = false;
//            }
//
//            if (homePage.url().toExternalForm().equals(getString(R.string.tkmce_home))) {
//                loginCheck = true;
//                Log.d("FacultyDirectory", "Logged In!");
//            } else {
//                Log.d("FacultyDirectory", "Not Logged In!");
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            if (loginCheck && parsingSuccessful) {
//                String jsonCookie = new Gson().toJson(loginCookies);
//
//                Intent intent = new Intent(SplashLoading.this, HomePage.class);
//                intent.putExtra("COOKIES", jsonCookie);  //send cookies
//                intent.putExtra("LOGIN_NAME", LoginName);
//                startActivity(intent);
//                finish();
//
//                overridePendingTransition(R.anim.load_up_anim, 0);
//
//            } else {
//                startActivity(new Intent(SplashLoading.this, LoginPage.class));
//                finish();
//            }
//        }
//
//    }

}
