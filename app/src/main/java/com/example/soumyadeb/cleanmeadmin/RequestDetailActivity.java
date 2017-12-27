package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    // UI instances:
    private ImageView imImage;
    private TextView tvDustbinId, tvLastCleaned, tvStatus;
    private ProgressDialog mProgress;
    private Button btnTakeAction;

    // Data members:
    private String dustbinId = null, image = null, timestamp = null, lastCleaned = null, status = null;

    // Firebase instances:
    private DatabaseReference mRootRef, mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        // Initialize UI instances:
        tvLastCleaned = (TextView) findViewById(R.id.last_cleaned);
        tvDustbinId = (TextView) findViewById(R.id.dustbin_id);
        tvStatus = (TextView) findViewById(R.id.status);
        imImage = (ImageView) findViewById(R.id.img);
        mProgress = new ProgressDialog(this);
        btnTakeAction = (Button)findViewById(R.id.btn_take_action);


        mProgress.setMessage("Loading...");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        if(intent != null){
            dustbinId = intent.getStringExtra("dustbin_id");
            image = intent.getStringExtra("image");
            timestamp = intent.getStringExtra("timestamp");

        }

        if(dustbinId != null){
            mProgress.show();
            mDatabase = mRootRef.child("dustbins").child("GVMC");

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot dustbinData = dataSnapshot.child(dustbinId);


                    lastCleaned = dustbinData.child("last_clean").getValue().toString();
                    status = dustbinData.child("status").getValue().toString();

                    SimpleDateFormat formatter = new SimpleDateFormat("DD-MM-YYYY, hh:mm:ss");

                    // Create a calendar object that will convert the date and time value in milliseconds to date.
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(lastCleaned));
                    tvLastCleaned.setText("Last Cleaned: "+formatter.format(calendar.getTime()));
                    tvDustbinId.setText("Dustbin ID: " + dustbinId);

                    tvStatus.setText("Current Status: "+status);

                    Picasso.with(RequestDetailActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.ic_launcher).into(imImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Do nothing
                        }

                        @Override
                        public void onError() {
                            Picasso.with(RequestDetailActivity.this).load(image).placeholder(R.mipmap.ic_launcher)
                                    .into(imImage);
                        }
                    });

                    mProgress.dismiss();


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mProgress.isShowing()){
                        mProgress.dismiss();
                    }
                }
            });


            // OnClick handler for Take Action button:
            btnTakeAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOptions(dustbinId);
                }
            });

        }

    }

    private void showOptions(String id) {

        mProgress.setMessage("Please wait...");

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.alert_options, null);
        Button btnReplace, btnRemove, btnMarkClean;
        btnReplace = (Button) alertLayout.findViewById(R.id.btn_replace);
        btnRemove = (Button) alertLayout.findViewById(R.id.btn_remove);
        btnMarkClean = (Button) alertLayout.findViewById(R.id.btn_mark_clean);



        final AlertDialog.Builder alertB = new AlertDialog.Builder(RequestDetailActivity.this);
        alertB.setTitle("Dustbin ID: "+id);
        // this is set the view from XML inside AlertDialog
        alertB.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alertB.setCancelable(false);
        alertB.setNegativeButton("CANCEL", null);
        //alertB.show();

        final AlertDialog alert = alertB.create();
        alert.show();




        // Replace dustbin onClick handler
        btnReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.hide();
                Intent intent = new Intent(RequestDetailActivity.this, ReplaceDustbinActivity.class);
                intent.putExtra("old_dustbin_id", dustbinId);
                startActivity(intent);
            }
        });

        // Remove dustbin onClick handler
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.hide();
                mProgress.show();
                DatabaseReference mDustbinRef = mRootRef.child("dustbins").child("GVMC");
                mDustbinRef.child(dustbinId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RequestDetailActivity.this, "Dustbin removed.", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(RequestDetailActivity.this, "Error occurred, please try again.", Toast.LENGTH_SHORT).show();
                        Log.d("asdf","Error"+ e.toString());
                        mProgress.dismiss();
                    }
                });

                DatabaseReference mFullListRef = mRootRef.child("full_dustbins").child("GVMC");
                mFullListRef.child(dustbinId).setValue(null);


            }
        });


        // Mark clean onClick handler
        btnMarkClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.hide();
                mProgress.show();

                Map requestMap = new HashMap();

                Calendar calendar = Calendar.getInstance();
                requestMap.put("dustbins/GVMC/" +dustbinId+"/status","clean");
                requestMap.put("dustbins/GVMC/" +dustbinId+"/last_clean",String.valueOf(calendar.getTimeInMillis()));

                Log.d("asdf","Dustbin Id: "+dustbinId);

                mRootRef.updateChildren(requestMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(RequestDetailActivity.this, "Dustbin marked clean.", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RequestDetailActivity.this, "Error occurred, please try again.", Toast.LENGTH_SHORT).show();
                        Log.d("asdf","Error"+ e.toString());
                        mProgress.dismiss();
                    }
                });

                DatabaseReference mFullListRef = mRootRef.child("full_dustbins").child("GVMC");
                mFullListRef.child(dustbinId).setValue(null);

            }
        });

    }
}
