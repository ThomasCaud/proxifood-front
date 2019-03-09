package com.cdps.proxifood;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;

import org.osmdroid.views.MapView;

public class MapCustomInfoBubble extends InfoWindow {

    private TextView default_text;
    private ListView commentsList;
    private long userId;
    private ArrayList<JSONObject> offers;
    private Context mContext;


    public MapCustomInfoBubble(Context mContext, MapView mapView, ArrayList<JSONObject> offers) {
        super(R.layout.map_infobubble, mapView);//my custom layout and my mapView
        this.offers = offers;
        this.mContext = mContext;
    }

    @Override
    public void onClose() {
        //by default, do nothing
    }

    @Override
    public void onOpen(Object item) {
        ListView offersListView = (ListView) getView().findViewById(R.id.offers_listView);
        OffersListAdapter adapter = new OffersListAdapter(mContext, R.layout.map_offer_item, offers);

        offersListView.setAdapter(adapter);
    }

}
