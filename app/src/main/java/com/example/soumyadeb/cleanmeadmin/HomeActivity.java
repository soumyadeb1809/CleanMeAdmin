package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // UI Instances:
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ProgressDialog mProgress;

    
    // Firebase instances:
    private DatabaseReference mRootRef;
    
    String dustbinId=null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_requests:
                    getSupportActionBar().setElevation(0);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, new RequestsFragment()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    getSupportActionBar().setElevation(4);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, new DashboardFragment()).commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        mProgress = new ProgressDialog(this);
        
        mRootRef = FirebaseDatabase.getInstance().getReference();


        getSupportFragmentManager().beginTransaction().replace(R.id.content, new DashboardFragment()).commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);

        fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(HomeActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setPrompt("Scan QR Code");
        integrator.setBarcodeImageEnabled(false);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Toast.makeText(this, "Scan result: " + result.getContents(), Toast.LENGTH_LONG).show();
                dustbinId = result.getContents();
                showOptions(dustbinId);
            } else {
                Toast.makeText(this, "Scan cancelled.", Toast.LENGTH_LONG).show();
            }
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



        final AlertDialog.Builder alertB = new AlertDialog.Builder(HomeActivity.this);
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
                Intent intent = new Intent(HomeActivity.this, ReplaceDustbinActivity.class);
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
                DatabaseReference mDustbinRef = mRootRef.child("dustbins").child("BMC");
                mDustbinRef.child(dustbinId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomeActivity.this, "Dustbin removed.", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(HomeActivity.this, "Error occurred, please try again.", Toast.LENGTH_SHORT).show();
                        Log.d("asdf","Error"+ e.toString());
                        mProgress.dismiss();
                    }
                });

                
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
                requestMap.put("dustbins/BMC/" +dustbinId+"/status","clean");
                requestMap.put("dustbins/BMC/" +dustbinId+"/last_clean",String.valueOf(calendar.getTimeInMillis()));

                Log.d("asdf","Dustbin Id: "+dustbinId);

                mRootRef.updateChildren(requestMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(HomeActivity.this, "Dustbin marked clean.", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Error occurred, please try again.", Toast.LENGTH_SHORT).show();
                        Log.d("asdf","Error"+ e.toString());
                        mProgress.dismiss();
                    }
                });

            }
        });

    }
}
