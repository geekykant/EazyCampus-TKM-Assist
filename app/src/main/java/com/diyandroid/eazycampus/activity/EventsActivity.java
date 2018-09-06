package com.diyandroid.eazycampus.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.diyandroid.eazycampus.Notification;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.adapter.AnnouncementListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventsActivity extends AppCompatActivity {

    int limit = 2;
    SharedPreferences pref;
    Query recentPostsQuery;
    DatabaseReference databaseReference;
    AnnouncementListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_events);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        limit = pref.getInt("FILTER_NOTIF", 2);
        retrieveAnnouncements();
    }

    private void retrieveAnnouncements() {
        ListView listView = (ListView) findViewById(R.id.listviewAnnouncements);
        final ProgressBar progressBar = findViewById(R.id.progressbarEvent);

        final ArrayList<Notification> announcements = new ArrayList<>();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        databaseReference = database.getReference("Notifications");
        recentPostsQuery = databaseReference.child("KTU").limitToLast(limit);

        adapter = new AnnouncementListAdapter(this, R.layout.events_adapter, announcements);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(EventsActivity.this, ReferenceActivity.class);
                intent.putExtra("INTENT_URL", "https://ktu.edu.in/eu/core/announcements.htm");
                startActivity(intent);
            }
        });

        // Attach a listener to read the data at our posts reference
        recentPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notification : dataSnapshot.getChildren()) {
                    Notification anm = notification.getValue(Notification.class);
                    announcements.add(anm);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.announcements_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.limitNotification:
                showFilterDilogBox();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterDilogBox() {
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.filter_dialoguebox, null);

        final AlertDialog filterDialogue = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle("Limit Announcements")
                .show();

        final EditText filterbox = (EditText) filterDialogue.findViewById(R.id.limitInput);
        filterbox.setText(String.valueOf(limit));

        filterbox.setSelection(filterbox.getText().length());

        ((Button) filterDialogue.findViewById(R.id.submitFilter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(filterbox.getText())) {
                    filterDialogue.dismiss();
                } else {
                    limit = Integer.parseInt(filterbox.getText().toString());
                    if (limit <= 0) {
                        Toast.makeText(EventsActivity.this, "Minimum values should be 1!", Toast.LENGTH_SHORT).show();
                    } else {
                        filterDialogue.dismiss();
                        recentPostsQuery = databaseReference.child("KTU").limitToLast(limit);
                        retrieveAnnouncements();
                        pref.edit().putInt("FILTER_NOTIF", limit).apply();
                    }
                }

            }
        });

    }

}
