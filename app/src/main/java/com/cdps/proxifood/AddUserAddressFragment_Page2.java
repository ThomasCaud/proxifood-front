package com.cdps.proxifood;

import android.app.ProgressDialog;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddUserAddressFragment_Page2 extends Fragment {

    private ProgressDialog _progressDialog;
    private EditText _streetNumberText;
    private EditText _streetText;
    private EditText _zipcodeText;
    private EditText _cityText;
    private EditText _complementaryText;

    private FloatingActionButton _confirmProfileButton;

    private JSONObject user;
    private JSONObject addressJSON;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user_address_2, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeComponents();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_update_address));

        try {
            Bundle params = getArguments();
            addressJSON = new JSONObject(params.getString("JSONaddress"));
            Log.i("ADDRESS", addressJSON.toString());
            _streetText.setText(addressJSON.getString("street"));
            _zipcodeText.setText(addressJSON.getString("zipcode"));
            _cityText.setText(addressJSON.getString("city"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _confirmProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

    }


    public void initializeComponents() {
        _progressDialog = new ProgressDialog(getActivity());
        _streetNumberText = getActivity().findViewById(R.id.street_number_input);
        _streetText = getActivity().findViewById(R.id.street_input);
        _zipcodeText = getActivity().findViewById(R.id.zipcode_input);
        _cityText = getActivity().findViewById(R.id.city_input);
        _complementaryText = getActivity().findViewById(R.id.complementary_input);
        _confirmProfileButton = getActivity().findViewById(R.id.btn_confirm_address);
    }

    public void saveProfile() {
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage("Enregistrement de l'adresse...");
        _progressDialog.show();

        JSONObject address = new JSONObject();
        long userId = -1;
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        try {
            userId = Globals.getUserId();
            address.put("zipcode", addressJSON.getString("zipcode"));
            address.put("city", addressJSON.getString("city"));
            address.put("country", addressJSON.getString("country"));
            address.put("street", addressJSON.getString("street"));
            address.put("number", _streetNumberText.getText().toString());
            address.put("complementary", _complementaryText.getText().toString());
            address.put("formattedAddress", addressJSON.getString("formattedAddress"));
            address.put("longitude", addressJSON.getDouble("longitude"));
            address.put("latitude", addressJSON.getDouble("latitude"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(Globals.TAG_REQUEST, "Update user profile");
        Log.i(Globals.TAG_REQUEST_DATA, address.toString());

        String url = Globals.SERVER_ADDRESS + "/users/" + userId + "/address";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, address,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        _progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Adresse enregistrée", Toast.LENGTH_SHORT).show();
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        AccountFragment fragment = new AccountFragment();
                        ft.replace(R.id.main_container, fragment).commit();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        _progressDialog.dismiss();
                        Log.e(Globals.TAG_FAILURE, "Error code : " + Integer.toString(error.networkResponse.statusCode));

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Globals.getSessionToken());
                return params;
            };
        };
        queue.add(request);

    }
}
