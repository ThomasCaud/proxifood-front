package com.cdps.proxifood;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class ProfileCommentsFragment extends Fragment {

    private TextView default_text;
    private ListView commentsList;
    private long userId;

    public static ProfileCommentsFragment newInstance(long userId) {

        Bundle args = new Bundle();
        args.putLong("userId", userId);
        ProfileCommentsFragment fragment = new ProfileCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_comments, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeComponents();

        if (getArguments() != null) {
            userId = getArguments().getLong("userId");
        }

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        String url = Globals.SERVER_ADDRESS + "/users/" + userId +"/receivedComments";
        Log.i("URL", url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.i("RESPONSE", response.toString());
                            ArrayList commentsArray = new ArrayList<Comment>();

                            if (response.length() == 0) {
                                commentsList.setVisibility(View.GONE);
                                default_text.setVisibility(View.VISIBLE);
                            } else {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject comment = response.getJSONObject(i);
                                    String commentText = comment.getString("message");
                                    int note = comment.getInt("note");
                                    JSONObject sender = comment.getJSONObject("sender");
                                    String authorFirstname = sender.getString("firstName");
                                    String authorName = sender.getString("lastName");
                                    Date date;
                                    Log.i("DATE", comment.get("createdAt").toString());

                                    if (comment.get("createdAt").toString() != "null") {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        String d = comment.getString("createdAt");
                                        date = format.parse(d);
                                    }
                                    else
                                        date = Calendar.getInstance().getTime();
                                    //TODO: change date with real date

                                    commentsArray.add(new Comment(authorFirstname, authorName, note, commentText, date));

                                }

                                ListView commentsListView = (ListView) getView().findViewById(R.id.comments_listView);
                                CommentsListAdapter adapter = new CommentsListAdapter(getContext(), R.layout.comment_item, commentsArray);

                                commentsListView.setAdapter(adapter);
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
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

        private void initializeComponents() {
            default_text = getView().findViewById(R.id.default_comments_text);
            commentsList = getView().findViewById(R.id.comments_listView);
        }

}
