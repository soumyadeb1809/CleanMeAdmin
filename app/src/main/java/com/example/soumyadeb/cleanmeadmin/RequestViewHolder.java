package com.example.soumyadeb.cleanmeadmin;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


/**
 * Created by Soumya Deb on 11-07-2017.
 */

public class RequestViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public RequestViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }
    public void setDustbin_id(String dustbin_id){
        TextView textId = (TextView) mView.findViewById(R.id.dustbin_id);
        textId.setText("Dustbin ID: "+dustbin_id);
    }

    public void setDustbin_zone(String zone_name){
        TextView textZone = (TextView) mView.findViewById(R.id.dustbin_zone);
        textZone.setText("Zone: "+zone_name);
    }

    public void setImage(final String image){
        final ImageView img = (ImageView) mView.findViewById(R.id.img);

            Picasso.with(mView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.mipmap.ic_launcher).into(img, new Callback() {
                @Override
                public void onSuccess() {
                    // Do nothing
                }

                @Override
                public void onError() {
                    Picasso.with(mView.getContext()).load(image).placeholder(R.mipmap.ic_launcher)
                            .into(img);
                }
            });

        }
    }
