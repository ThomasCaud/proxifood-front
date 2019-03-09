package com.cdps.proxifood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ProgressDialog _progressDialog;
    private JSONObject user;
    private EditText _nameText;
    private EditText _firstnameText;
    private EditText _emailText;
    private EditText _passwordText;
    private EditText _descriptionText;

    private FloatingActionButton _addAddressBtn;

    public EditProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_edit_profile));

        _progressDialog = new ProgressDialog(getActivity());
        _nameText = getView().findViewById(R.id.input_name);
        _firstnameText = getView().findViewById(R.id.input_firstname);
        _emailText = getView().findViewById(R.id.input_email);
        _passwordText = getView().findViewById(R.id.input_password);
        _descriptionText = getView().findViewById(R.id.input_description);

        _addAddressBtn = getView().findViewById(R.id.btn_add_address);

        _addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        // TODO: Préremplir les informations

        Bundle args = getArguments();

        try {
            user = new JSONObject(args.getString("user"));
            _firstnameText.setText(user.getString("firstName"));
            _nameText.setText(user.getString("lastName"));
            _emailText.setText(user.getString("email"));
            SharedPreferences prefs = getActivity().getSharedPreferences("PROXIFOOD_USER", Context.MODE_PRIVATE);
            String password = prefs.getString("USER_PWD", "");
            _passwordText.setText(password);
            if(user.getString("description") != "null")
                _descriptionText.setText(user.getString("description"));
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveProfile() {

        String firstname = _firstnameText.getText().toString();
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String description = _descriptionText.getText().toString();

        Log.i(Globals.TAG_ACTION, "Save user profile");

        JSONObject json = new JSONObject();

        try {
            json.put("firstName", firstname);
            json.put("lastName", name);
            Log.i("Email", email);
            Log.i("User email", user.getString("email"));
            if(!email.equals(user.getString("email")))
                json.put("email", email);
            json.put("password",password);
            json.put("description",description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("JSON", json.toString());

        if (validate()) {

            _progressDialog.setIndeterminate(true);
            _progressDialog.setMessage(getString(R.string.saving_profile));
            _progressDialog.show();


            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = "http://proxifood.ddns.net:8080/users/" + Globals.getUserId();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                            Log.i("YES", "Enregistré !");
                            _progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Profil enregistré", Toast.LENGTH_SHORT).show();
                            nextPage();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    if(error.networkResponse.statusCode == 400 ) {
                        NetworkResponse networkResponse = error.networkResponse;
                        try {
                            JSONObject response = new JSONObject(new String(networkResponse.data));
                            Log.i("JSON", new String(networkResponse.data));

                            if (response.getInt("code") == 2) {
                                Log.i(Globals.TAG_FAILURE, "Email already used");
                                _emailText.setError(getString(R.string.email_already_used));
                            }
                        }
                        catch(JSONException e) {
                            Log.e(Globals.TAG_FAILURE, e.getMessage());
                        }
                    }
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("token", Globals.getSessionToken());
                    return params;
                }
            };

            queue.add(request);
        }
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String firstname = _firstnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // Check name
        if(name.isEmpty()) {
            _nameText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            _nameText.setError(null);
        }

        // Check firstname
        if(firstname.isEmpty()) {
            _firstnameText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            _firstnameText.setError(null);
        }

        // Check email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        // Check password
        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void nextPage() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://restcountries.eu/rest/v2/all";
        final ArrayList<String> listCountries = new ArrayList<>();
        listCountries.add("Sélectionnez votre pays");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        try {
                            for(int i=0; i<response.length(); i++ ) {
                                JSONObject country = response.getJSONObject(i);

                                if(Locale.getDefault().getLanguage() == "fr") {
                                    JSONObject translations = country.getJSONObject("translations");
                                    listCountries.add(translations.getString("fr"));
                                }
                                else {
                                    listCountries.add(country.getString("name"));
                                }
                            }

                            Bundle params = new Bundle();
                            final FragmentTransaction ft = getFragmentManager().beginTransaction();
                            params.putStringArrayList("listCountries", listCountries);
                            AddUserAddressFragment_Page1 fragment = new AddUserAddressFragment_Page1();
                            fragment.setArguments(params);
                            ft.replace(R.id.main_container, fragment).commit();
                            ft.addToBackStack(null);

                        } catch(JSONException e) {
                            Log.e("TAG", e.getMessage());
                        }

                        Log.i("response", listCountries.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                }
        });

        queue.add(request);
    }
}
