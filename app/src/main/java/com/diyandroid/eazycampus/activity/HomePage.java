package com.diyandroid.eazycampus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.diyandroid.eazycampus.MyApplication;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.Story;
import com.diyandroid.eazycampus.app.Config;
import com.diyandroid.eazycampus.app.LogOutTimerUtil;
import com.diyandroid.eazycampus.fragment.AboutFragment;
import com.diyandroid.eazycampus.fragment.HelpSupportFragment;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        LogOutTimerUtil.LogOutListener{

    private CircleImageView profile_image;
    private AlertDialog profileDialog;

    private SharedPreferences pref;

    private NavigationView navigationView;
    private String jsonCookies, today;
    private String PREF_QUOTE_AUTHOR = "quoteAuthor", PREF_QUOTE_TEXT = "quoteText", PREF_TODAY_DATE = "today";

    public static final int OPEN_NEW_ACTIVITY = 6588;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_layout);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        String QUOTE_AUTHOR = pref.getString(PREF_QUOTE_AUTHOR, null);
        String QUOTE_TEXT = pref.getString(PREF_QUOTE_TEXT, null);
        String TODAY_DATE = pref.getString(PREF_TODAY_DATE, null);

        String PREF_PROFILE_IMG = pref.getString("PREF_PROFILE_IMG", null);
        boolean FIRST_TIME = pref.getBoolean("FIRST_RUN", true);

        String loginName = getIntent().getStringExtra("LOGIN_NAME");
        loginName = loginName.substring(0, 1).toUpperCase() + loginName.substring(1).toLowerCase();

        // Default Mailing system for all_semesters (Add Intent Later)
//        if (FIRST_TIME) {
            //Ads dialogue display
            View adsDialogueView = LayoutInflater.from(this).inflate(R.layout.ads_dialoguebox, null);
            ((TextView) adsDialogueView.findViewById(R.id.ads_description)).setText("Hey " + loginName + "! " + getText(R.string.ads_dialog_message));
            final AlertDialog adsDialogue = new AlertDialog.Builder(this).create();
            adsDialogue.setCancelable(false);
            adsDialogue.setCanceledOnTouchOutside(false);
            adsDialogue.setView(adsDialogueView);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            adsDialogue.show();
                        }
                    }, 2000);

            Button submitAdsBox = (Button) adsDialogueView.findViewById(R.id.contribute);
            submitAdsBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adsDialogue.hide();
                }
            });

            FirebaseMessaging.getInstance().subscribeToTopic("all_semesters");
            FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
//        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_toggle, R.string.close_toggle);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_button); //back button change
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        navigationView = (NavigationView) findViewById(R.id.navigationview);

        final View headView = navigationView.getHeaderView(0);
        TextView drawerName = headView.findViewById(R.id.drawerName);

        profile_image = headView.findViewById(R.id.profile_image);

        //Change profile picture dialogue
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View profileChange = layoutInflater.inflate(R.layout.profile_image_change, null);
        profileDialog = new AlertDialog.Builder(this).create();
        profileDialog.setView(profileChange);

        if (PREF_PROFILE_IMG != null) {
            profile_image.setImageDrawable(getResources().getDrawable(Integer.parseInt(PREF_PROFILE_IMG)));
        }
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileDialog.show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);

        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        if (Objects.equals(TODAY_DATE, today)) {
            //Set saved quote for day
            ((TextView) findViewById(R.id.quoteWritten)).setText(QUOTE_TEXT);
            ((TextView) findViewById(R.id.quoteBy)).setText(QUOTE_AUTHOR);
        } else {
            new getQuote().execute();
        }

        jsonCookies = getIntent().getStringExtra("COOKIES");

        TextView welName = (TextView) findViewById(R.id.welName);
        welName.setText("Hi " + loginName + "!");
        drawerName.setText(loginName);

        int[] profile_ids = {
                R.id.profile_ic_1,
                R.id.profile_ic_2,
                R.id.profile_ic_3,
                R.id.profile_ic_4
        };

        for (int i = 0; i < profile_ids.length; i++) {
            ((ImageView) profileChange.findViewById(profile_ids[i])).setOnClickListener(btnClickListener);
            ((ImageView) profileChange.findViewById(profile_ids[i])).setTag(i + 1);
        }

        int[] intentIds = {
                R.id.calendar, R.id.booster, R.id.attendance, R.id.marks, R.id.directory, R.id.evaluation, R.id.notes, R.id.updates
        };

        for (int intentId : intentIds) {
            ((LinearLayout) findViewById(intentId)).setOnClickListener(this);
        }

        fetchStories();
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
                case R.id.app_send:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_eazycampus_text));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    return true;

                case R.id.app_notification:
                    Intent intent = new Intent(HomePage.this, EventsActivity.class);
                    startActivity(intent);
                    return true;
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

            case R.id.feedback:
                fragment = new HelpSupportFragment();
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
                        .remove("FIRST_RUN")
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
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.attendance:
                Intent IntentAttendance = new Intent(HomePage.this, AttendancePage.class);
                IntentAttendance.putExtra("COOKIES", jsonCookies);
                startActivity(IntentAttendance);
                break;

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

            case R.id.evaluation:
                Intent intentEval = new Intent(HomePage.this, StaffEvaluation.class);
                intentEval.putExtra("COOKIES", jsonCookies);
                startActivity(intentEval);
                break;

            case R.id.calendar:
                Intent intent = new Intent(HomePage.this, ReferenceActivity.class);
                String remoteURL = getString(R.string.academic_calendar_url);
                intent.putExtra("INTENT_URL", remoteURL);
                intent.putExtra("IS_CALENDAR", true);
                startActivity(intent);
                break;

            case R.id.notes:
                intent = new Intent(HomePage.this, ReferenceActivity.class);
                intent.putExtra("INTENT_URL", getString(R.string.ktustudy_blog_url));
                startActivity(intent);
                break;

            case R.id.updates:
                startActivity(new Intent(HomePage.this, EventsActivity.class));
                break;

            case R.id.booster:
                startActivity(new Intent(HomePage.this, BoosterAttendance.class));
                break;
        }

    }

    private class getQuote extends AsyncTask<Void, Void, Void> {
        String data = "";
        String quoteAuthor, quoteText;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(getString(R.string.quotes_firebase_url));
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    data = data + line;
                }

                final int random = new Random().nextInt(797);

                JSONArray JA = new JSONArray(data);
                JSONObject JO = (JSONObject) JA.get(random);

                quoteAuthor = "-" + JO.get("quoteAuthor").toString();
                quoteText = "“" + JO.get("quoteText").toString() + "”";

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ((TextView) findViewById(R.id.quoteWritten)).setText(quoteText);
            ((TextView) findViewById(R.id.quoteBy)).setText(quoteAuthor);

            pref.edit()
                    .putString(PREF_QUOTE_AUTHOR, quoteAuthor)
                    .putString(PREF_QUOTE_TEXT, quoteText)
                    .putString(PREF_TODAY_DATE, today)
                    .apply();
        }
    }

    private void fetchStories() {
        final ArrayList<Story> campusStories = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(getString(R.string.stories_firebase_url),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            return;
                        }

                        List<Story> items = new Gson().fromJson(response.toString(), new TypeToken<List<Story>>() {
                        }.getType());

                        // adding contacts to contacts list
                        campusStories.clear();
                        campusStories.addAll(items);

                        int ids[] = {
                                R.id.storyCard1,
                                R.id.storyCard2,
                                R.id.storyCard3,
                                R.id.storyCard4,
                                R.id.storyCard5,
                        };

                        int text_ids[] = {
                                R.id.stackTitle1,
                                R.id.stackTitle2,
                                R.id.stackTitle3,
                                R.id.stackTitle4,
                                R.id.stackTitle5,
                        };

                        int image_ids[] = {
                                R.id.stackImage1,
                                R.id.stackImage2,
                                R.id.stackImage3,
                                R.id.stackImage4,
                                R.id.stackImage5
                        };

                        if (campusStories.size() != 0) {
                            for (int i = 0; i < 5; i++) {
                                Story story1 = campusStories.get(i);
                                if (story1.isIs_present()) {
                                    ((CardView) findViewById(ids[i])).setVisibility(View.VISIBLE);
                                    final int finalI = i;

                                    ((TextView) findViewById(text_ids[i])).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(campusStories.get(finalI).getIntentURL()));
                                            startActivity(i);

                                        }
                                    });

                                    if (story1.getTitle().length() > 37) {
                                        ((TextView) findViewById(text_ids[i])).setText(story1.getTitle().substring(0, 37) + "...");
                                    }
                                    Glide.with(getApplicationContext())
                                            .load(story1.getImageURL())
                                            .apply(new RequestOptions().placeholder(R.drawable.horizontal_stack))
                                            .transition(DrawableTransitionOptions.withCrossFade(1000))
                                            .into((ImageView) findViewById(image_ids[i]));
                                } else {
                                    ((CardView) findViewById(ids[i])).setVisibility(View.GONE);
                                }
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e("HomePage", "Error: " + error.getMessage());
            }
        });
        MyApplication.getInstance().addToRequestQueue(request);
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
        if (requestCode == OPEN_NEW_ACTIVITY) {
            navigationView.setCheckedItem(R.id.home);
            FrameLayout layout = (FrameLayout) findViewById(R.id.showFragment);
            layout.removeAllViewsInLayout();
        }
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            String fnm = "profile_ic_" + v.getTag();
            int imgId = getResources().getIdentifier(getApplicationContext().getPackageName() + ":drawable/" + fnm, null, null);
            Drawable res = getResources().getDrawable(imgId);
            profile_image.setImageDrawable(res);
            profileDialog.hide();

            pref.edit()
                    .putString("PREF_PROFILE_IMG", String.valueOf(imgId))
                    .apply();
        }
    };

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
        LogOutTimerUtil.startLogoutTimer(this, this);
    }

    @Override
    public void doLogout() {
        startActivity(new Intent(HomePage.this, SplashLoading.class));
        finish();
    }
}