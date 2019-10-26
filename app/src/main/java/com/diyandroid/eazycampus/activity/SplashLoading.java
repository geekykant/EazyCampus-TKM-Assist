package com.diyandroid.eazycampus.activity;

import android.app.AlertDialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


public class SplashLoading extends AppCompatActivity {
    private SharedPreferences pref;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private String USR_NAME, PASS_NAME;
    private boolean CHK_EVAL;

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

        mFirebaseRemoteConfig.fetch(60 * 25)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            if (!checkForUpdate()) {
                                pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                boolean FIRST_TIME = pref.getBoolean("FIRST_RUN", true);
                                if (!FIRST_TIME) {
                                    USR_NAME = pref.getString("username", null);
                                    PASS_NAME = pref.getString("password", null);

                                    CHK_EVAL = pref.getBoolean("skipEvaluation", false);

                                    if (!TextUtils.isEmpty(USR_NAME) && !TextUtils.isEmpty(PASS_NAME)) {
                                        new getWebsite(getApplicationContext()).execute();
                                    }
                                } else {
                                    startActivity(new Intent(SplashLoading.this, LoginPage.class));
                                    finish();
                                }
                            }
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

                Document parsed = getName.parse();

                if (homePage != null && CHK_EVAL) {
                    LoginName = "Paavam";
                } else if (parsed.getElementById("userName") != null) {
                    LoginName = parsed.getElementById("userName").val().split(" ")[0];
                }

                Log.e("gummi", "Before: " + LoginName);

                if (TextUtils.isEmpty(LoginName)) {
                    try {
                        Log.e("gummi", "Noww4: " + parsed.getElementsByTag("tr").get(5).getElementsByTag("td").get(1));
                        LoginName = parsed.getElementsByTag("tr").get(5).getElementsByTag("td").get(1).text().split(" ")[0];
                    } catch (NullPointerException ex) {
                        Log.e("gummi", "doInBackground2: Null expception in LoginPage");
                    }
                }

                Log.i("gummi", "After: " + LoginName);

            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            }

            if (!TextUtils.isEmpty(LoginName)) {
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

    private int getCurrentVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean checkForUpdate() {
        int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble("VERSION_CODE_KEY");

        Log.d("SplashLoading", "LatestAppVersion: " + latestAppVersion + " currentversion: " + getCurrentVersionCode());

        if (latestAppVersion > getCurrentVersionCode()) {

            View adsDialogueView = LayoutInflater.from(this).inflate(R.layout.ads_dialoguebox, null);
            ((TextView) adsDialogueView.findViewById(R.id.ads_description)).setText(R.string.update_description);
            final AlertDialog adsDialogue = new AlertDialog.Builder(this).create();
            adsDialogue.setCanceledOnTouchOutside(false);
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
