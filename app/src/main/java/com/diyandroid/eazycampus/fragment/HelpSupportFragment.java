package com.diyandroid.eazycampus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.diyandroid.eazycampus.R;

public class HelpSupportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.help_support_fragment, container, false);

        //initialize the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.helpSupportToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open navigation drawer when click navigation back button
                ((FrameLayout) getActivity().findViewById(R.id.showFragment)).removeAllViewsInLayout();
                ((NavigationView) getActivity().findViewById(R.id.navigationview)).setCheckedItem(R.id.home);
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
