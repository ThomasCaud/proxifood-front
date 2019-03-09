package com.cdps.proxifood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationsFragment extends Fragment {

    private FloatingActionButton fabButton;
    private ArrayList<Post> posts;
    private InvitationsListAdapter invitationsListAdapter;
    private ProgressDialog progressDialog;
    private ListView invitationsListView;
    private TextView default_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_invitations, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_invitations));

        ListView listInvitations = ((MainActivity) getActivity()).findViewById(R.id.invitationsListView);
        fabButton = getView().findViewById(R.id.invitationsFab);
        progressDialog = new ProgressDialog(getActivity());
        invitationsListView = getView().findViewById(R.id.invitationsListView);
        default_text = getView().findViewById(R.id.default_invitations_text);

        posts = getInvitations();

        invitationsListAdapter = new InvitationsListAdapter(getActivity(), R.layout.item_list_text_button, posts);
        invitationsListAdapter.notifyDataSetChanged();

        listInvitations.setAdapter(invitationsListAdapter);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPost();
            }
        });
        invitationsListAdapter.notifyDataSetChanged();
    }

    public ArrayList<Post> getInvitations() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.processing_get_posts));
        progressDialog.show();

        final ArrayList<Post> invitations = new ArrayList<Post>();

        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/users/" + Globals.getUserId() + "/offers";
        Log.i("TOKEN", Globals.getSessionToken());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0) {
                            invitationsListView.setVisibility(View.GONE);
                            default_text.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject post = response.getJSONObject(i);
                                    JSONObject creator = post.getJSONObject("creator");

                                    final Post p = new Post();

                                    p.setId(post.getLong("id"));
                                    p.getCreator().setId(creator.getLong("id"));
                                    p.getCreator().setLogin(creator.getString("login"));
                                    p.getCreator().setFirstName(creator.getString("firstName"));
                                    p.getCreator().setLastName(creator.getString("lastName"));
                                    p.getCreator().setEmail(creator.getString("email"));
                                    p.getCreator().setDescription(creator.getString("description"));
                                    p.setAllowHelpCooking(post.getBoolean("allowHelpCooking"));
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    p.setDate(simpleDateFormat.parse(post.getString("date")));
                                    p.setEndOfInscription(simpleDateFormat.parse(post.getString("endOfInscription")));
                                    p.setCreatedAt(simpleDateFormat.parse(post.getString("createdAt")));
                                    p.setMeal(post.getString("meal"));
                                    p.setDescription(post.getString("description"));
                                    p.setNbPlaces(post.getInt("nbPlaces"));
                                    p.setPrice((float) post.getDouble("price"));

                                    String url2 = Globals.SERVER_ADDRESS + "/offers/" + post.getLong("id") + "/applications";
                                    JsonArrayRequest request2 = new JsonArrayRequest(Request.Method.GET, url2, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    int nbBookings = 0;

                                                    for (int i = 0; i < response.length(); i++) {
                                                        try {
                                                            JSONObject booking = response.getJSONObject(i);
                                                            nbBookings += booking.getInt("nbPlaces");
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    p.setNbBookings(nbBookings);
                                                    invitationsListAdapter.notifyDataSetChanged();
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

                                    invitations.add(p);

                                    invitationsListAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        progressDialog.dismiss();
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

        queue.add(request);

        return invitations;
    }

    public void displayEditPostFragment(long id) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/offers/" + id;
        Log.i("TOKEN", Globals.getSessionToken());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ((ViewManager)getParentFragment().getActivity().findViewById(R.id.tab_layout_invitations_bookings).getParent()).removeView(getParentFragment().getActivity().findViewById(R.id.tab_layout_invitations_bookings));

                        Bundle args = new Bundle();
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        args.putString("post", response.toString());
                        ViewPostFragment fragment = new ViewPostFragment();
                        fragment.setArguments(args);
                        ft.replace(R.id.invitations_bookings_container, fragment).commit();
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

    public void addPost() {
        ((ViewManager)getParentFragment().getActivity().findViewById(R.id.tab_layout_invitations_bookings).getParent()).removeView(getParentFragment().getActivity().findViewById(R.id.tab_layout_invitations_bookings));

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        PostFragment fragment = new PostFragment();
        ft.replace(R.id.invitations_bookings_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public Post deletePost(final long id) {
        final Post post = new Post();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/offers/" + id;
        Log.i("TOKEN", Globals.getSessionToken());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject creator = response.getJSONObject("creator");
                        User c = new User();

                        JSONObject address = creator.getJSONObject("homeAddress");
                        Address a = new Address();
                        if(!address.getString("formattedAddress").equals("null"))
                            a = new Address(address.getString("zipcode"), address.getString("city"), address.getString("country"),
                                address.getString("street"), address.getString("number"), address.getString("formattedAddress"),
                                address.getDouble("longitude"), address.getDouble("latitude"));

                        c.setId(response.getJSONObject("creator").getLong("id"));
                        post.setId(id);
                        post.setCreator(c);
                        post.setAllowHelpCooking(response.getBoolean("allowHelpCooking"));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        post.setDate(simpleDateFormat.parse(response.getString("date")));
                        post.setDate(simpleDateFormat.parse(response.getString("endOfInscription")));
                        post.setDate(simpleDateFormat.parse(response.getString("createdAt")));
                        post.setMeal(response.getString("meal"));
                        post.setDescription(response.getString("description"));
                        post.setNbPlaces(response.getInt("nbPlaces"));
                        post.setPrice((float)response.getDouble("price"));
                        post.setLocation(a);

                        for(Post p : posts) {
                            if(p.getId() == id) {
                                posts.remove(p);
                                Toast.makeText(getContext(), getString(R.string.successful_post_deletion), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }


                        invitationsListAdapter.remove(post);

                        invitationsListAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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

        queue.add(request);

        return post;
    }

    private class InvitationsListAdapter extends ArrayAdapter<Post> {
        private int layout;

        public InvitationsListAdapter(@NonNull Context context, int resource, @NonNull List<Post> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewButtonHolder mainViewHolder = null;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewButtonHolder viewHolder = new ViewButtonHolder();
                viewHolder.setTitle((TextView) convertView.findViewById(R.id.itemList_textView));
                viewHolder.setButton((ImageButton) convertView.findViewById(R.id.itemList_imageButton));

                viewHolder.getTitle().setText(getItem(position).toString());
                viewHolder.getTitle().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayEditPostFragment(posts.get(position).getId());
                    }
                });

                viewHolder.getButton().setImageResource(R.drawable.ic_delete);
                viewHolder.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog dialogBox = AskOption(getItem(position).getId());
                        dialogBox.show();
                    }
                });

                convertView.setTag(viewHolder);
            }
            else {
                mainViewHolder = (ViewButtonHolder) convertView.getTag();
                mainViewHolder.getTitle().setText(getItem(position).toString());
            }

            return convertView;
        }

        public void remove(@Nullable Post object) {
            super.remove(object);
        }
    }

    private AlertDialog AskOption(final long id) {
        AlertDialog dialogBox = new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_post_confirmation))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    deletePost(id);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create();

        return dialogBox;
    }
}
