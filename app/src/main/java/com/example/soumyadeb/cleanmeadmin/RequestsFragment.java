package com.example.soumyadeb.cleanmeadmin;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mReqList;
    private ProgressDialog mProgress;

    // Firebase Instances:
    private DatabaseReference mRootRef, mDatabaseRef;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_requests, container, false);



        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Loading data...");
        mProgress.show();

        mReqList = (RecyclerView)v.findViewById(R.id.requests);
        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(new LinearLayoutManager(getContext()));

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mDatabaseRef = mRootRef.child("full_dustbins/BMC");


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<RequestItem, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestItem, RequestViewHolder>(
                RequestItem.class,
                R.layout.request_item,
                RequestViewHolder.class,
                mDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(RequestViewHolder viewHolder, final RequestItem model, int position) {
                viewHolder.setDustbin_id(model.getDustbin_id());
                viewHolder.setImage(model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getContext(), "Dustbin ID: "+model.getDustbin_id(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), RequestDetailActivity.class);
                        intent.putExtra("dustbin_id", model.getDustbin_id());
                        intent.putExtra("image", model.getImage());
                        intent.putExtra("timestamp", model.getTimestamp());
                        startActivity(intent);
                    }
                });

                mProgress.dismiss();
            }
        };

        mReqList.setAdapter(adapter);

    }
}
