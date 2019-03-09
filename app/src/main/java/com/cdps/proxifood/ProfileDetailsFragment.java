package com.cdps.proxifood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileDetailsFragment extends Fragment {

    private ProgressDialog _progressDialog;
    private TextView _firstnameText;
    private TextView _nameText;
    private TextView _descriptionText;
    private TextView _addressText;
    private Button _contactBtn;

    private long userId;

    public static ProfileDetailsFragment newInstance(long userId) {

        Bundle args = new Bundle();
        args.putLong("userId", userId);
        ProfileDetailsFragment fragment = new ProfileDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("ACTION","Activity Created");
        initializeComponents();

        if (getArguments() != null) {
            userId = getArguments().getLong("userId");
        }

        Log.i("User id", Long.toString(userId));

        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.loading_profile));
        _progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        String url = Globals.SERVER_ADDRESS + "/users/" + userId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject user) {
                        try {
                            _firstnameText.setText(user.getString("firstName"));
                            _nameText.setText(user.getString("lastName"));
                            Log.i("DESCRIPTION", user.getString("description"));

                            if (user.has("description") && !user.isNull("description") && !user.getString("description").equals(""))
                                _descriptionText.setText(user.getString("description"));
                            else
                                _descriptionText.setText(getString(R.string.default_user_description));

                           JSONObject address = user.getJSONObject("homeAddress");
                           String addressStr = "";
                            if (!address.isNull("city") && !address.getString("city").equals("") && !address.getString("country").equals(""))
                                addressStr += address.getString("city") + ", " + address.getString("country");
                            else if (!address.isNull("country") && !address.getString("country").equals(""))
                                addressStr += address.getString("country");
                            else
                                addressStr = getString(R.string.default_address_text);

                            _addressText.setText(addressStr);
                            _progressDialog.dismiss();
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Globals.TAG_SERVER_ERROR, error.getMessage());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Globals.getSessionToken());
                return params;
            }
        };

        queue.add(request);

    }

    public void initializeComponents() {
        _progressDialog = new ProgressDialog(getActivity());
        _firstnameText = getActivity().findViewById(R.id.firstname_text);
        _nameText = getActivity().findViewById(R.id.name_text);
        _descriptionText = getActivity().findViewById(R.id.description_text);
        _addressText = getActivity().findViewById(R.id.address_text);
        _contactBtn = getActivity().findViewById(R.id.contact_button);

        _contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : Lier au chat
            }
        });

    }

}
