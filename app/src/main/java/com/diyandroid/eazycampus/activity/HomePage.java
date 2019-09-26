package com.diyandroid.eazycampus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;
import com.diyandroid.eazycampus.adapter.AttendanceListAdapter;
import com.diyandroid.eazycampus.app.Config;
import com.diyandroid.eazycampus.fragment.AboutFragment;
import com.diyandroid.eazycampus.util.AttendanceGrabber;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        AttendanceGrabber.AsyncResponse {

    private static final String TAG = "HomePage";
    private SharedPreferences pref;

    private NavigationView navigationView;
    private String jsonCookies;
    public static final int OPEN_NEW_ACTIVITY = 6588;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private SwipeRefreshLayout mySwipeRefreshLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_layout);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean FIRST_RUN = pref.getBoolean("FIRST_RUN", true);

        String loginName = getIntent().getStringExtra("LOGIN_NAME");

        if (FIRST_RUN) {
            FirebaseMessaging.getInstance().subscribeToTopic("all_semesters");
            FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
            pref.edit().putBoolean("FIRST_RUN", false).apply();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_toggle, R.string.close_toggle);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_button); //back button change
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        navigationView = (NavigationView) findViewById(R.id.navigationview);

        final View headView = navigationView.getHeaderView(0);
        TextView drawerName = headView.findViewById(R.id.drawerName);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);

        jsonCookies = getIntent().getStringExtra("COOKIES");


        new AttendanceGrabber(this, this).execute(jsonCookies);

        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new AttendanceGrabber(getApplicationContext(), HomePage.this).execute(jsonCookies);
                    }
                }
        );


        TextView welName = (TextView) findViewById(R.id.welName);
        if (!TextUtils.isEmpty(loginName)) {
            loginName = loginName.substring(0, 1).toUpperCase() + loginName.substring(1).toLowerCase();
            welName.setText("Hi " + loginName + "!");
            drawerName.setText(loginName);
        }

        int[] intentIds = {
                R.id.booster, R.id.calendar, R.id.marks, R.id.directory
        };

        for (int intentId : intentIds) {
            ((LinearLayout) findViewById(intentId)).setOnClickListener(this);
        }
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
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
//                case R.id.app_send:
//                    Intent sendIntent = new Intent();
//                    sendIntent.setAction(Intent.ACTION_SEND);
//                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_eazycampus_text));
//                    sendIntent.setType("text/plain");
//                    startActivity(sendIntent);
//                    return true;

                case R.id.app_notification:
                    Intent intent = new Intent(HomePage.this, ReferenceActivity.class);
                    String remoteURL = "https://ktu.edu.in/eu/core/announcements.htm";
                    intent.putExtra("INTENT_URL", remoteURL);
                    intent.putExtra("IS_CALENDAR", false);
                    startActivity(intent);
                    break;

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
                FrameLayout layout = (FrameLayout) findViewById(R.id.showFragment);
                layout.removeAllViewsInLayout();
                break;

            case R.id.settings:
                getSupportFragmentManager().popBackStack();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, OPEN_NEW_ACTIVITY);
                break;

            case R.id.about:
                fragment = new AboutFragment();
                break;

            case R.id.signout:
                Intent signoutIntent = new Intent(HomePage.this, LoginPage.class);
                startActivity(signoutIntent);
                finish();

                pref.edit()
                        .remove("username")
                        .remove("password")
                        .apply();

                Toast.makeText(this, "You have logged out!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (fragment != null) {
            navigationView.setCheckedItem(id);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.showFragment, fragment);
            ft.addToBackStack(null);
            getSupportFragmentManager().popBackStack();
            ft.commit();
            ((ScrollView) findViewById(R.id.scroll_home)).setVisibility(View.INVISIBLE);
        } else {
            ((ScrollView) findViewById(R.id.scroll_home)).setVisibility(View.VISIBLE);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.marks:
                Intent IntentMarks = new Intent(HomePage.this, MarksPage.class);
                IntentMarks.putExtra("COOKIES", jsonCookies);
                startActivity(IntentMarks);
                break;

            case R.id.directory:
                Intent intentPhonDirect = new Intent(HomePage.this, FacultyDirectory.class);
                intentPhonDirect.putExtra("COOKIES", jsonCookies);
                startActivity(intentPhonDirect);
                break;

            case R.id.calendar:
                Intent intent = new Intent(HomePage.this, ReferenceActivity.class);
                String remoteURL = getString(R.string.academic_calendar_url);
                intent.putExtra("INTENT_URL", remoteURL);
                intent.putExtra("IS_CALENDAR", true);
                startActivity(intent);
                break;

            case R.id.booster:
                startActivity(new Intent(HomePage.this, BoosterAttendance.class));
                break;
        }

    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        ((ScrollView) this.findViewById(R.id.scroll_home)).setVisibility(View.VISIBLE);

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

        if (requestCode == OPEN_NEW_ACTIVITY) {
            navigationView.setCheckedItem(R.id.home);
            FrameLayout layout = (FrameLayout) findViewById(R.id.showFragment);
            layout.removeAllViewsInLayout();

        } else if (requestCode == 1337) {

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

                    Button tryagain = (Button) contributionView.findViewById(R.id.tryagain);
                    Button cont_later = (Button) contributionView.findViewById(R.id.cont_later);
                    CircleImageView tick = (CircleImageView) contributionView.findViewById(R.id.tick);

                    if (Status.equals("SUCCESS")) {
                        tryagain.setVisibility(View.VISIBLE);
                        ((TextView) contributionView.findViewById(R.id.contribution_title)).setText(R.string.thank_you);
                        ((TextView) contributionView.findViewById(R.id.transaction_desc)).setText(R.string.contribution_sent_message);

                        pref.edit().putInt("FIRST_COUNT", -1).apply();
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

    @Override
    public void onResume() {
        super.onResume();
        if (pref.getBoolean("FIRST_RUN", true)) {
            pref.edit().putBoolean("FIRST_RUN", false).apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void processFinish(ArrayList<SubjectAttendance> attendanceList) {
        if (attendanceList != null) {
            Log.i(TAG, "Attendance List: " + attendanceList.get(3).getSubjectName());

            AttendanceListAdapter adapter = new AttendanceListAdapter(this, R.layout.adapter_attendance, attendanceList);
            ListView mListView = (ListView) findViewById(R.id.homelistAtendance);
            mListView.setAdapter(adapter);

            ((CardView) findViewById(R.id.home_attendance_view)).setVisibility(View.VISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
        } else {
            ((CardView) findViewById(R.id.home_attendance_view)).setVisibility(View.GONE);
        }
    }
}
