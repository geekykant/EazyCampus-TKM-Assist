package com.diyandroid.eazycampus.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.diyandroid.eazycampus.Contact;
import com.diyandroid.eazycampus.FacultyProfile;
import com.diyandroid.eazycampus.MyApplication;
import com.diyandroid.eazycampus.MyDividerItemDecoration;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.adapter.ContactsAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class FacultyDirectory extends AppCompatActivity implements ContactsAdapter.ContactsAdapterListener {
    private static final String TAG = FacultyDirectory.class.getSimpleName();
    private List<Contact> contactList;
    private ContactsAdapter mAdapter;
    private SearchView searchView;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressbarContact);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        contactList = new ArrayList<>();
        mAdapter = new ContactsAdapter(this, contactList, this);

        // white background notification bar
        whiteNotificationBar(recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);

        fetchContacts();
        recyclerView.getRecycledViewPool().clear();
    }

    /**
     * fetches json by making http calls
     */
    private void fetchContacts() {
        JsonArrayRequest request = new JsonArrayRequest(getString(R.string.all_faculty_url),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<Contact> items = new Gson().fromJson(response.toString(), new TypeToken<List<Contact>>() {
                        }.getType());

                        // adding contacts to contacts list
                        contactList.clear();
                        contactList.addAll(items);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

//    private static final int REQUEST_SEND_SMS = 110, REQUEST_CALL_PHONE = 111;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            //request permission
//
//            boolean hasPermissionSms = (ContextCompat.checkSelfPermission(getApplicationContext(),
//                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
//            if (!hasPermissionSms) {
//                ActivityCompat.requestPermissions(FacultyDirectory.this,
//                        new String[]{Manifest.permission.SEND_SMS},
//                        REQUEST_SEND_SMS);
//            }
//
//            boolean hasPermissionPhone = (ContextCompat.checkSelfPermission(getApplicationContext(),
//                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
//            if (!hasPermissionPhone) {
//                ActivityCompat.requestPermissions(FacultyDirectory.this,
//                        new String[]{Manifest.permission.CALL_PHONE},
//                        REQUEST_CALL_PHONE);
//            }
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_SEND_SMS: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    Toast.makeText(FacultyDirectory.this, "Permission granted.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(FacultyDirectory.this, "Permission denied. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//            break;
//
//            case REQUEST_CALL_PHONE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    Toast.makeText(FacultyDirectory.this, "Permission granted.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(FacultyDirectory.this, "Permission denied. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//            break;
//
//        }
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onContactSelected(Contact contact) {
        String jsonContact = new Gson().toJson(contact);

        Intent intent = new Intent(FacultyDirectory.this, FacultyProfile.class);
        intent.putExtra("SELECTED_FACULTY", jsonContact);
        startActivity(intent);
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
