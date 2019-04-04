package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username, password;
    private ProgressBar progressBar;
    private CheckBox checkBox;
    private Button login_button;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//        checkForUpdate();

        login_button = (Button) findViewById(R.id.login_button);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        checkBox = (CheckBox) findViewById(R.id.rememberCheck);

        LinearLayout layout = findViewById(R.id.rememberme);

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
            if (TextUtils.isEmpty(username.getText().toString()) && TextUtils.isEmpty(password.getText().toString())) {
                username.setError("Username incorrect");
                password.setError("Password incorrect");
                return;
            }

            login_button.setClickable(false);
            username.setEnabled(false);
            password.setEnabled(false);
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
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
//        super.onBackPressed();
    }


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
            //Progress bar implementations
            progressBar.setVisibility(View.VISIBLE);
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
                        .data("studentAccount", username.getText().toString())
                        .data("studentPassword", password.getText().toString())
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
                            .putString("username", username.getText().toString())
                            .putString("password", password.getText().toString())
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
