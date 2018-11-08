package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username, password, captcha;
    private ProgressBar progressBar;
    private CheckBox checkBox;
    ImageView captchaImage;
    private WebView mwebView;

    Map<String, String> LOGIN_IDS;
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

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();

                            String LOGIN_ID_JSON = mFirebaseRemoteConfig.getString("LOGIN_PAGE_IDS");
                            Toast.makeText(LoginPage.this, "Dude!! its " + LOGIN_ID_JSON, Toast.LENGTH_SHORT).show();

                            Log.d("LoginPage", "Fetched value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY));
                            checkForUpdate();

                            LOGIN_IDS = new Gson().fromJson(LOGIN_ID_JSON, new TypeToken<Map<String, String>>() {
                            }.getType());
                        } else {
                            Toast.makeText(LoginPage.this, "Some problem occurred!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        Button login_button = (Button) findViewById(R.id.login_button);

        //WebView
        mwebView = (WebView) findViewById(R.id.webViewNotes);
        WebSettings webSettings = mwebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //improve webView performance
        mwebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mwebView.getSettings().setAppCacheEnabled(false);
        mwebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);

        if (isNetworkAvailable()) {
            mwebView.loadUrl(getString(R.string.tkmce_index_url));
        } else {
            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }

        ((ImageView) findViewById(R.id.captchaReload)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mwebView.loadUrl(getString(R.string.tkmce_index_url));
            }
        });

        mwebView.setWebViewClient(new MyWebviewClient());
        mwebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (url.equals(getString(R.string.tkmce_index_url)) || url.equals("http://210.212.227.210/tkmce/")) {

                    byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    if (isNetworkAvailable()) {
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .transition(new BitmapTransitionOptions().crossFade())
                                .load(bitmap)
                                .apply(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true))
                                .into(captchaImage);

                        captchaImage.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(LoginPage.this, "Network problem! Unable to load captcha.", Toast.LENGTH_LONG).show();
                    }

                    if (message.contains("Another Active Logged")) {
                        mwebView.loadUrl(getString(R.string.tkmce_index_url));
                        captcha.setText("");
                        Toast.makeText(LoginPage.this, "Message: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else if (url.equals(getString(R.string.tkmce_home))) {

                    if (message.contains("only view reports")) {
                        result.confirm();
                        return true;
                    }

                    loginName = message.replace("WELCOME", "");

                    Intent intent = new Intent(LoginPage.this, HomePage.class);
                    intent.putExtra("COOKIES", jsonCookie);  //send cookies
                    intent.putExtra("LOGIN_NAME", loginName);
                    startActivity(intent);
                    finish();

                    if (checkBox.isChecked()) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        pref.edit().putString("username", username.getText().toString().trim())
                                .putString("password", password.getText().toString().trim()).apply();
                    }
                }

                result.confirm();
                return true;
            }

        });

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

    class MyWebviewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.equals(getString(R.string.tkmce_home))) {
                String cookies = CookieManager.getInstance().getCookie(url);

                Map<String, String> map = new HashMap<>();
                map.put(cookies.split("=")[0], cookies.split("=")[1]);

                jsonCookie = new Gson().toJson(map);
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mwebView.loadUrl(getString(R.string.get_base64_js));

            if (url.equals(getString(R.string.tkmce_home))) {
                mwebView.loadUrl(getString(R.string.welcome_name_js));
            }

            progressBar.setVisibility(View.GONE);
        }

    }

    private void checkForUpdate() {
        int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);
        if (latestAppVersion > getCurrentVersionCode()) {
            new AlertDialog.Builder(this).setTitle("Please Update the App")
                    .setMessage("A new version of this app is available. Please update it").setPositiveButton(
                    "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(LoginPage.this, "Take user to Google Play Store", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }).setCancelable(false).show();
        } else {
            Toast.makeText(this, "This app is already upto date", Toast.LENGTH_SHORT).show();
        }
    }

    private void doLogin() {
        if (isNetworkAvailable()) {
            if (TextUtils.isEmpty(username.getText().toString()) && TextUtils.isEmpty(password.getText().toString())) {
                username.setError("Username incorrect");
                password.setError("Password incorrect");
                return;
            }

            if (TextUtils.isEmpty(captcha.getText().toString())) {
                captcha.setError("Captcha incorrect");
            } else {
                mwebView.loadUrl(
                        "javascript:(function() { " +
                                "document.getElementById('" + LOGIN_IDS.get("LOGIN_USERNAME_ID") + "').value = '" + username.getText() + "';" +
                                "document.getElementById('" + LOGIN_IDS.get("LOGIN_PASSWORD_ID") + "').value = '" + password.getText() + "';" +
                                "document.getElementById('" + LOGIN_IDS.get("CAPTCHA_INPUT_ID") + "').value = '" + captcha.getText() + "';" +
                                "document.getElementById('" + LOGIN_IDS.get("LOGIN_BTN") + "').click()" +
                                "})()");
                progressBar.setVisibility(View.VISIBLE);
            }
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
