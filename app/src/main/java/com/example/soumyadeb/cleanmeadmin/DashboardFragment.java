package com.example.soumyadeb.cleanmeadmin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    private Button btnAddDustbin, btnViewAll;
    private TextView tvZoneName;

    private SharedPreferences sp;

    View mView;
    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        btnAddDustbin = (Button) mView.findViewById(R.id.add_dustbin);
        btnViewAll = (Button) mView.findViewById(R.id.view_all);
        tvZoneName = (TextView) mView.findViewById(R.id.tv_zone_name);

        sp = getContext().getSharedPreferences("cleanme", Context.MODE_PRIVATE);
        tvZoneName.setText(sp.getString("name","NA"));



        btnAddDustbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddDustbinActivity.class));
            }
        });

        btnViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MapsActivity.class));
            }
        });



        return mView;
    }

}
