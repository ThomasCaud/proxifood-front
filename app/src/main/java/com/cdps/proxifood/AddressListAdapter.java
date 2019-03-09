package com.cdps.proxifood;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.TextView;

import com.cdps.proxifood.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AddressListAdapter extends ArrayAdapter<Address> {

    private int resourceLayout;
    private Context mContext;
    private List<Address> mListAddresses;

    public AddressListAdapter(Context context, int resource, List<Address> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        this.mListAddresses = new ArrayList<Address>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Address p = getItem(position);

        if (p != null) {

            TextView txt1 = (TextView) v.findViewById(android.R.id.text1);
            if (txt1 != null) {
                txt1.setText(p.getFormatted());
            }
        }

        return v;
    }


    public void setData(List<Address> list) {
        mListAddresses.clear();
        mListAddresses.addAll(list);
    }

    @Override
    public int getCount() {
        return mListAddresses.size();
    }

    @Nullable
    @Override
    public Address getItem(int position) {
        return mListAddresses.get(position);
    }

    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    public Address getObject(int position) {
        return mListAddresses.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = mListAddresses;
                    filterResults.count = mListAddresses.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
    }

}