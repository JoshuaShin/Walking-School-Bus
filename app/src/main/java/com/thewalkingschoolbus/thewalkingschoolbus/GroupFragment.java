package com.thewalkingschoolbus.thewalkingschoolbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GroupFragment extends android.app.Fragment {

    private static final String TAG = "GroupFragment";
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group, container, false);
        return view;

        /*
        * How to add content in fragment:
        *
        * Fragments function identical to regular activities, except it does not extend from AppCompatActivity.
        * Hence, some things such as findViewByID or executing context related code works differently.
        *
        * FindViewBYId Example
        * instead   of: Button btn = findViewById(R.id.example);
        *           do: Button btn = view.findViewById(R.id.example);
        *
        * Context Example
        * Instead   of: Toast.makeText(this, "example", Toast.LENGTH_SHORT).show()
        *           do: Toast.makeText(getActivity(), "example.", Toast.LENGTH_SHORT).show()
        *
        * If this is unclear, look at example code in MonitoringFragment.
        */
    }
}
