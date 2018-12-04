package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.diyandroid.eazycampus.activity.LoginPage.VERSION_CODE_KEY;

public class SplashLoading extends AppCompatActivity {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences pref;

    private boolean FIRST_TIME;

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

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FIRST_TIME = pref.getBoolean("FIRST_RUN", true);

        mFirebaseRemoteConfig.fetch(60 * 25)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            checkForCaptcha();
                        } else {
                            Toast.makeText(SplashLoading.this, "Please restart app to work!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //Force closing app after Exception to LoginPage Close..
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        if (isNetworkAvailable()) {
//            welcomeThread.start();

            DilatingDotsProgressBar progress = (DilatingDotsProgressBar) findViewById(R.id.progress);
            progress.showNow();
        } else {
            startActivity(new Intent(SplashLoading.this, LoginPage.class));
            finish();
        }
    }

    private void checkForCaptcha() {
        if (!checkForUpdate()) {
            Intent intent = new Intent(getApplicationContext(), LoginPage.class);

            if (FIRST_TIME) {
                startActivity(intent);
                finish();
            } else if (mFirebaseRemoteConfig.getBoolean("SPECIAL_MODE")) {
                startActivity(intent);
                finish();
            } else if (!mFirebaseRemoteConfig.getBoolean("CAPTCHA_ENABLED")) {
                new getWebsite(getApplicationContext()).execute();
            } else {
                startActivity(intent);
                finish();
            }
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

                Log.d("SplashLoading", "Scraped Login Page!");

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
                Log.d("SplashLoading", "Logged In!");
            } else {
                Log.d("SplashLoading", "Not Logged In!");
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
                intent.putExtra("LOGIN_USERNAME", LOGIN_USERNAME);
                startActivity(intent);
                finish();

                overridePendingTransition(R.anim.load_up_anim, 0);

            } else {
                startActivity(new Intent(SplashLoading.this, LoginPage.class));
                finish();
            }
        }
    }

    private int getCurrentVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean checkForUpdate() {
        int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);

        Log.d("SplashLoading", "LatestAppVersion: " + latestAppVersion + " currentversion: " + getCurrentVersionCode());

        if (latestAppVersion > getCurrentVersionCode()) {

            View adsDialogueView = LayoutInflater.from(this).inflate(R.layout.ads_dialoguebox, null);
            ((TextView) adsDialogueView.findViewById(R.id.ads_description)).setText(R.string.update_description);
            final AlertDialog adsDialogue = new AlertDialog.Builder(this).create();
            adsDialogue.setView(adsDialogueView);

            ((TextView) adsDialogueView.findViewById(R.id.headingDialogue)).setText(R.string.update_available);
            ((Button) adsDialogueView.findViewById(R.id.contribute)).setVisibility(View.GONE);
            Button update = (Button) adsDialogueView.findViewById(R.id.later);
            update.setText(R.string.update_button_title);
            adsDialogue.show();

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            });
            return true;
        }
        return false;
    }
}
