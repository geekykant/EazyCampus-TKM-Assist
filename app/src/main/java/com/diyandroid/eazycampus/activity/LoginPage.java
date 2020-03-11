package com.diyandroid.eazycampus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.helper.LoginHelper;
import com.diyandroid.eazycampus.helper.ProgressDialog;
import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;
import com.diyandroid.eazycampus.util.TokenUser;
import com.diyandroid.eazycampus.util.Utils;

import java.util.ArrayList;

public class LoginPage extends AppCompatActivity implements LoginHelper.LoginListener {

    private EditText username, password;
    private CheckBox remember;
    private Button login_button;

    private TokenUser tokenUser;
    private LoginHelper loginHelper;
    private ProgressDialog dialogLoad;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        tokenUser = new TokenUser(this);

        loginHelper = new LoginHelper(this);

        init();

        LinearLayout layout1 = findViewById(R.id.rememberme);
        findViewById(R.id.privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.privacy_policy_link)));
                startActivity(i);
            }
        });

        //Listener for Remember Me
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remember.setChecked(!remember.isChecked());
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });
    }

    private void init() {
        login_button = findViewById(R.id.login_button);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        remember = findViewById(R.id.rememberCheck);
    }

    private void doLogin() {
        if (isNetworkAvailable()) {
            if (TextUtils.isEmpty(username.getText()) || TextUtils.isEmpty(password.getText())) {
                username.setError("Username incorrect");
                password.setError("Password incorrect");
                return;
            }

            if (dialogLoad != null) dialogLoad.dismiss();
            dialogLoad = new ProgressDialog();
            dialogLoad.show(getSupportFragmentManager(), "");

            User user = new User(Integer.parseInt(username.getText().toString()), password.getText().toString());
            System.out.println(user);

            loginHelper.doLogin(user);

            login_button.setClickable(false);
            username.setEnabled(false);
            password.setEnabled(false);
        } else {
            Toast.makeText(LoginPage.this, "No Internet Connection!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        tokenUser = new TokenUser(this);
//
//        int USR_NAME = tokenUser.getPrefUsername();
//        String PASS_NAME = tokenUser.getPrefPassword();
//
//        if (USR_NAME != -1 && !TextUtils.isEmpty(PASS_NAME)) {
//            //Prompt for username and password
//            username.setText(USR_NAME);
//            password.setText(PASS_NAME);
//            ((TextInputLayout) findViewById(R.id.passbox)).setPasswordVisibilityToggleEnabled(false);
//        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
//        super.onBackPressed();
    }

    private static final String TAG = LoginPage.class.getSimpleName();

    @Override
    public void onLoginSuccessful(User user, ArrayList<SubjectAttendance> attendanceList) {
        if (remember.isChecked()) {
            tokenUser.storeToken(user, Integer.parseInt(username.getText().toString()), password.getText().toString());
        }

        Intent intent = new Intent(LoginPage.this, HomePage.class);
        intent.putExtra("ATTENDANCE_LIST", Utils.getGsonParser().toJson(attendanceList));

        tokenUser.setFirstTime(false);

        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.load_up_anim, 0);
    }

    @Override
    public void onLoginFailed(String message, boolean show_error) {
        dialogLoad.dismiss();

        login_button.setClickable(true);
        username.setEnabled(true);
        password.setEnabled(true);

        if (show_error) {
            username.setError("Username incorrect");
            password.setError("Password incorrect");
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
