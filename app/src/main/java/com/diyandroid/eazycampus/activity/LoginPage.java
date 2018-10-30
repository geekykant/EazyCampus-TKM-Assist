package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username, password, captcha;
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

        new getCaptcha(this).execute();

        login_button = (Button) findViewById(R.id.login_button);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        captcha = (EditText) findViewById(R.id.captchaInput);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        LinearLayout layout = findViewById(R.id.rememberme);
        checkBox = (CheckBox) findViewById(R.id.rememberCheck);

        ((TextView) findViewById(R.id.privacy_policy)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.privacy_policy_link)));
                startActivity(i);
            }
        });

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

    private void doLogin() {
        if (isNetworkAvailable()) {
            login_button.setClickable(false);
            username.setEnabled(false);
            password.setEnabled(false);
            captcha.setEnabled(false);
            new getWebsite(this).execute();
        } else {
            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String USR_NAME = pref.getString("username", null);
        final String PASS_NAME = pref.getString("password", null);

        if (!TextUtils.isEmpty(USR_NAME) && !TextUtils.isEmpty(PASS_NAME)) {
            //Prompt for username and password
            username.setText(USR_NAME);
            password.setText(PASS_NAME);
            ((TextInputLayout) findViewById(R.id.passbox)).setPasswordVisibilityToggleEnabled(false);
//            doLogin();
        }
    }

    private Document loginPage;
    private Map<String, String> loginCookies;

    private boolean captchaGet = false;

    private class getCaptcha extends ExceptionHandlingAsyncTask<String, Void, Element> {

        private Connection.Response homePage;

        public getCaptcha(Context context) {
            super(context);
        }

        ImageView captchaImage = (ImageView) findViewById(R.id.captchaImage);

        String captchaHTML;
        Bitmap bmp;

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

                Log.d("FacultyDirectory", "Scraped Login Page!");

                loginPage = loginForm.parse(); //Same
                loginCookies = loginForm.cookies(); //Grabs all cookies

                captchaHTML = loginPage.getElementById("ImgCaptcha").attr("abs:src");

                captchaGet = true;
            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
                captchaGet = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute2(Element element) {

            Toast.makeText(LoginPage.this, "Captcha: " + captchaHTML, Toast.LENGTH_SHORT).show();

            if (captchaGet) {

//                byte[] data = captchaHTML.html().getBytes();
//                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                captchaImage.setImageBitmap(bmp);

                Log.d("MainActivity", "captch: ");

                Glide.with(getApplicationContext())
                        .load(bmp)
                        .apply(new RequestOptions().placeholder(R.drawable.error_faculty))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(captchaImage);
            }
        }
    }


    private class getWebsite extends ExceptionHandlingAsyncTask<String, Void, Element> {
        private boolean loginCheck = false;
        private String LoginName = "";
        private Connection.Response homePage;

        public getWebsite(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Progress bar implementations
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                final String userAgent = "Firefox";

                homePage = Jsoup.connect(getString(R.string.tkmce_index_url))
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
                        .data("txtInput", captcha.getText().toString().trim())
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

            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
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
            progressBar.setVisibility(View.GONE);
            login_button.setClickable(true);
            username.setEnabled(true);
            password.setEnabled(true);
            captcha.setEnabled(true);

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
                captcha.setError("Captcha incorrect");

                new getCaptcha(getApplicationContext()).execute();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
//        super.onBackPressed();
//        Toast.makeText(this, "Hello!", Toast.LENGTH_SHORT).show();
    }
}
