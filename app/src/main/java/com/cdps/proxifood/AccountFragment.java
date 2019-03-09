package com.cdps.proxifood;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {
    ListView _account_list_view;
    private ProgressDialog _progressDialog;

    static int VIEW_PROFILE_POSITION = 0;
    static int PROFILE_POSITION = 1;
    static int HISTORY_POSITION = 2;
    static int REVIEWS_POSITION = 3;
    static int DISCONNECT_POSITION = 4;
    static int DELETE_ACCOUNT_POSITION = 5;


    private JSONObject loggedUser;


    public AccountFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_account));

        _progressDialog = new ProgressDialog(getActivity());
        _account_list_view = getView().findViewById(R.id.account_list_view);

        String[] values = new String[] {
                getString(R.string.view_my_profile),
                getString(R.string.edit_my_profile),
                getString(R.string.booking_history),
                getString(R.string.my_reviews),
                getString(R.string.disconnect),
                getString(R.string.delete_account)
        };


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, values);
        _account_list_view.setAdapter(adapter);

        _account_list_view.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String entry = (String) _account_list_view.getItemAtPosition(position);

                if(position == PROFILE_POSITION) {
                    displayEditProfileFragment();
                }
                else if(position == HISTORY_POSITION) {
                    displayHistoryFragment();
                }
                else if(position == REVIEWS_POSITION) {
                    // TODO
                }
                else if(position == DISCONNECT_POSITION) {
                    disconnect();
                }
                else if(position == DELETE_ACCOUNT_POSITION) {
                    handleDeleteAccount();
                }
                else if(position == VIEW_PROFILE_POSITION) {
                    displayViewProfileFragment();
                }
            }
        });

    }

    public void displayEditProfileFragment() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/users/" + Globals.getUserId();
        Log.i("TOKEN", Globals.getSessionToken());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle args = new Bundle();
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        args.putString("user", response.toString());
                        EditProfileFragment fragment = new EditProfileFragment();
                        fragment.setArguments(args);
                        ft.replace(R.id.main_container, fragment).commit();
                        ft.addToBackStack(null);
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

    public void displayHistoryFragment() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        BookingsFragment fragment = new BookingsFragment(true);
        ft.replace(R.id.main_container, fragment).commit();
        ft.addToBackStack(null);
    }

    public void displayViewProfileFragment() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putLong("userId", Globals.getUserId());
        fragment.setArguments(args);
        ft.replace(R.id.main_container, fragment).commit();
        ft.addToBackStack(null);
    }

    public void disconnect() {
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.processing_logout));
        _progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/auth/token/";
        Log.i("TOKEN", Globals.getSessionToken());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        disconnectFromApp();
                        Toast.makeText(getActivity(), getString(R.string.successful_logout), Toast.LENGTH_SHORT).show();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Globals.TAG_SERVER_ERROR, error.getMessage());
                disconnectFromApp();
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

    };

    public void disconnectFromApp() {
        // Reset token
        Globals.setSessionToken("");

        // Delete shared preferences used for automatic log in
        SharedPreferences prefs = getActivity().getSharedPreferences("PROXIFOOD_USER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        _progressDialog.dismiss();
        // Close activiy
        getActivity().finish();
        startActivity(getActivity().getIntent());
    }


    public void handleDeleteAccount() {
        // Display confirmation dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Delete account");

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.delete_account_confirmation))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.delete_confirm_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        deleteAccount();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void deleteAccount() {
        _progressDialog.setIndeterminate(true);
        _progressDialog.setMessage(getString(R.string.processing_delete_account));
        _progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/users/"+Globals.getUserId();
        Log.i("URL", url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                        disconnectFromApp();
                        Toast.makeText(getActivity(), getString(R.string.successful_account_deletion), Toast.LENGTH_SHORT).show();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if(error.networkResponse != null) {
                    Log.e(Globals.TAG_SERVER_ERROR, Integer.toString(error.networkResponse.statusCode));
                }
                else {
                    Log.e(Globals.TAG_SERVER_INVALID_RESPONSE, "La réponse du serveur n'est pas au format JSON.");
                }
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

}
