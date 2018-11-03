package com.diyandroid.eazycampus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.diyandroid.eazycampus.R;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

public class ReferenceActivity extends AppCompatActivity {

    //initializing WebView
    private WebView mwebView;
    private ProgressBar progressBar, firstProgress;
    private FrameLayout frameLayout;
    private Boolean isCalendar;

    private SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        progressBar = (ProgressBar) findViewById(R.id.progressBarNotes);
        firstProgress = (ProgressBar) findViewById(R.id.firstProgressNotes);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayoutNotes);

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


        final String url = getIntent().getStringExtra("INTENT_URL");
        isCalendar = getIntent().getBooleanExtra("IS_CALENDAR", false);

        mwebView.loadUrl(url);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mwebView.loadUrl(url);
                pullToRefresh.setRefreshing(false);
            }
        });

        //force links open in webview only
        mwebView.setWebViewClient(new MyWebviewClient());
        mwebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);

                if (newProgress == 100) {
                    progressBar.setProgress(0);
                    firstProgress.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    private class MyWebviewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Toast.makeText(ReferenceActivity.this, url + " !!!", Toast.LENGTH_LONG).show();

            if (url.equals("http://210.212.227.210/tkmce/Common/Home/Home.aspx")) {

                String[] cookies = CookieManager.getInstance().getCookie(url).split("=");

                Map<String, String> loginCookies = null;
                loginCookies.put(cookies[0], cookies[1]);

                Log.d("Blah", "Point 1: " + loginCookies);

                JSONObject jsonObject = new JSONObject(loginCookies);
                String jsonCookie = new Gson().toJson(jsonObject);

//                String jsonCookie = "{" + cookies[0] + ":" + cookies[1] + "}";
                Toast.makeText(ReferenceActivity.this, "Cookies: " + jsonCookie, Toast.LENGTH_SHORT).show();

                Log.d("Blah", "Point 2: " + jsonCookie);

                Intent intent = new Intent(ReferenceActivity.this, HomePage.class);

                intent.putExtra("COOKIES", jsonCookie);  //send cookies
                intent.putExtra("LOGIN_NAME", "Manda");
                startActivity(intent);
                finish();

            }

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Toast.makeText(ReferenceActivity.this, url, Toast.LENGTH_LONG).show();

//            if (url.equals("http://210.212.227.210/tkmce/Common/Home/Home.aspx")) {
//                String[] cookies = CookieManager.getInstance().getCookie(url).split("=");
//
//                Map<String, String> loginCookies = null;
//                loginCookies.put(cookies[0], cookies[1]);
////
//                Log.d("Blah", "Point 1: " + loginCookies);
//
//                JSONObject jsonObject = new JSONObject(loginCookies);
//                String jsonCookie = new Gson().toJson(jsonObject);
//
////                String jsonCookie = "{" + cookies[0] + ":" + cookies[1] + "}";
//                Toast.makeText(ReferenceActivity.this, "Cookies: " + jsonCookie, Toast.LENGTH_SHORT).show();
//
//                Log.d("Blah", "Point 2: " + jsonCookie);
//
//                Intent intent = new Intent(ReferenceActivity.this, HomePage.class);
//
//                intent.putExtra("COOKIES", jsonCookie);  //send cookies
//                intent.putExtra("LOGIN_NAME", "Manda");
//                startActivity(intent);
//                finish();
//
//                return false;
//            }

            //        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            if (Uri.parse(url).getHost().equals(getString(R.string.ktustudy_url))) {
//                //open url contents in webview
//                return false;
//            }
//            if (Uri.parse(url).getHost().equals("http://210.212.227.210/tkmce/") ||
//                    Uri.parse(url).getHost().equals("http://210.212.227.210/tkmce/Common/Home/Home.aspx")) {
//
//                return false;
//
//            } else {
//                //here open external links in external browser or app
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(intent);
//
//                if (isCalendar) {
//                    finish();
//                }
//                return true;
//        }
//
//        }
            return false;
        }

    }

    //goto previous page when pressing back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mwebView.canGoBack()) {
                        mwebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
