package com.cdps.proxifood;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cdps.proxifood.Comment;
import com.cdps.proxifood.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentsListAdapter extends ArrayAdapter<Comment> {

    private int resourceLayout;
    private Context mContext;

    public CommentsListAdapter(Context context, int resource, List<Comment> items) {
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

        Comment c = getItem(position);

        if (c != null) {
            TextView authorFirstname = (TextView) v.findViewById(R.id.authorFirstname_text);
            TextView authorName = (TextView) v.findViewById(R.id.authorName_text);
            RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
            TextView comment = (TextView) v.findViewById(R.id.comment_text);
            TextView date = (TextView) v.findViewById(R.id.date_text);

            if (authorFirstname != null) {
                authorFirstname.setText(c.getAuthorFirstname());
            }

            if (authorName != null) {
                authorName.setText(c.getAuthorName());
            }

            if (comment != null) {
                comment.setText(c.getComment());
            }

            if(ratingBar != null) {
                ratingBar.setRating(c.getMark());
            }

            if(date != null) {
                SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy");
                String d = dateFormat.format(c.getDate());
                dateFormat= new SimpleDateFormat("HH:mm");
                String hour = dateFormat.format(c.getDate());
                date.setText("Le " + d+ " à " + hour);
            }
        }

        return v;
    }

}