package com.example.soumyadeb.cleanmeadmin;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mReqList;
    private ProgressDialog mProgress;

    // Firebase Instances:
    private DatabaseReference mRootRef, mDatabaseRef;

    SharedPreferences sp;
    private String userId, type;
    public static ArrayList<RequestItem> requestArray = new ArrayList<>();
    public static RequestsAdapter adapter;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_requests, container, false);

        sp = getContext().getSharedPreferences("cleanme", Context.MODE_PRIVATE);
        userId = sp.getString("userId", "NA");
        type = sp.getString("type", "NA");



        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Loading data...");
        mProgress.show();

        mReqList = (RecyclerView)v.findViewById(R.id.requests);
        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(new LinearLayoutManager(getContext()));

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mDatabaseRef = mRootRef.child("full_dustbins/GVMC");

        adapter = new RequestsAdapter(requestArray, getActivity(), mRootRef);
        mReqList.setAdapter(adapter);




        receiveData();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        /*
        final FirebaseRecyclerAdapter<RequestItem, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestItem, RequestViewHolder>(
                RequestItem.class,
                R.layout.request_item,
                RequestViewHolder.class,
                mDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(RequestViewHolder viewHolder, final RequestItem model, int position) {
                if(type.equals("admin") || model.getZone().equals(userId)) {
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
                }
                else {

                }

                mProgress.dismiss();
            }
        };
        */

    }


    private void receiveData(){

        if(requestArray.size() > 0) {
            Log.i("asdf", "size: "+requestArray.size());
            requestArray = new ArrayList<>();
        }

        mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String dustbinId,image, timestamp, zone, zone_name;

                dustbinId = dataSnapshot.child("dustbin_id").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                timestamp = dataSnapshot.child("timestamp").getValue().toString();
                zone = dataSnapshot.child("zone").getValue().toString();
                zone_name = dataSnapshot.child("zone").getValue().toString();

                RequestItem item = new RequestItem(dustbinId, timestamp, image, zone, zone_name);

                if(type.equals("admin") ||zone.equals(userId)) {
                    requestArray.add(item);
                    adapter.notifyDataSetChanged();
                }

                if(mProgress.isShowing() || (requestArray.size() == 0))
                    mProgress.dismiss();


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Do nothing
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String id = dataSnapshot.child("dustbin_id").getValue().toString();
                int pos = -1;
                for(int i=0; i<requestArray.size(); i++){
                    if (requestArray.get(i).getDustbin_id().equals(id)){
                        pos = i;
                        break;
                    }
                }
                if(pos != -1) {
                    requestArray.remove(pos);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Do nothing
            }

        });

        if(requestArray.size() == 0)
            mProgress.dismiss();


    }


}
