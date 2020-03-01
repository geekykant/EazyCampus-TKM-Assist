package com.diyandroid.eazycampus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.app.Config;
import com.diyandroid.eazycampus.fragment.AboutFragment;
import com.diyandroid.eazycampus.fragment.AttendanceFragment;
import com.diyandroid.eazycampus.util.TokenUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomePage";

    private NavigationView navigationView;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FragmentManager fm;

    private TokenUser tokenUser;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigationHome:
                    loadFragment(new AttendanceFragment(getApplicationContext()));
                    return true;
                case R.id.navigationDirectory:
                    startActivity(new Intent(HomePage.this, FacultyDirectory.class));
                    return false;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.showFragment, fragment);
            ft.addToBackStack(null);
            getSupportFragmentManager().popBackStack();
            ft.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_layout);

        loadFragment(new AttendanceFragment(getApplicationContext()));
        tokenUser = new TokenUser(this);
        fm = getSupportFragmentManager();

        init();

        if (tokenUser.isFirstTime()) {
            FirebaseMessaging.getInstance().subscribeToTopic("all_semesters");
            FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
        }
    }

    private void init() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigationHome);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer fancy navigation bar
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_toggle, R.string.close_toggle);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_button); //back button change
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        navigationView = findViewById(R.id.navigationview);

        //Drawer header details - RollNo, Name
        final View headView = navigationView.getHeaderView(0);
        TextView drawerName = headView.findViewById(R.id.drawerName);
        TextView drawerRollNo = headView.findViewById(R.id.drawerRollNo);

        drawerName.setText(tokenUser.getPrefLoginName());
        drawerRollNo.setText(tokenUser.getPrefRollNo());

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);
    }

    @SuppressLint("RestrictedApi")
    private void paywithUPI() {
        Uri UPI = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", "geekykant@oksbi")
                .appendQueryParameter("pn", "SREEKANT SHENOY")
                .appendQueryParameter("tn", "EazyCampus Development Support :)")
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent();
        intent.setData(UPI);
        Intent chooser = Intent.createChooser(intent, "Pay with...");
        startActivityForResult(chooser, 1337, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case android.R.id.home:
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    break;
                case R.id.app_send:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_eazycampus_text));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    return true;

                case R.id.app_notification:
                    String ktu_url = "https://ktu.edu.in/eu/core/announcements.htm";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ktu_url)));
                    break;

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
                FrameLayout layout = findViewById(R.id.showFragment);
                layout.removeAllViewsInLayout();
                break;

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.about:
                loadFragment(new AboutFragment());
                break;

            case R.id.signout:
                Intent signoutIntent = new Intent(HomePage.this, LoginPage.class);
                startActivity(signoutIntent);
                finish();

                new TokenUser(this).removeTokens();
                Toast.makeText(this, "You have logged out!", Toast.LENGTH_SHORT).show();
                break;
        }

        navigationView.setCheckedItem(id);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        //back press removes fragment layout
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            ((FrameLayout) findViewById(R.id.showFragment)).removeAllViewsInLayout();
            getSupportFragmentManager().popBackStack();
            navigationView.setCheckedItem(R.id.home);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);

        if (requestCode == 1337) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Pleaseee contribute! ;_;", Toast.LENGTH_LONG).show();
            } else {
                if (resultCode == RESULT_OK && data != null) {
                    Bundle bundle = data.getExtras();
                    String response = bundle.getString("response");
                    String responseValues[] = getKeyValueFromString(response, "&");
                    HashMap<String, String> keyValueOfResponse = new HashMap<>();
                    for (String responseValue : responseValues) {
                        String[] keyValue = getKeyValueFromString(responseValue, "=");
                        if (keyValue != null && keyValue.length > 1) {
                            keyValueOfResponse.put(keyValue[0], keyValue[1]);
                        }
                    }
                    String txnRef = keyValueOfResponse.get("txnRef");
                    String responseCode = keyValueOfResponse.get("responseCode");
                    String Status = keyValueOfResponse.get("Status");
                    String txnId = keyValueOfResponse.get("txnId");

                    View contributionView = LayoutInflater.from(this).inflate(R.layout.contribution_summary, null);

                    final AlertDialog contriDialogue = new AlertDialog.Builder(this).create();
                    contriDialogue.setCancelable(false);
                    contriDialogue.setCanceledOnTouchOutside(false);
                    contriDialogue.setView(contributionView);

                    Button tryagain = contributionView.findViewById(R.id.tryagain);
                    Button cont_later = contributionView.findViewById(R.id.cont_later);
                    CircleImageView tick = contributionView.findViewById(R.id.tick);

                    if (Status.equals("SUCCESS")) {
                        tryagain.setVisibility(View.VISIBLE);
                        ((TextView) contributionView.findViewById(R.id.contribution_title)).setText(R.string.thank_you);
                        ((TextView) contributionView.findViewById(R.id.transaction_desc)).setText(R.string.contribution_sent_message);

//                        pref.edit().putInt("FIRST_COUNT", -1).apply();
                    } else if (Status.equals("FAILURE")) {
                        tick.setImageResource(R.drawable.ic_cross);
                        ((TextView) contributionView.findViewById(R.id.contribution_title)).setText(R.string.transaction_failed);
                    }

                    contriDialogue.show();

                    tryagain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            contriDialogue.hide();
                            paywithUPI();
                        }
                    });

                    cont_later.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            contriDialogue.hide();
                        }
                    });

                    Log.d("UPI RESULT: ", txnRef + " : " + responseCode + " : " + Status + " : " + txnId);
                }
            }
        }

    }

    private String[] getKeyValueFromString(String stringToSplit, String splitBy) {
        if (stringToSplit == null) {
            return null;
        }
        return stringToSplit.split(splitBy);
    }
}
