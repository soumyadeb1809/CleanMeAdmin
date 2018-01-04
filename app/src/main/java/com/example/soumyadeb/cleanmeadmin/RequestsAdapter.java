package com.example.soumyadeb.cleanmeadmin;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Soumya Deb on 04-01-2018.
 */

public class RequestsAdapter extends RecyclerView.Adapter<RequestViewHolder> {

    private ArrayList<RequestItem> requestsList = new ArrayList<>();
    private Activity activity;
    DatabaseReference rootRef;
    String  zoneName = "NA";

    public RequestsAdapter(ArrayList<RequestItem> requestsList, Activity activity, DatabaseReference rootRef) {
        this.requestsList = requestsList;
        this.activity = activity;
        this.rootRef = rootRef;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        
        RequestViewHolder viewHolder = new RequestViewHolder(mView);
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RequestViewHolder holder, final int position) {
        final RequestItem item = requestsList.get(position);
        holder.setDustbin_id(item.getDustbin_id());
        holder.setImage(item.getImage());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Dustbin ID: "+item.getDustbin_id(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(activity, RequestDetailActivity.class);
                intent.putExtra("dustbin_id", item.getDustbin_id());
                intent.putExtra("image", item.getImage());
                intent.putExtra("timestamp", item.getTimestamp());
                intent.putExtra("position", ""+position);
                activity.startActivity(intent);

            }
        });

        rootRef.child("municipalities").child("GVMC").child("zones").child(item.getZone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                zoneName = dataSnapshot.child("name").getValue().toString();
                holder.setDustbin_zone(zoneName);
                zoneName = "NA";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }
}
