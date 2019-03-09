package com.cdps.proxifood;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
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

import static com.cdps.proxifood.Globals.SESSION_TOKEN;

@SuppressLint("ValidFragment")
public class ParticipantsFragment extends Fragment {

    private long id;

    private ListView listParticipants;
    private ArrayList<Booking> bookings;
    private ParticipantsListAdapter participantsListAdapter;

    @SuppressLint("ValidFragment")
    public ParticipantsFragment(long id) {
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_participants, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_invitations));

        listParticipants = ((MainActivity) getActivity()).findViewById(R.id.participantsListView);

        bookings = getBookings();

        participantsListAdapter = new ParticipantsListAdapter(getActivity(), R.layout.item_list_text_button, bookings);
        participantsListAdapter.notifyDataSetChanged();

        listParticipants.setAdapter(participantsListAdapter);

        participantsListAdapter.notifyDataSetChanged();
    }

    public ArrayList<Booking> getBookings() {
        final ArrayList<Booking> bookings = new ArrayList<Booking>();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.SERVER_ADDRESS + "/offers/" + id + "/applications";
        Log.i("TOKEN", Globals.getSessionToken());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for(int i = 0; i < response.length(); i++) {
                        try {
                            Booking b = new Booking();

                            JSONObject booking = response.getJSONObject(i);

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                            b.setId(booking.getLong("id"));
                            b.getUser().setId(booking.getJSONObject("applicant").getLong("id"));

                            b.getUser().setLogin(booking.getJSONObject("applicant").getString("login"));
                            b.setAskedAt(simpleDateFormat.parse(booking.getString("askedAt")));
                            b.setNbPlaces(booking.getInt("nbPlaces"));
                            b.setHelpCooking(booking.getBoolean("helpCooking"));
                            b.setStatus(Status.valueOf(booking.getString("statut")));

                            bookings.add(b);

                            participantsListAdapter.notifyDataSetChanged();
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

    private class ParticipantsListAdapter extends ArrayAdapter<Booking> {
        private int layout;

        public ParticipantsListAdapter(@NonNull Context context, int resource, @NonNull List<Booking> objects) {
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

                final Booking booking = getItem(position);
                String txt = booking.getUser().getLogin() + "\n" + booking.getNbPlaces() + " - " + booking.getStatus().toString();
                viewHolder.getTitle().setText(txt);
                viewHolder.getTitle().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog dialogBox = AskOption(booking.getId());
                        dialogBox.show();
                    }
                });

                viewHolder.getButton().setImageResource(R.drawable.ic_person);
                viewHolder.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayParticipantProfile(booking.getUser().getId());
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

    public void displayParticipantProfile(long userId) {
        ((ViewManager)getParentFragment().getActivity().findViewById(R.id.tab_layout_view_post).getParent()).removeView(getParentFragment().getActivity().findViewById(R.id.tab_layout_view_post));

        Bundle args = new Bundle();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        args.putLong("userId", userId);
        ViewProfileFragment fragment = new ViewProfileFragment();
        fragment.setArguments(args);
        ft.replace(R.id.view_post_container, fragment).commit();
        ft.addToBackStack(null);
    }

    public void replyBooking(long id, String status) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JSONObject json = new JSONObject();
        try {
            json.put("statut", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(Globals.TAG_REQUEST, "Reply booking");
        Log.i(Globals.TAG_REQUEST_DATA, json.toString());

        String url = Globals.SERVER_ADDRESS + "/applications/" + id;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, json,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                    Log.i(Globals.TAG_SUCCESS, "Successful booking reply");
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

    private AlertDialog AskOption(final long id) {
        AlertDialog dialogBox = new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.accept) + " / " + getString(R.string.refuse))
            .setMessage(getString(R.string.accept_refuse_option))
            .setIcon(R.drawable.ic_message)
            .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // TODO: display the participant profile
                    // Accept booking
                    replyBooking(id, Status.ACCEPTED.toString());
                    Toast.makeText(getContext(), getString(R.string.successful_booking_acceptance), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            })
            .setNegativeButton(getString(R.string.refuse), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: display the participant profile
                    // Refuse booking
                    replyBooking(id, Status.CANCELED.toString());
                    Toast.makeText(getContext(), getString(R.string.successful_booking_refusal), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            })
            .create();

        return dialogBox;
    }

}
