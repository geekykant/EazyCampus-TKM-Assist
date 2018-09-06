package com.diyandroid.eazycampus.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.diyandroid.eazycampus.R;

public class CreditsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits_page);

        //initialize the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.creditsToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open navigation drawer when click navigation back button
                finish();
                overridePendingTransition(0,R.anim.exit);
            }
        });
    }
}
