package com.cdps.proxifood;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.cdps.proxifood.Globals.SESSION_TOKEN;

public class BookFragment extends Fragment {
    private static final String TAG = "[BookFragment] ";

    private JSONObject post;
    private long postId;
    private ProgressDialog progressDialog;
    private EditText hostText;
    private EditText mealNameText;
    private EditText mealDescriptionText;
    private EditText mealPriceText;
    private Spinner nbParticipantsSpinner;
    private ArrayList<Integer> arraySpinner;
    private ArrayAdapter<Integer> spinnerAdapter;
    private int totalPlaces;
    private int availablePlaces;
    private EditText mealEndOfInscriptionText;
    private EditText mealDateText;
    private Switch cookTogetherSwitch;
    private boolean cookWithGuests;
    private boolean cookWithHost;
    private Button contactHostButton;
    private Button bookButton;

    public BookFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_book));

        hostText = getView().findViewById(R.id.input_host);
        mealNameText = getView().findViewById(R.id.input_meal_name);
        mealDescriptionText = getView().findViewById(R.id.input_meal_description);
        nbParticipantsSpinner = getView().findViewById(R.id.spinner_meal_nb_participants);
        mealPriceText = getView().findViewById(R.id.input_meal_price);
        mealDateText = getView().findViewById(R.id.input_meal_date);
        mealEndOfInscriptionText = getView().findViewById(R.id.input_meal_end_of_inscription);
        cookTogetherSwitch = getView().findViewById(R.id.switch_cook_together);
        contactHostButton = getView().findViewById(R.id.button_contact_host);
        bookButton = getView().findViewById(R.id.button_book);
        progressDialog = new ProgressDialog(getActivity());

        cookTogetherSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cookWithHost = isChecked;
            }
        });


        Bundle args = getArguments();

        try {
            post = new JSONObject(args.getString("post"));
            JSONObject creator = post.getJSONObject("creator");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            postId = post.getLong("id");
            hostText.setText(creator.getString("firstName") + " " + creator.getString("lastName") + " - " + creator.getString("login"));
            mealNameText.setText(post.getString("meal"));
            mealDescriptionText.setText(post.getString("description"));

            arraySpinner = new ArrayList<Integer>();
            totalPlaces = Integer.parseInt(post.getString("nbPlaces"));
            getAvailablePlaces();

            Spinner s = (Spinner) getView().findViewById(R.id.spinner_meal_nb_participants);
            spinnerAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, arraySpinner);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(spinnerAdapter);
            spinnerAdapter.notifyDataSetChanged();

            mealPriceText.setText(post.getString("price"));
            String dateStr = post.getString("date");
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            df.setTimeZone(tz);
            Date dateMeal  =  df.parse(dateStr);
            DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
            String day = df2.format(dateMeal);
            DateFormat df3 = new SimpleDateFormat("HH:mm");
            String time = df3.format(dateMeal);
            mealDateText.setText(day + " " + time);

            dateStr = post.getString("endOfInscription");
            Date dateEndInscriptions  =  df.parse(dateStr);
            day = df2.format(dateEndInscriptions);
            time = df3.format(dateEndInscriptions);
            mealEndOfInscriptionText.setText(day + " " + time);
            cookWithGuests = post.getBoolean("allowHelpCooking");
            cookTogetherSwitch.setChecked(cookWithGuests);
        }
        catch(JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hostText.setEnabled(false);
        mealNameText.setEnabled(false);
        mealDescriptionText.setEnabled(false);
        mealPriceText.setEnabled(false);
        mealEndOfInscriptionText.setEnabled(false);
        mealDateText.setEnabled(false);
        if(!cookWithGuests)
            ((LinearLayout)getView().findViewById(R.id.switch_cook_together_layout)).setVisibility(View.INVISIBLE);

        contactHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactHost();
            }
        });

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book();
            }
        });
    }

    public void getAvailablePlaces() {
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/offers/" + postId + "/applications";
        JsonArrayRequest request2 = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int bookings = 0;

                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject booking = response.getJSONObject(i);
                                bookings += booking.getInt("nbPlaces");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        availablePlaces = totalPlaces - bookings;

                        for(int i = 1; i <= availablePlaces; i++)
                            arraySpinner.add(i);

                        spinnerAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
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

        queue.add(request2);
    }

    public void contactHost() {
        Toast.makeText(getContext(), getString(R.string.successful_message_sent), Toast.LENGTH_SHORT).show();
    }

    public void book() {
        Log.d(TAG + Globals.TAG_ACTION, "Book");

        if (!validate()) {
            return;
        }

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.processing_book));
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JSONObject json = new JSONObject();
        try {
            json.put("applicantId", Globals.getUserId());
            json.put("offerId", postId);
            json.put("nbPlaces", nbParticipantsSpinner.getSelectedItem().toString());
            json.put("allowHelpCooking", cookWithHost);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(Globals.TAG_REQUEST, "Book");
        Log.i(Globals.TAG_REQUEST_DATA, json.toString());

        String url = Globals.SERVER_ADDRESS + "/applications";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        Log.i(Globals.TAG_SUCCESS, "Successful book");
                        progressDialog.dismiss();

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        BookingsFragment fragment = new BookingsFragment(false);
                        ft.replace(R.id.main_container, fragment).commit();
                        ft.addToBackStack(null);

                        BottomNavigationView bottomNavigationView;
                        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.action_post);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Globals.TAG_FAILURE, "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("token", SESSION_TOKEN);
                return params;
            }
        };
        queue.add(request);
    }

    public boolean validate() {
        boolean valid = true;

        String nbParticipants = nbParticipantsSpinner.getSelectedItem().toString();

        if (nbParticipants.isEmpty()) {
            valid = false;
        }

        return valid;
    }

}
