package com.bue.susanne.safeway;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by abaumgra on 28/01/2017.
 */

public class MyDialog extends DialogFragment {

    static MyDialog newInstance() {
        return new MyDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.event_choices, container, false);
        getDialog().setTitle("Enter your type of point");
        return v;
    }

}