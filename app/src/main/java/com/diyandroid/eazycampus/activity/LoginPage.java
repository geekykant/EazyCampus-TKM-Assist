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
import android.util.Base64;
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
import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username, password, captcha;
    private Button login_button;
    private ProgressBar progressBar;
    private CheckBox checkBox;
    ImageView captchaImage;
    private WebView mwebView;

    private String loginName = "Godaddy", jsonCookie;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        captchaImage = (ImageView) findViewById(R.id.captchaImage);

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

        mwebView.loadUrl(getString(R.string.tkmce_index_url));

        mwebView.setWebViewClient(new MyWebviewClient());
        mwebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (url.equals(getString(R.string.tkmce_index_url)) || url.equals("http://210.212.227.210/tkmce/")) {

                    Toast.makeText(LoginPage.this, "length: " + message.length(), Toast.LENGTH_SHORT).show();

                    byte[] imageByteArray = Base64.decode(message, Base64.DEFAULT);
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(imageByteArray)
                            .into(captchaImage);

//                    byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//
//                    Glide.with(getApplicationContext())
//                            .asBitmap()
//                            .load(bitmap)
//                            .apply(new RequestOptions()
//                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                    .skipMemoryCache(true))
//                            .into(captchaImage);

                    captchaImage.setVisibility(View.VISIBLE);

                    if (message.contains("Another Active Logged")) {
                        mwebView.loadUrl(getString(R.string.tkmce_index_url));
                        Toast.makeText(LoginPage.this, "Message: " + message, Toast.LENGTH_SHORT).show();
                    }
                }

                if (url.equals(getString(R.string.tkmce_home))) {
                    loginName = message.replace("WELCOME", "");

                    Intent intent = new Intent(LoginPage.this, HomePage.class);
                    intent.putExtra("COOKIES", jsonCookie);  //send cookies
                    intent.putExtra("LOGIN_NAME", loginName);
                    intent.putExtra("USER_ID", username.getText().toString().trim());
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

            mwebView.loadUrl("javascript:function getBase64Image(img) " +
                    "{ var canvas = document.createElement(\"canvas\"); canvas.width = img.width; " +
                    "canvas.height = img.height; var ctx = canvas.getContext(\"2d\"); ctx.drawImage(img, 0, 0); " +
                    "var dataURL = canvas.toDataURL(\"image/png\"); return dataURL.replace(/^data:image\\/(png|jpg);base64,/, \"\"); }" +
                    " alert(getBase64Image(document.getElementById(\"ImgCaptcha\")))");

            if (url.equals(getString(R.string.tkmce_home))) {
                mwebView.loadUrl("javascript:(function() { alert(" +
                        "\'WELCOME\' + document.getElementById('ctl00_lblFirstName').innerHTML);" +
                        "})()");
            }

            progressBar.setVisibility(View.GONE);
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
                                "document.getElementById('txtUserName').value = '" + username.getText() + "';" +
                                "document.getElementById('txtPassword').value = '" + password.getText() + "';" +
                                "document.getElementById('txtInput').value = '" + captcha.getText() + "';" +
                                "document.getElementById('btnLogin').click()" +
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
