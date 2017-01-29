package com.bue.susanne.safeway;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Susanne on 29.01.2017.
 */

public class RouteDialog extends DialogFragment {


    public static RouteDialog newInstance(String routeString) {
        RouteDialog f = new RouteDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("routeString", routeString);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String routeString = getArguments().getString("routeString");
        View v = inflater.inflate(R.layout.route_popup, container, false);
        getDialog().setTitle("Route Information");
        final TextView routeInfo = (TextView) v.findViewById(R.id.routeInfoShow);
        routeInfo.setText(routeString);
        return v;
    }
}
