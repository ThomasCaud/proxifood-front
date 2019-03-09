package com.cdps.proxifood;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "[LoginActivity] ";
    private static final int REQUEST_SIGNUP = 0;

    ProgressDialog _progressDialog;
    EditText _loginText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = this.getSharedPreferences("PROXIFOOD_USER", Context.MODE_PRIVATE);

        String login = prefs.getString("USER_LOGIN", "");
        String password = prefs.getString("USER_PWD", "");


        if (login != null && login.length()>0 && password != null && password.length()>0) {
            // Autoconnect
            initializeSplashView();
            sendLoginRequest(login, password);

        } else {
            // Display login view
            initializeLoginView();
        }

    }

    public void initializeLoginView() {
        setContentView(R.layout.activity_login);

        _progressDialog = new ProgressDialog(LoginActivity.this);
        _loginButton = findViewById(R.id.btn_login);
        _signupLink = findViewById(R.id.link_signup);
        _loginText = findViewById(R.id.input_login);
        _passwordText = findViewById(R.id.input_password);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void initializeSplashView() {
        setContentView(R.layout.splash);
    }

    public void login() {
        Log.d(TAG + Globals.TAG_ACTION, "Login");

        if (!validate()) {
            return;
        }

        _loginButton.setEnabled(false);

        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.authenticating_loader));
        _progressDialog.show();

        String login = _loginText.getText().toString();
        String password = _passwordText.getText().toString();

        sendLoginRequest(login, password);


    }

    public void sendLoginRequest(final String login, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {
            json.put("login", login);
            json.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Globals.SERVER_ADDRESS + "/auth/token";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            Log.i(TAG + Globals.TAG_SERVER_RESPONSE, response.toString());

                            if (response.has("token")) {
                                Log.i(TAG + Globals.TAG_SUCCESS, "Successful Login");
                                try {
                                    String token = response.getString("token");
                                    Log.i(TAG + Globals.TAG_TOKEN, token);
                                    saveSharedPreferences(login, password);
                                    Globals.setSessionToken(token);
                                    Globals.setUserId(response.getLong("userId"));

                                    if(_loginButton != null) {
                                        _loginButton.setEnabled(true);
                                        _progressDialog.dismiss();
                                    }

                                    Intent resultIntent = new Intent();
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Log.e(Globals.TAG_FAILURE, "No token retrieved.");
                                Toast.makeText(getBaseContext(), R.string.error_server, Toast.LENGTH_SHORT).show();                            }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        if(error.networkResponse == null) {
                            Log.i(Globals.TAG_FAILURE, "Vérifiez que le serveur est fonctionnel.");
                        }
                        else if(error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400 ) {
                            Log.i(Globals.TAG_FAILURE, "User not found");
                            _loginButton.setEnabled(true);
                            _progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                        }

                    }
        });

        // Add the request to the RequestQueue.
        queue.add(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                _loginText.setText(data.getStringExtra("login"));
                _passwordText.setText(data.getStringExtra("password"));
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void saveSharedPreferences(String login, String password) {
        SharedPreferences prefs = this.getSharedPreferences("PROXIFOOD_USER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USER_LOGIN", login);     //RESET TO DEFAULT VALUE
        editor.putString("USER_PWD", password);     //RESET TO DEFAULT VALUE
        editor.apply();
    }

    public boolean validate() {
        boolean valid = true;

        String login = _loginText.getText().toString();
        String password = _passwordText.getText().toString();

        if (login.isEmpty()) {
            _loginText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else {
            _loginText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (password.length() < 6) {
            _passwordText.setError(getString(R.string.error_incorrect_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
