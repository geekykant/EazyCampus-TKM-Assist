package com.diyandroid.eazycampus.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.text.TextUtils;
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
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.MyApplication;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.Story;
import com.diyandroid.eazycampus.adapter.MainSliderAdapter;
import com.diyandroid.eazycampus.app.Config;
import com.diyandroid.eazycampus.app.LogOutTimerUtil;
import com.diyandroid.eazycampus.fragment.AboutFragment;
import com.diyandroid.eazycampus.fragment.HelpSupportFragment;
import com.diyandroid.eazycampus.service.PicassoImageLoadingService;
import com.diyandroid.eazycampus.UserStatus;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import ss.com.bannerslider.Slider;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        LogOutTimerUtil.LogOutListener {

    private CircleImageView profile_image;
    private AlertDialog profileDialog;

    private SharedPreferences pref;

    private NavigationView navigationView;
    private String jsonCookies, today;
    private String PREF_QUOTE_AUTHOR = "quoteAuthor", PREF_QUOTE_TEXT = "quoteText", PREF_TODAY_DATE = "today";

    public static final int OPEN_NEW_ACTIVITY = 6588;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private AlertDialog adsDialogue;
    private Slider slider;

    private InterstitialAd mInterstitialAd;

    private UserStatus user;
    private boolean FIRST_RUN;
    int FIRST_COUNT;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_layout);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        Slider.init(new PicassoImageLoadingService(this));
        fetchCGPUStories();

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        String QUOTE_AUTHOR = pref.getString(PREF_QUOTE_AUTHOR, null);
        String QUOTE_TEXT = pref.getString(PREF_QUOTE_TEXT, null);
        String TODAY_DATE = pref.getString(PREF_TODAY_DATE, null);

        String PREF_PROFILE_IMG = pref.getString("PREF_PROFILE_IMG", null);
        FIRST_COUNT = pref.getInt("FIRST_COUNT", 0);
        FIRST_RUN = pref.getBoolean("FIRST_RUN", true);

//        boolean ONE_TIME_POPUP = pref.getBoolean("ONE_TIME_POPUP", true);

        String loginName = getIntent().getStringExtra("LOGIN_NAME");
        loginName = loginName.substring(0, 1).toUpperCase() + loginName.substring(1).toLowerCase();

        // Default Mailing system for all_semesters (Add Intent Later)
        if (FIRST_COUNT % 3 == 0) {
            //Ads dialogue display
            final View adsDialogueView = LayoutInflater.from(this).inflate(R.layout.ads_dialoguebox, null);
            adsDialogueView.setElevation(10);
            ((TextView) adsDialogueView.findViewById(R.id.ads_description)).setText("Hey " + loginName + "! " + getText(R.string.ads_dialog_message));
            adsDialogue = new AlertDialog.Builder(this).create();
            adsDialogue.setCancelable(false);
            adsDialogue.setCanceledOnTouchOutside(false);
            adsDialogue.setView(adsDialogueView);

            Button contribute = (Button) adsDialogueView.findViewById(R.id.contribute);
            Button later = (Button) adsDialogueView.findViewById(R.id.later);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            adsDialogue.show();
                        }
                    }, 1000);

            contribute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    paywithUPI();
                }
            });
            later.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adsDialogue.hide();
                }
            });
        }

//        if (ONE_TIME_POPUP) {
//            View cgpuDialogueView = LayoutInflater.from(this).inflate(R.layout.cgpu_enroll, null);
//            cgpuDialogueView.setElevation(5);
//            final AlertDialog cgpuDialogue = new AlertDialog.Builder(this).create();
//            cgpuDialogue.setCancelable(false);
//            cgpuDialogue.setCanceledOnTouchOutside(false);
//            cgpuDialogue.setView(cgpuDialogueView);
//            new android.os.Handler().postDelayed(
//                    new Runnable() {
//                        public void run() {
//                            cgpuDialogue.show();
//                        }
//                    }, 2000);
//
//            Button okay = (Button) cgpuDialogueView.findViewById(R.id.okay_cgpu);
//            Button subscribe = (Button) cgpuDialogueView.findViewById(R.id.subscribe);
//            okay.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    cgpuDialogue.hide();
//                }
//            });
//
//            subscribe.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    FirebaseMessaging.getInstance().subscribeToTopic("cgpu");
//                    pref.edit().putBoolean("CGPU_NOTIF_ENABLED", true).apply();
//                    Toast.makeText(HomePage.this, "Subscribed to CGPU Notifications!", Toast.LENGTH_SHORT).show();
//                    cgpuDialogue.hide();
//                }
//            });
//
//            pref.edit().putBoolean("ONE_TIME_POPUP", false).apply();
//        }

        if (FIRST_COUNT >= 0) {
            pref.edit().putInt("FIRST_COUNT", FIRST_COUNT + 1).apply();
        }

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
                if (user != null) {
                    if (!user.hasPaid() && !FIRST_RUN) {
                        mInterstitialAd = new InterstitialAd(getApplicationContext());
                        mInterstitialAd.setAdUnitId("ca-app-pub-9024346977117639/3816312153");
                        showAds();
                    }
                } else {
                    showAds();
                }

                Intent signoutIntent = new Intent(HomePage.this, LoginPage.class);
                startActivity(signoutIntent);
                finish();

                new doLogout(this).execute();

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
                new doLogout(this).execute();
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

        String intent_username = getIntent().getStringExtra("LOGIN_USERNAME");

        if (!TextUtils.isEmpty(intent_username)) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference query = ref.child("Payments").child(intent_username);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mInterstitialAd = new InterstitialAd(getApplicationContext());
                    mInterstitialAd.setAdUnitId("ca-app-pub-9024346977117639/4642524265");

                    if (dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(UserStatus.class);
                        if (!user.hasPaid() && !FIRST_RUN) {
                            showAds();
                        }

                        if (user.hasPaid()) {
                            pref.edit().putInt("FIRST_COUNT", -1).apply();
                        } else {
                            pref.edit().putInt("FIRST_COUNT", 0).apply();
                        }

                        Log.d("LoginPage", "Contribution status: " + user.hasPaid());
                    } else if (!FIRST_RUN) {
                        showAds();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    }

    private void showAds() {
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
    }

    @Override
    public void doLogout() {
        startActivity(new Intent(HomePage.this, SplashLoading.class));
        finish();
    }

    private boolean parsingSuccessful;

    private class doLogout extends ExceptionHandlingAsyncTask<String, Void, Element> {

        Map<String, String> loginCookies = new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {
        }.getType());

        Connection.Response response;

        public doLogout(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingSuccessful = true;
            //Progress bar implementations
        }

        @Override
        protected Element doInBackground2(String... strings) {
            try {
                Document evaluationPage = Jsoup.connect(getString(R.string.tkmce_home))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_index_url))
                        .followRedirects(true)
                        .userAgent("Mozilla")
                        .method(Connection.Method.GET)
                        .timeout(30 * 1000)
                        .execute().parse();

                response = Jsoup.connect(getString(R.string.tkmce_home))
                        .cookies(loginCookies)
                        .referrer(getString(R.string.tkmce_home))
                        .data("__EVENTTARGET", "ctl00$lnkBtnLogout")
                        .data("__EVENTARGUMENT", "")
                        .data("__LASTFOCUS", "")
                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
                        .data("ctl00$hdnisclose", "false")
                        .data("ctl00$ContentPlaceHolder1$hdnIsMarkEntry", "NO")
                        .data("ctl00$ContentPlaceHolder1$hdnSubjectValues", "")
                        .data("ctl00$ContentPlaceHolder1$hdnValues", "")
                        .data("ctl00$HiddenField1", "")
                        .followRedirects(false)
                        .method(Connection.Method.POST)
                        .userAgent("Mozilla")
                        .timeout(30 * 1000)
                        .execute();

            } catch (IOException ex) {
                ex.printStackTrace();
                parsingSuccessful = false;
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute2(Element element) {
            if (parsingSuccessful && response.statusCode() == 200) {
                Toast.makeText(HomePage.this, "Logged out!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchCGPUStories() {
        final ArrayList<String> bannerList = new ArrayList<>();
        slider = findViewById(R.id.banner_slider1);

        JsonArrayRequest request = new JsonArrayRequest(getString(R.string.cgpu_stories_url),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            return;
                        }

                        List<String> items = new Gson().fromJson(response.toString(), new TypeToken<List<String>>() {
                        }.getType());

                        // adding contacts to contacts list
                        bannerList.clear();
                        bannerList.addAll(items);

                        slider.setAdapter(new MainSliderAdapter(bannerList));
                        slider.setSelectedSlide(0);

                        if (bannerList.size() != 1) {
                            slider.setLoopSlides(true);
                            slider.setInterval(3300);
                        }

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        ((ImageView) findViewById(R.id.cgpuPlaceholder)).setVisibility(View.GONE);
                                        slider.setVisibility(View.VISIBLE);
                                    }
                                }, 1000);


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
}