package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReplaceDustbinActivity extends AppCompatActivity {
    Toolbar toolbar;

    private TextInputLayout tilDustbinId;
    private Button btnScanQR, btnSubmit;
    private ProgressDialog mProgress;

    private String oldDustbinId, newDustbinId;

    private DatabaseReference mRootRef, mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace_dustbin);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setMessage("Please wait...");

        Intent intent = getIntent();
        if(intent!= null)
            oldDustbinId = intent.getStringExtra("old_dustbin_id");

        else {
            Toast.makeText(this, "Error occurred. Please try again", Toast.LENGTH_LONG).show();
            finish();
        }


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = mRootRef.child("dustbins").child("GVMC");



        tilDustbinId = (TextInputLayout) findViewById(R.id.til_dustbin_id);
        btnScanQR = (Button) findViewById(R.id.btn_scan);
        btnSubmit = (Button) findViewById(R.id.btn_submit);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tilDustbinId.getEditText().getText().toString() != null){

                    mProgress.show();

                    newDustbinId = tilDustbinId.getEditText().getText().toString();

                    mDatabase.child(oldDustbinId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String city, last_clean, status, locality, latitude, longitude, municipality;
                            city = dataSnapshot.child("city").getValue().toString();
                            last_clean = dataSnapshot.child("last_clean").getValue().toString();
                            status = dataSnapshot.child("status").getValue().toString();
                            locality = dataSnapshot.child("locality").getValue().toString();
                            latitude = dataSnapshot.child("latitude").getValue().toString();
                            longitude = dataSnapshot.child("longitude").getValue().toString();
                            municipality = dataSnapshot.child("municipality").getValue().toString();

                            Log.i("asdf", "city: "+city);
                            Log.i("asdf", "locality: "+locality);
                            Log.i("asdf", "municipality : "+municipality);


                            Map<String, String> data = new HashMap<String, String>();
                            data.put("latitude", latitude);
                            data.put("longitude", longitude);
                            data.put("city", city);
                            data.put("locality", locality);
                            data.put("municipality", municipality);
                            data.put("status", status);
                            data.put("last_clean", last_clean);

                            mDatabase.child(newDustbinId).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.child(oldDustbinId).setValue(null);
                                    DatabaseReference mFullListRef = mRootRef.child("full_dustbins").child("GVMC");
                                    mFullListRef.child(oldDustbinId).setValue(null);
                                    Toast.makeText(getApplicationContext(), "Dustbin replaced successfully.", Toast.LENGTH_LONG).show();
                                    finish();
                                    mProgress.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
                                    mProgress.dismiss();
                                }
                            });




                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                                    mProgress.dismiss();
                        }
                    });

                }
            }
        });

    }
}
