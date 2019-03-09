package com.cdps.proxifood;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.cdps.proxifood.Globals.SESSION_TOKEN;

public class PostFragment extends Fragment {
    private static final String TAG = "[PostFragment] ";

    private ProgressDialog progressDialog;
    private EditText mealNameText;
    private EditText mealDescriptionText;
    private EditText mealPriceText;
    private EditText nbParticipantsText;
    private EditText mealEndOfInscriptionText;
    private EditText mealDateText;
    private int mealDateYear, mealDateMonth, mealDateDay;
    private int mealEndOfInscriptionYear, mealEndOfInscriptionMonth, mealEndOfInscriptionDay;
    private int mealHour, mealMin, endOfInscriptionHour, endOfInscriptionMin;
    private Switch cookTogetherSwitch;
    private boolean cookTogether;
    private FloatingActionButton fabButton;

    public PostFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_post));

        mealNameText = getView().findViewById(R.id.input_meal_name);
        mealDescriptionText = getView().findViewById(R.id.input_meal_description);
        nbParticipantsText = getView().findViewById(R.id.input_meal_nb_participants);
        mealPriceText = getView().findViewById(R.id.input_meal_price);
        mealDateText = getView().findViewById(R.id.input_meal_date);
        mealEndOfInscriptionText = getView().findViewById(R.id.input_meal_end_of_inscription);
        cookTogetherSwitch = getView().findViewById(R.id.switch_cook_together);
        fabButton = getView().findViewById(R.id.fab);
        progressDialog = new ProgressDialog(getActivity());

        mealDateText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                final int action = e.getAction();

                if(action == MotionEvent.ACTION_DOWN) {

                    // Get Current Date
                    final Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    c.add(Calendar.DATE, 1);
                    mealDateYear = c.get(Calendar.YEAR);
                    mealDateMonth = c.get(Calendar.MONTH);
                    mealDateDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    mealDateDay = dayOfMonth;
                                    mealDateMonth = monthOfYear;
                                    mealDateYear = year;

                                    mealDateText.setText(String.format("%02d-%02d", dayOfMonth, (monthOfYear + 1)) + "-" + year );

                                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                mealHour = hourOfDay;
                                                mealMin = minute;
                                            }
                                        }, mealHour, mealMin, true);
                                    timePickerDialog.show();
                                }

                            }, mealDateYear, mealDateMonth, mealDateDay);

                    datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                    datePickerDialog.show();
                }

                return true;
            }
        });

        mealEndOfInscriptionText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                final int action = e.getAction();

                if(action == MotionEvent.ACTION_DOWN) {

                    // Get Current Date
                    final Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    mealEndOfInscriptionYear = c.get(Calendar.YEAR);
                    mealEndOfInscriptionMonth = c.get(Calendar.MONTH);
                    mealEndOfInscriptionDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    mealEndOfInscriptionDay = dayOfMonth;
                                    mealEndOfInscriptionMonth = monthOfYear;
                                    mealEndOfInscriptionYear = year;

                                    mealEndOfInscriptionText.setText(String.format("%02d-%02d", dayOfMonth, (monthOfYear + 1)) + "-" + year);

                                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                endOfInscriptionHour = hourOfDay;
                                                endOfInscriptionMin = minute;
                                            }
                                        }, endOfInscriptionHour, endOfInscriptionMin, true);
                                    timePickerDialog.show();
                                }

                            }, mealEndOfInscriptionYear, mealEndOfInscriptionMonth, mealEndOfInscriptionDay);

                    datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                    datePickerDialog.show();
                }

                return true;
            }
        });


        cookTogetherSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cookTogether = isChecked;
            }
        });

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post();
            }
        });

    }


    public void post() {
        Log.d(TAG + Globals.TAG_ACTION, "Post");

        if (!validate()) {
            return;
        }

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.processing_post));
        progressDialog.show();

        String name = mealNameText.getText().toString();
        String description = mealDescriptionText.getText().toString();
        String nbParticipants = nbParticipantsText.getText().toString();
        String price = mealPriceText.getText().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, mealDateYear);
        cal.set(Calendar.MONTH, mealDateMonth);
        cal.set(Calendar.DAY_OF_MONTH, mealDateDay);
        cal.set(Calendar.HOUR_OF_DAY, mealHour);
        cal.set(Calendar.MINUTE, mealMin);
        cal.set(Calendar.SECOND, 0);

        Date date = cal.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.YEAR, mealEndOfInscriptionYear);
        calEnd.set(Calendar.MONTH, mealEndOfInscriptionMonth);
        calEnd.set(Calendar.DAY_OF_MONTH, mealEndOfInscriptionDay);
        calEnd.set(Calendar.HOUR_OF_DAY, endOfInscriptionHour);
        calEnd.set(Calendar.MINUTE, endOfInscriptionMin);
        calEnd.set(Calendar.SECOND, 0);

        Date dateEndOfInscription = calEnd.getTime();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JSONObject json = new JSONObject();
        try {
            json.put("creatorId", Globals.getUserId());
            json.put("meal", name);
            json.put("description", description);
            json.put("price", Float.parseFloat(price));
            json.put("nbPlaces", Integer.parseInt(nbParticipants));
            json.put("allowHelpCooking", cookTogether);
            json.put("date", df.format(date));
            json.put("endOfInscription", df.format(dateEndOfInscription));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(Globals.TAG_REQUEST, "Create post");
        Log.i(Globals.TAG_REQUEST_DATA, json.toString());

        String url = Globals.SERVER_ADDRESS + "/offers";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        Log.i(Globals.TAG_SUCCESS, "Successful post creation");
                        fabButton.setEnabled(true);
                        progressDialog.dismiss();

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        InvitationsFragment fragment = new InvitationsFragment();
                        ft.replace(R.id.post_container, fragment).commit();
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

        String name = mealNameText.getText().toString();
        String description = mealDescriptionText.getText().toString();
        String date = mealDateText.getText().toString();
        String endOfInscription = mealEndOfInscriptionText.getText().toString();
        String nbParticipants = nbParticipantsText.getText().toString();

        if (name.isEmpty()) {
            mealNameText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            mealNameText.setError(null);
        }

        if (description.isEmpty()) {
            mealDescriptionText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            mealDescriptionText.setError(null);
        }

        if (date.isEmpty()) {
            mealDateText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            mealDateText.setError(null);
        }

        if (endOfInscription.isEmpty()) {
            mealEndOfInscriptionText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            mealEndOfInscriptionText.setError(null);
        }

        // Check if endOfInscription <= date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date mealDateFormat = simpleDateFormat.parse(date);
            Date mealEndOfInscriptionFormat = simpleDateFormat.parse(endOfInscription);

            if(mealEndOfInscriptionFormat.compareTo(mealDateFormat) > 0) {
                mealEndOfInscriptionText.setError(getString(R.string.error_date_before));
                valid = false;
            }
            else {
                mealEndOfInscriptionText.setError(null);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (nbParticipants.isEmpty()) {
            valid = false;
        }

        return valid;
    }
}
