package com.cdps.proxifood;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.arch.core.util.Function;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ViewButtonHolder {
    private TextView title;
    private ImageButton button;

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public ImageButton getButton() {
        return button;
    }

    public void setButton(ImageButton button) {
        this.button = button;
    }
}
