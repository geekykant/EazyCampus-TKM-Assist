package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username, password;
    private Button login_button;
    private ProgressBar progressBar;
    private CheckBox checkBox;

    //Shared preferences to remember credentials
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        login_button = (Button) findViewById(R.id.login_button);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        LinearLayout layout = findViewById(R.id.rememberme);
        checkBox = (CheckBox) findViewById(R.id.rememberCheck);

        //Listener for Remember Me
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });
    }

    private void doLogin(){
        if (isNetworkAvailable()) {
            login_button.setClickable(false);
            username.setEnabled(false);
            password.setEnabled(false);
            new getWebsite().execute();
        } else {
            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this); //getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String USR_NAME = pref.getString("username", null);
        final String PASS_NAME = pref.getString("password", null);

        if (!TextUtils.isEmpty(USR_NAME) && !TextUtils.isEmpty(PASS_NAME)){
            //Prompt for username and password
            username.setText(USR_NAME);
            password.setText(PASS_NAME);
            doLogin();
        }
    }

    public class getWebsite extends AsyncTask<Void, Void, Void> {

        boolean loginCheck = false;
        Map<String, String> loginCookies = new HashMap<>();
        String LoginName = "";
        Connection.Response homePage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Progress bar implementations
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                final String urlLogin = "http://210.212.227.210/tkmce/index.aspx";
                final String userAgent = "Firefox";

                Connection.Response loginForm = Jsoup.connect(urlLogin)
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .execute();

                Log.d("FacultyDirectory", "Scraped Login Page!");

                Document loginPage = loginForm.parse(); //Same
                loginCookies = loginForm.cookies(); //Grabs all cookies

                homePage = Jsoup.connect(urlLogin)
                        .data("__LASTFOCUS", "")
                        .data("__EVENTTARGET", "")
                        .data("__EVENTARGUMENT", "")
                        .data("__VIEWSTATEGENERATOR", loginPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__VIEWSTATE", loginPage.getElementById("__VIEWSTATE").val())
                        .data("__EVENTVALIDATION", loginPage.getElementById("__EVENTVALIDATION").val())
                        .data("hdnstatus", "0")
                        .data("hdnstatus0", "0")
                        .data("txtUserName", username.getText().toString().trim())
                        .data("txtPassword", password.getText().toString().trim())
                        .data("btnLogin", "Login")
                        .userAgent(userAgent)
                        .followRedirects(true)
                        .referrer("http://210.212.227.210/tkmce/index.aspx")
                        .cookies(loginCookies)
                        .method(Connection.Method.POST)
                        .timeout(120 * 1000)
                        .execute();

                LoginName = homePage.parse().select("span#ctl00_lblFirstName").text();
                // Evaluator = homePage.parse().select("table#ctl00_ContentPlaceHolder1_dlAlertLIst_dlAlertDisplay").toString();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (homePage.url().toExternalForm().equals("http://210.212.227.210/tkmce/Common/Home/Home.aspx")) {
                loginCheck = true;
                Log.d("FacultyDirectory", "Logged In!");
            } else {
                Log.d("FacultyDirectory", "Not Logged In!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            login_button.setClickable(true);
            username.setEnabled(true);
            password.setEnabled(true);

            if (loginCheck) {
                Intent intent = new Intent(LoginPage.this, HomePage.class);

                String jsonCookie = new Gson().toJson(loginCookies);

                if (checkBox.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(PREF_USERNAME, username.getText().toString())
                            .putString(PREF_PASSWORD, password.getText().toString())
                            .apply();

                }

                intent.putExtra("COOKIES", jsonCookie);  //send cookies
                intent.putExtra("LOGIN_NAME", LoginName);
                startActivity(intent);
                finish();

                overridePendingTransition(R.anim.load_up_anim, 0);

            } else {
                username.setError("Username incorrect");
                password.setError("Password incorrect");
            }
        }
    }
}
