package com.diyandroid.eazycampus.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.diyandroid.eazycampus.AppCompatPreferenceActivity;
import com.diyandroid.eazycampus.R;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            bindPreferenceSummaryToValue(findPreference("key_notification_semester"));
            bindPreferenceSummaryToValue(findPreference("key_course_selection"));

//            findPreference("key_cgpu_notification_receive").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object o) {
//
//                    Toast.makeText(getActivity(), "You are here: 1", Toast.LENGTH_SHORT).show();
//
//                    if (preference instanceof SwitchPreference) {
//                        if (preference.getKey().equals("key_cgpu_notification_receive")) {
//                            if (preference.getExtras().getBoolean("CGPU_NOTIF_ENABLED", false)) {
//                                ((SwitchPreference) preference).setChecked(true);
//                                Toast.makeText(getActivity(), "You are here: 2", Toast.LENGTH_SHORT).show();
//                            } else {
//                                ((SwitchPreference) preference).setChecked(false);
//                            }
//                        }
//                    }
//                    return true;
//                }
//            });

            final Preference pref = findPreference("key_cgpu_notification_receive");

            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (preference.getKey().equals("key_cgpu_notification_receive")) {
                        if (!((SwitchPreference) preference).isChecked()) {
                            FirebaseMessaging.getInstance().subscribeToTopic("cgpu");
                            preference.getEditor().putBoolean("CGPU_NOTIF_ENABLED", true).apply();
                        } else {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("cgpu");
                            preference.getEditor().putBoolean("CGPU_NOTIF_ENABLED", false).apply();
                        }
                    }
                    return true;
                }
            });


            Preference preference = findPreference("key_notification_receive");
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (preference.getKey().equals("key_notification_receive")) {
                        if (((SwitchPreference) preference).isChecked()) {
                            for (int i = 1; i < 9; i++) {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("S" + i);
                            }
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("all_semesters");
                            Toast.makeText(getActivity(), "Unsubscribed from push notifications!", Toast.LENGTH_SHORT).show();

                        } else {
                            Preference topic_pref = findPreference("key_notification_semester");
                            ListPreference listPreference = (ListPreference) topic_pref;
                            int index = Integer.parseInt(listPreference.getValue());

                            Log.d(TAG, "Index is: " + index);
                            if (index != 0) {
                                FirebaseMessaging.getInstance().subscribeToTopic("S" + index);
                                Log.d(TAG, "Subscribed to: " + "S" + index);
                            } else {
                                FirebaseMessaging.getInstance().subscribeToTopic("all_semesters");
                                Log.d(TAG, "Subscribed to: all_semesters");
                            }
                        }
                        Log.d(TAG, "Now its time!");
                    }
                    return true;

                }
            });
        }
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String summaryValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                if (preference.getKey().equals("key_notification_semester")) {
                    ListPreference listPreference = (ListPreference) preference;

                    //Unsubscribe from previous topic
                    if (Objects.equals(listPreference.getValue(), "0")) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_semesters");
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("S" + listPreference.getValue());
                    }

                    int index = listPreference.findIndexOfValue(summaryValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                    //Subscribe to topic
                    if (index != 0) {
                        FirebaseMessaging.getInstance().subscribeToTopic("S" + index);
                        Log.d(TAG, "Subscribed to: " + "S" + index);
                    } else {
                        FirebaseMessaging.getInstance().subscribeToTopic("all_semesters");
                        Log.d(TAG, "Subscribed to: all_semesters");
                    }
                } else if (preference.getKey().equals("key_course_selection")) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(summaryValue);

                    switch (index) {
                        case 0:
                            preference.getEditor().putInt("ATTENDANCE_PERCENT", 75).apply();
                            break;
                        case 1:
                            preference.getEditor().putInt("ATTENDANCE_PERCENT", 85).apply();
                            break;
                        default:
                            preference.getEditor().putInt("ATTENDANCE_PERCENT", 85).apply();
                    }

                    Log.d(TAG, "Summary: " + listPreference.getEntries()[index].toString());
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index].toString() : null);
                }
            }
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}