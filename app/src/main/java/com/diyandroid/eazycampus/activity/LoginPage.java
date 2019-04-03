package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    private EditText username, password, captcha;
    private ProgressBar progressBar;
    private CheckBox checkBox;

    Button login_button;

    private String loginName = "Godaddy", jsonCookie;

    public static final String VERSION_CODE_KEY = "VERSION_CODE_KEY";

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private int getCurrentVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
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

        mFirebaseRemoteConfig.fetch(60 * 60)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            checkForUpdate();
                        } else {
                            Toast.makeText(LoginPage.this, "Some problem occurred!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//
//        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        login_button = (Button) findViewById(R.id.login_button);
//
//        //WebView
//        mwebView = (WebView) findViewById(R.id.webView);
//        WebSettings webSettings = mwebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        //improve webView performance
//        mwebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        mwebView.getSettings().setAppCacheEnabled(false);
//        mwebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//        webSettings.setUseWideViewPort(true);
//
//        webSettings.setUserAgentString("Mozilla");
//
//        if (isNetworkAvailable()) {
//            mwebView.loadUrl(getString(R.string.tkmce_index_url));
//        } else {
//            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
//        }
//
//        ((ImageView) findViewById(R.id.captchaReload)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mwebView.loadUrl(getString(R.string.tkmce_index_url));
//            }
//        });
//
//        mwebView.setWebViewClient(new MyWebviewClient());
//        mwebView.setWebChromeClient(new WebChromeClient() {
//
//            @Override
//            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                if (url.equals(getString(R.string.tkmce_index_url)) || url.equals(getString(R.string.tkmce_slash))) {
//
//                    byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//
//                    if (isNetworkAvailable()) {
//                        Glide.with(getApplicationContext())
//                                .asBitmap()
//                                .transition(new BitmapTransitionOptions().crossFade())
//                                .load(bitmap)
//                                .apply(new RequestOptions()
//                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                        .skipMemoryCache(true))
//                                .into(captchaImage);
//
//                        captchaImage.setVisibility(View.VISIBLE);
//                    } else {
//                        Toast.makeText(LoginPage.this, "Network problem! Unable to load captcha.", Toast.LENGTH_LONG).show();
//                    }
//
//                    if (message.contains("Another Active Logged")) {
////                        mwebView.loadUrl(getString(R.string.tkmce_index_url));
//                        captcha.setText("");
//                        Toast.makeText(LoginPage.this, "Message: " + message, Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                if (url.equals(getString(R.string.tkmce_home))) {
//
//                    if (message.contains("only view reports")) {
//                        result.confirm();
//                        return true;
//                    }
//
//                    loginName = message.replace("WELCOME", "");
//
//                    Intent intent = new Intent(LoginPage.this, HomePage.class);
//                    intent.putExtra("COOKIES", jsonCookie);  //send cookies
//                    intent.putExtra("LOGIN_NAME", loginName);
//                    startActivity(intent);
//                    finish();
//
//                    if (checkBox.isChecked()) {
//                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                        pref.edit().putString("username", username.getText().toString().trim())
//                                .putString("password", password.getText().toString().trim()).apply();
//                    }
//                }
//                result.confirm();
//                return true;
//            }
//
//        });
//
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
//        captcha = (EditText) findViewById(R.id.captchaInput);

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

    private class getWebsite extends ExceptionHandlingAsyncTask<String, Void, Element> {

        private boolean loginCheck = false;
        private Map<String, String> loginCookies = new HashMap<>();
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

                Connection.Response loginForm = Jsoup.connect("https://tkmce.linways.com/student/index.php")
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .execute();

                Log.d("FacultyDirectory", "Scraped Login Page!");

//                Document loginPage = loginForm.parse(); //Same
                loginCookies = loginForm.cookies(); //Grabs all cookies

                homePage = Jsoup.connect("https://tkmce.linways.com/student/index.php")
                        .data("studentAccount", "170907")
                        .data("studentPassword", "170907")
                        .data("btnLogin", "Login")
                        .userAgent(userAgent)
                        .followRedirects(true)
                        .referrer("https://tkmce.linways.com/student/index.php")
                        .cookies(loginCookies)
                        .method(Connection.Method.POST)
                        .timeout(120 * 1000)
                        .execute();

                LoginName = "Dummy";
//                LoginName = homePage.parse().select("div.panel-heading(1)").html();

            } catch (IOException | RuntimeException ex) {
                ex.printStackTrace();
            }

            if (homePage.statusCode() == 200) {
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

//
//    class MyWebviewClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//
//            if (url.equals(getString(R.string.tkmce_home))) {
//                String cookies = CookieManager.getInstance().getCookie(url);
//
//                Map<String, String> map = new HashMap<>();
//                map.put(cookies.split("=")[0], cookies.split("=")[1]);
//
//                jsonCookie = new Gson().toJson(map);
//            }
//
//            return super.shouldOverrideUrlLoading(view, url);
//        }
//
//        @Override
//        public void onPageFinished(WebView view, String url) {
//            super.onPageFinished(view, url);
//
//            mwebView.loadUrl(getString(R.string.get_base64_js));
//
//            if (url.equals(getString(R.string.tkmce_home))) {
//                mwebView.loadUrl(
//                        "javascript:(function() { " +
//                                "alert(document.getElementById('" + mFirebaseRemoteConfig.getString("LOGIN_FIRST_NAME") + "').innerHTML);" +
//                                "})()");
//
//            }
//
//            progressBar.setVisibility(View.GONE);
//        }

    private void checkForUpdate() {
        int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);

        Log.d("LoginActivity", "LatestAppVersion: " + latestAppVersion + " currentversion: " + getCurrentVersionCode());

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

        }
    }
//
//    private void doLogin() {
//        if (isNetworkAvailable()) {
//            if (TextUtils.isEmpty(username.getText().toString()) && TextUtils.isEmpty(password.getText().toString())) {
//                username.setError("Username incorrect");
//                password.setError("Password incorrect");
//                return;
//            }
//
//            if (TextUtils.isEmpty(captcha.getText().toString())) {
//                captcha.setError("Captcha incorrect");
//            } else {
//                mwebView.loadUrl(
//                        "javascript:(function() { " +
//                                "document.getElementById('" + mFirebaseRemoteConfig.getString("LOGIN_USERNAME_ID") + "').value = '" + username.getText() + "';" +
//                                "document.getElementById('" + mFirebaseRemoteConfig.getString("LOGIN_PASSWORD_ID") + "').value = '" + password.getText() + "';" +
//                                "document.getElementById('" + mFirebaseRemoteConfig.getString("CAPTCHA_INPUT_ID") + "').value = '" + captcha.getText() + "';" +
//                                "document.getElementById('" + mFirebaseRemoteConfig.getString("LOGIN_BTN") + "').click()" +
//                                "})()");
//                progressBar.setVisibility(View.VISIBLE);
//            }
//        } else {
//            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
//        }

    private void doLogin() {
        if (isNetworkAvailable()) {
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
}
