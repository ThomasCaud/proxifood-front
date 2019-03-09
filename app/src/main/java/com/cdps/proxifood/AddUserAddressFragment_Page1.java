package com.cdps.proxifood;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.List;

public class AddUserAddressFragment_Page1 extends Fragment {

    private Spinner _countrySpinner;
    private EditText _addressText;
    private FloatingActionButton _nextButton;
    private String selectedCountry;
    private JSONObject selectedAddress;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AddressListAdapter autoSuggestAdapter;
    private boolean isSpinnerTouched = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user_address, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeComponents();

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_update_address));

        Bundle params = getArguments();
        ArrayList<String> listCountries = params.getStringArrayList("listCountries");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listCountries) //selected item will look like a spinner set from XML
        {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _countrySpinner.setAdapter(spinnerArrayAdapter);
        _countrySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });

        _countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!isSpinnerTouched) return;
                _addressText.setEnabled(true);
                selectedCountry = _countrySpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        _nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                AddUserAddressFragment_Page2 fragment = new AddUserAddressFragment_Page2();
                params.putString("JSONaddress", selectedAddress.toString());
                fragment.setArguments(params);
                ft.replace(R.id.main_container, fragment).commit();
                ft.addToBackStack(null);
            }
        });

        final AppCompatAutoCompleteTextView autoCompleteTextView =
                getActivity().findViewById(R.id.auto_complete_address);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AddressListAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<Address>());
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Address a = autoSuggestAdapter.getObject(position);
                        autoCompleteTextView.setText(a.getFormatted());
                        try {
                            selectedAddress = new JSONObject();
                            selectedAddress.put("street", a.getStreet());
                            selectedAddress.put("zipcode", a.getZipcode());
                            selectedAddress.put("city", a.getCity());
                            selectedAddress.put("country", a.getCountry());
                            selectedAddress.put("formattedAddress", a.getFormatted());
                            selectedAddress.put("longitude", a.getLongitude());
                            selectedAddress.put("latitude", a.getLatitude());
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before,
                                          int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable e) {
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });

    }


    public void initializeComponents() {
        _countrySpinner = getActivity().findViewById(R.id.list_countries);
        _addressText = getActivity().findViewById(R.id.auto_complete_address);
        _nextButton = getActivity().findViewById(R.id.btn_add_address_next);
        selectedAddress = new JSONObject();
    }

    private void makeApiCall(String text) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        Log.i("TEXT", text);
        JSONObject json = new JSONObject();
        try {
            json.put("q", "Moabit%2C%20Berlin");
            json.put("key", "cb8aa02f6c78442282a92fa33760ef96");
            json.put("language", "fr");
            Log.i(Globals.TAG_REQUEST, "Create user");
        }
        catch(JSONException e) {
            Log.i("EXCEPTION","exception");
        }

        Log.i("INFO", selectedCountry);
        //String url = "https://api.opencagedata.com/geocode/v1/json?q=92150,"+ text +"&key=cb8aa02f6c78442282a92fa33760ef96&language=fr";
        try {
            String country = URLEncoder.encode(selectedCountry, "utf-8");
            Log.i("INFO", country);
        String url = "https://www.mapquestapi.com/geocoding/v1/address?key=0UCY2i85UiMToB5kBqcLZ5upu6t3oQoO&inFormat=kvp&outFormat=json&location="+ country + "+" + text+ "&thumbMaps=false";
        Log.i("INFO", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        List<Address> addressList = new ArrayList<>();
                        try {
                            JSONArray array = response.getJSONArray("results");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject row = array.getJSONObject(i);
                                JSONArray locations = row.getJSONArray("locations");
                                for (int j = 0; j < locations.length(); j++) {
                                    JSONObject loc = locations.getJSONObject(j);
                                    Log.i("JSON", loc.toString());
                                    String postcode = null;
                                    String formatted = "";

                                    String road = null;
                                    if(loc.has("street") && loc.getString("street") != "") {
                                        road = loc.getString("street");
                                        formatted+=(road + ", ");
                                    }

                                    if(loc.has("postalCode") && loc.getString("postalCode") != "") {
                                        postcode = loc.getString("postalCode");
                                        formatted+=postcode + " ";

                                    }

                                    String town = null;
                                    if(loc.has("adminArea5")) {
                                        town = loc.getString("adminArea5");
                                        formatted+=town;
                                    }

                                    String country = null;
                                    if(loc.has("adminArea1")) {
                                        country = loc.getString("adminArea1");
                                    }

                                    double longitude = 0;
                                    double latitude = 0;

                                    if(loc.has("latLng")) {
                                        JSONObject coordinates = loc.getJSONObject("latLng");
                                        longitude = coordinates.getDouble("lng");

                                        latitude = coordinates.getDouble("lat");
                                        country = loc.getString("adminArea1");
                                    }

                                    addressList.add(new Address(postcode, town, country, road, null, formatted, longitude, latitude));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //IMPORTANT: set data here and notify
                        autoSuggestAdapter.setData(addressList);
                        autoSuggestAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(Globals.TAG_FAILURE, "ERROR API");
                    }
                });
        queue.add(request);
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }
}
