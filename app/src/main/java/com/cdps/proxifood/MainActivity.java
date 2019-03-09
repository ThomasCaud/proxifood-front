package com.cdps.proxifood;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "[MainActivity] ";

    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.action_post:
                        fragment = new InvitationsBookingsFragment();
                        break;
                    case R.id.action_search:
                        fragment = new SearchFragment();
                        break;
                    case R.id.action_user_account:
                        fragment = new AccountFragment();
                        break;
                }

                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
                return true;
            }
        });


        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);

      //  bottomNavigation.setSelectedItemId(R.id.action_search);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("REQUEST CODE", Integer.toString(requestCode));
        Log.i("RESULT CODE", Integer.toString(resultCode));
        super.onActivityResult(requestCode, resultCode, data);
             if (resultCode == Activity.RESULT_OK)
                 bottomNavigation.setSelectedItemId(R.id.action_search);
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
