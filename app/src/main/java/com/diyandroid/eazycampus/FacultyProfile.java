package com.diyandroid.eazycampus;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

public class FacultyProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faculty_profile);

        final TextView name, mobile_no, office_phone, department, designation, email, address;
        ImageView image;
        Button call_mobile, message_mobile;

        LinearLayout officeNoLayout, mobileNoLayout, addressLayout, emailLayout;

        //Toolbar the fancy stuffs
        Toolbar toolbar = findViewById(R.id.toolbarFaculty);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        image = findViewById(R.id.profile_image);
        name = findViewById(R.id.profile_name);
        mobile_no = findViewById(R.id.profile_mobile_no);
        office_phone = findViewById(R.id.profile_office_phone);
        department = findViewById(R.id.profile_department);
        designation = findViewById(R.id.profile_designation);
        email = findViewById(R.id.profile_email);
        address = findViewById(R.id.profile_address);

        mobileNoLayout = findViewById(R.id.mobileNoLayout);
        officeNoLayout = findViewById(R.id.officeNoLayout);
        addressLayout = findViewById(R.id.addressLayout);
        emailLayout = findViewById(R.id.emailLayout);

        Gson gson = new Gson();
        final Contact contact = gson.fromJson(getIntent().getStringExtra("SELECTED_FACULTY"), Contact.class);

        // image.setImageBitmap(getBitmapFromURL(contact.image));
        name.setText(contact.name);
        department.setText(contact.department);
        designation.setText(contact.designation);

        Glide.with(this)
                .load(contact.image)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(true))
                .into(image);

        if (contact.mobile_no == null) {
            mobileNoLayout.setVisibility(LinearLayout.GONE);
        } else {
            mobile_no.setText(contact.mobile_no);
        }

        if (contact.office_phone == null) {
            officeNoLayout.setVisibility(LinearLayout.GONE);
        } else {
            office_phone.setText(contact.office_phone);
        }

        if (contact.address == null) {
            addressLayout.setVisibility(LinearLayout.GONE);
        } else {
            address.setText(contact.address.replaceAll("undefined", ""));
        }

        if (contact.email == null) {
            emailLayout.setVisibility(LinearLayout.GONE);
        } else {
            email.setText(contact.email);
        }


        call_mobile = findViewById(R.id.call_mobile);
        message_mobile = findViewById(R.id.message_mobile);

        call_mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:+91 " + contact.mobile_no);
                Intent dial = new Intent(Intent.ACTION_DIAL, number);
                startActivity(dial);
            }
        });

        message_mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contact.mobile_no, null)));
            }
        });

        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mailto = "mailto:" + email.getText().toString();

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    //TODO: Handle case where no email app is available
                    Toast.makeText(FacultyProfile.this, "No Email Apps found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //Closing Activity with back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
