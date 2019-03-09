package com.cdps.proxifood;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "SignupActivity";

    private ProgressDialog _progressDialog;
    private EditText _nameText;
    private EditText _firstnameText;
    private EditText _loginText;
    private EditText _emailText;
    private EditText _passwordText;
    private EditText _birthText;
    private Button _signupButton;
    private TextView _loginLink;
    private int mYear, mMonth, mDay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _progressDialog = new ProgressDialog(SignupActivity.this);
        _nameText = findViewById(R.id.input_name);
        _firstnameText = findViewById(R.id.input_firstname);
        _loginText = findViewById(R.id.input_login);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _birthText = findViewById(R.id.input_birth);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);


        _birthText.setOnTouchListener(this);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(Globals.TAG_ACTION, "Sign up");

        if (!validate()) {
            return;
        }

        _signupButton.setEnabled(false);

        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage("Création de compte...");
        _progressDialog.show();

        String firstname = _firstnameText.getText().toString();
        String name = _nameText.getText().toString();
        final String login = _loginText.getText().toString();
        String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, mYear);
        cal.set(Calendar.MONTH, mMonth);
        cal.set(Calendar.DAY_OF_MONTH, mDay);

        Date birthday = cal.getTime();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject json = new JSONObject();
        try {
            json.put("firstName", firstname);
            json.put("lastName", name);
            json.put("login", login);
            json.put("email", email);
            json.put("password",password);
            json.put("dateOfBirth",df.format(birthday));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(Globals.TAG_REQUEST, "Create user");
        Log.i(Globals.TAG_REQUEST_DATA, json.toString());

        String url = Globals.SERVER_ADDRESS + "/users";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        Log.i(Globals.TAG_SUCCESS, "Successful Sign up");
                        _signupButton.setEnabled(true);

                        Intent result = new Intent();

                        result.putExtra("login", login);
                        result.putExtra("password", password);
                        setResult(RESULT_OK, result);
                        _progressDialog.dismiss();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(Globals.TAG_FAILURE, "Error code : " + Integer.toString(error.networkResponse.statusCode));
                        if(error.networkResponse.statusCode == 400 ) {
                            NetworkResponse networkResponse = error.networkResponse;
                            try {
                                JSONObject response = new JSONObject(new String(networkResponse.data));
                                Log.i("JSON", new String(networkResponse.data));

                                if(response.getInt("code") == 1) {
                                    Log.i(Globals.TAG_FAILURE, "Login already used");
                                    _loginText.setError(getString(R.string.login_already_used));
                                }
                                else if (response.getInt("code") == 2) {
                                    Log.i(Globals.TAG_FAILURE, "Email already used");
                                    _emailText.setError(getString(R.string.email_already_used));
                                }
                                _signupButton.setEnabled(true);
                                _progressDialog.dismiss();
                            }
                            catch(JSONException e) {
                                Log.e(Globals.TAG_FAILURE, e.getMessage());
                            }

                        }

                    }
        });
        queue.add(request);
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {

        final int action = e.getAction();

        if(action == MotionEvent.ACTION_DOWN) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, -18);
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            mDay = dayOfMonth;
                            mMonth = monthOfYear;
                            mYear = year;

                            _birthText.setText(String.format("%02d-%02d", dayOfMonth, (monthOfYear + 1)) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            datePickerDialog.show();
        }
        return super.onTouchEvent(e);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String firstname = _firstnameText.getText().toString();
        String login = _loginText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String birthday = _birthText.getText().toString();

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

        // Check login
        if (login.isEmpty() || login.length() < 3) {
            _loginText.setError(getString(R.string.error_invalid_login));
            valid = false;
        } else {
            _loginText.setError(null);
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

        // Check birthday
        if (birthday.isEmpty()) {
            _birthText.setError(getString(R.string.error_field_required));
            valid = false;
        } else {
            _birthText.setError(null);
        }

        return valid;
    }
}