package com.cdps.proxifood;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import org.json.JSONObject;

public class ViewProfileFragment extends Fragment {

    private ProgressDialog _progressDialog;
    private JSONObject user;
    private EditText _nameText;
    private EditText _firstnameText;
    private EditText _emailText;
    private EditText _passwordText;
    private EditText _descriptionText;

    private long userId;

    private FloatingActionButton _addAddressBtn;

    public ViewProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        userId = args.getLong("userId");

        System.out.println(userId);


        // Set the content of the activity to use the  activity_main.xml layout file
       // getActivity().setContentView(R.layout.fragment_view_profile);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) getView().findViewById(R.id.view_pager);

        // Create an adapter that knows which fragment should be shown on each page
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getContext(), getChildFragmentManager(), userId);

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
