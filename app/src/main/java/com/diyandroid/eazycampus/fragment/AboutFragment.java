package com.diyandroid.eazycampus.fragment;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.activity.CreditsPage;

import java.util.List;

public class AboutFragment extends Fragment implements View.OnClickListener {

    Button facebook, linkedin, youtube, instagram;
    RelativeLayout clickToDonate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        //initialize the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragToolbar);
//        toolbar.setTitle("About");
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open navigation drawer when click navigation back button
                ((FrameLayout) getActivity().findViewById(R.id.showFragment)).removeAllViewsInLayout();
                ((NavigationView) getActivity().findViewById(R.id.navigationview)).setCheckedItem(R.id.home);
            }
        });

        facebook = view.findViewById(R.id.facebook);
        youtube = view.findViewById(R.id.youtube);
        linkedin = view.findViewById(R.id.linkedin);
        instagram = view.findViewById(R.id.instagram);

        clickToDonate = view.findViewById(R.id.clickToDonate);

        LinearLayout license = (LinearLayout) view.findViewById(R.id.licenseClick);

        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CreditsPage.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.enter, R.anim.exit);
                startActivity(intent, options.toBundle());
            }
        });

        facebook.setOnClickListener(this);
        youtube.setOnClickListener(this);
        linkedin.setOnClickListener(this);
        instagram.setOnClickListener(this);
        clickToDonate.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.facebook:
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(getActivity());
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
                break;

            case R.id.linkedin:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://sreekantshenoy"));
                final PackageManager packageManager = getContext().getPackageManager();
                final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.isEmpty()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/profile/view?id=sreekantshenoy"));
                }
                startActivity(intent);
                break;

            case R.id.instagram:
                Uri uri = Uri.parse("http://instagram.com/_u/geekykant");
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                likeIng.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/geekykant")));
                }
                break;

            case R.id.youtube:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCV_-ArLOkdC9P1-sp60iX9A")));
                break;

            case R.id.clickToDonate:
                Uri UPI = new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", "eazycampusapp@paytm")
                                .appendQueryParameter("pn", "SREEKANT S SHENOY")
                                .appendQueryParameter("tn", "EazyCampus Development Support :)")
                                .appendQueryParameter("cu", "INR")
                                .build();

                intent = new Intent();
                intent.setData(UPI);
                Intent chooser = Intent.createChooser(intent, "Pay with...");
                startActivityForResult(chooser, 1, null);
                break;
        }
    }

    public static String FACEBOOK_URL = "https://www.facebook.com/iamsreekantshenoy";
    public static String FACEBOOK_PAGE_ID = "iamsreekantshenoy";

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
}
