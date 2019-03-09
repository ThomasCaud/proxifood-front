///*
//package com.cdps.proxifood;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonArrayRequest;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ProfileOffersFragment extends Fragment {
//
//    private ListView _offersListView;
//    private TextView defaultText;
//
//    private long userId;
//
//    public static ProfileOffersFragment newInstance(long userId) {
//
//        Bundle args = new Bundle();
//        args.putLong("userId", userId);
//        ProfileOffersFragment fragment = new ProfileOffersFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_profile_offers, container, false);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        initializeComponents();
//
//        if (getArguments() != null) {
//            userId = getArguments().getLong("userId");
//        }
//
//        Log.i("User id", Long.toString(userId));
//
//        RequestQueue queue = Volley.newRequestQueue(getActivity());
//
//        String url = Globals.SERVER_ADDRESS + "/users/" + userId + "/offers";
//        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        if (response.length() == 0) {
//                            _offersListView.setVisibility(View.GONE);
//                            defaultText.setVisibility(View.VISIBLE);
//                        } else {
//                            try {
//                                ArrayList<JSONObject> offers = new ArrayList<>();
//                                for (int i = 0; i < response.length(); i++) {
//                                    offers.add(response.getJSONObject(i));
//                                }
//                                OffersListAdapter adapter = new OffersListAdapter(getContext(), R.layout.offer_item, offers);
//
//                                _offersListView.setAdapter(adapter);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(Globals.TAG_SERVER_ERROR, error.getMessage());
//            }
//        }) {
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("token", Globals.getSessionToken());
//                return params;
//            }
//        };
//
//        queue.add(request);
//
//    }
//
//    public void initializeComponents() {
//        _offersListView = getActivity().findViewById(R.id.offers_listView);
//        defaultText = getActivity().findViewById(R.id.default_offers_text);
//    }
//
//}
//*/
