package com.diyandroid.eazycampus.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.helper.LoginHelper;
import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;
import com.diyandroid.eazycampus.util.TokenUser;
import com.diyandroid.eazycampus.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.ArrayList;


public class SplashLoading extends AppCompatActivity implements LoginHelper.LoginListener {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private TokenUser tokenUser;
    private LoginHelper loginHelper;

    private DilatingDotsProgressBar progress;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_loading);

        progress = findViewById(R.id.progress);
        progress.showNow();

        tokenUser = new TokenUser(this);
        loginHelper = new LoginHelper(this);

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
                                if (tokenUser.isFirstTime() || !isNetworkAvailable()) {
                                    onLoginFailed(null, false);
                                    return;
                                }

                                User user = tokenUser.getUser();
                                loginHelper.doLogin(user);
                            }
                        } else {
                            Toast.makeText(SplashLoading.this, "Restart app with Internet ON to work!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onLoginSuccessful(User user, ArrayList<SubjectAttendance> attendanceList) {
        Intent intent = new Intent(SplashLoading.this, HomePage.class);
        intent.putExtra("ATTENDANCE_LIST", Utils.getGsonParser().toJson(attendanceList));

        Log.d("k", "onLoginSuccessful: " + attendanceList);

        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.load_up_anim, 0);
    }

    @Override
    public void onLoginFailed(String message, boolean show_error) {
//        if (TextUtils.isEmpty(message)) {
//            return;
//        }

        progress.hide();

        startActivity(new Intent(SplashLoading.this, LoginPage.class));
        finish();
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
            adsDialogueView.findViewById(R.id.contribute).setVisibility(View.GONE);
            Button update = adsDialogueView.findViewById(R.id.later);
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
