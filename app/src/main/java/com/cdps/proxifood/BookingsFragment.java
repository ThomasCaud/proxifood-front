package com.cdps.proxifood;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.android.volley.toolbox.RequestFuture;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SuppressLint("ValidFragment")
public class BookingsFragment extends Fragment {

    private ListView listBookings;
    private ArrayList<Booking> bookings;
    private BookingsListAdapter bookingsListAdapter;
    private boolean isHistoryFragment;

    @SuppressLint("ValidFragment")
    public BookingsFragment(boolean isHistoryFragment) {
        this.isHistoryFragment = isHistoryFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_bookings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_bookings));

        listBookings = ((MainActivity) getActivity()).findViewById(R.id.bookingsListView);

        // Get bookings of current user
        bookings = getBookings(this.isHistoryFragment);

        bookingsListAdapter = new BookingsListAdapter(getActivity(), R.layout.item_list_text_button, bookings);
        bookingsListAdapter.notifyDataSetChanged();

        listBookings.setAdapter(bookingsListAdapter);

        bookingsListAdapter.notifyDataSetChanged();
    }

    public ArrayList<Booking> getBookings(final boolean beforeToday) {
        final ArrayList<Booking> bookings = new ArrayList<Booking>();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/users/" + Globals.getUserId() + "/applications";
        Log.i("TOKEN", Globals.getSessionToken());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                    for(int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject booking = response.getJSONObject(i);
                            JSONObject offer = booking.getJSONObject("offer");

                            if((!beforeToday && dateTimeFormat.parse(offer.getString("date")).after(new Date())) || (beforeToday && dateTimeFormat.parse(offer.getString("date")).before(new Date()))) {
                                JSONObject creator = offer.getJSONObject("creator");
                                JSONObject address = creator.getJSONObject("homeAddress");

                                Post post = new Post();

                                Address a = new Address();
                                if(!address.getString("formattedAddress").equals("null"))
                                    a = new Address(address.getString("zipcode"), address.getString("city"), address.getString("country"),
                                        address.getString("street"), address.getString("number"), address.getString("formattedAddress"),
                                        address.getDouble("longitude"), address.getDouble("latitude"));

                                post.setId(offer.getLong("id"));
                                post.getCreator().setLogin(creator.getString("login"));
                                post.getCreator().setFirstName(creator.getString("firstName"));
                                post.getCreator().setLastName(creator.getString("lastName"));
                                post.getCreator().setAddress(a);
                                post.setMeal(offer.getString("meal"));
                                post.setDate(dateTimeFormat.parse(offer.getString("date")));
                                post.setNbPlaces(offer.getInt("nbPlaces"));
                                post.setLocation(a);

                                int totalPlaces = booking.getInt("nbPlaces");
                                boolean helpCooking = booking.getBoolean("helpCooking");
                                Status status = Status.valueOf(booking.getString("statut"));

                                Booking b = new Booking(booking.getLong("id"), new User(), post, new Date(), totalPlaces, helpCooking, status);

                                b.getUser().setId(booking.getJSONObject("applicant").getLong("id"));
                                b.getUser().setLogin(booking.getJSONObject("applicant").getString("login"));
                                b.setAskedAt(simpleDateFormat.parse(booking.getString("askedAt")));

                                bookings.add(b);
                            }

                            bookingsListAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
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

        return bookings;
    }

    public void displayReadOnlyPostFragment(long id) {
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
                        EditPostFragment fragment = new EditPostFragment(response, true);
                        fragment.setArguments(args);
                        if(isHistoryFragment)
                            ft.replace(R.id.main_container, fragment).commit();
                        else
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

    private class BookingsListAdapter extends ArrayAdapter<Booking> {
        private int layout;

        public BookingsListAdapter(@NonNull Context context, int resource, @NonNull List<Booking> objects) {
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
                        displayReadOnlyPostFragment(bookings.get(position).getPost().getId());
                    }
                });

                viewHolder.getButton().setImageResource(R.drawable.ic_message);
                viewHolder.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog dialogBox = AskOption(getItem(position).getUser().getId());
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
    }

    private AlertDialog AskOption(final long id) {
        AlertDialog dialogBox = new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.contact))
            .setMessage(getString(R.string.contact_user_confirmation))
            .setIcon(R.drawable.ic_message)
            .setPositiveButton(getString(R.string.contact), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(getContext(), getString(R.string.successful_message_sent), Toast.LENGTH_SHORT).show();
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
