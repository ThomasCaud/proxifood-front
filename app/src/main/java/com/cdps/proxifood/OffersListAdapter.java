package com.cdps.proxifood;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class OffersListAdapter extends ArrayAdapter<JSONObject> {

    private int resourceLayout;
    private Context mContext;
    private TextView title;
    private TextView subtitle;
    private TextView date;
    private TextView nbPlaces;
    private TextView price;
    private Button btnDetails;

    public OffersListAdapter(Context context, int resource, List<JSONObject> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        final JSONObject offer = getItem(position);

        if (offer != null) {
            title = v.findViewById(R.id.titleText);
            price = v.findViewById(R.id.priceText);
            subtitle = v.findViewById(R.id.subtitleText);
            date = v.findViewById(R.id.dateText);
            nbPlaces = v.findViewById(R.id.nbPlacesText);
            btnDetails = v.findViewById(R.id.moreBtn);


            btnDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        displayBookFragment(offer.getLong("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            try {
                JSONObject creator = offer.getJSONObject("creator");
                title.setText(offer.getString("meal"));
                price.setText(offer.getString("price") + " €");
                subtitle.setText("Chez " + creator.getString("firstName"));

                String dateStr = offer.getString("date");
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                df.setTimeZone(tz);

                Date dateMeal  =  df.parse(dateStr);

                DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                String day = df2.format(dateMeal);

                DateFormat df3 = new SimpleDateFormat("HH:mm");
                String time = df3.format(dateMeal);

                date.setText(day + " à " + time);
                int maxPlaces = offer.getInt("nbPlaces");
                int acceptedPlaces = offer.getInt("nbAcceptedPeople");
                int remainingPlaces = maxPlaces - acceptedPlaces;
                nbPlaces.setText(remainingPlaces + "/" + maxPlaces);


            } catch(JSONException | ParseException e) {
                e.printStackTrace();
            }


        }

        return v;
    }

    public void displayBookFragment(long id) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = Globals.SERVER_ADDRESS + "/offers/" + id;
        Log.i("TOKEN", Globals.getSessionToken());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle args = new Bundle();
                        FragmentActivity activity = (FragmentActivity) mContext;
                        android.support.v4.app.FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        args.putString("post", response.toString());
                        BookFragment fragment = new BookFragment();
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

}