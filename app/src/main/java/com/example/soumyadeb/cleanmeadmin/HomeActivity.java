package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
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
    private FirebaseAuth mAuth;


    String dustbinId=null;
    public static ArrayList<Zones> zoneList = new ArrayList<>();
    public static ArrayList<String> zoneNames = new ArrayList<>();

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

        mAuth = FirebaseAuth.getInstance();


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
                Toast.makeText(this, "Scan result: " + Tools.idModifier(result.getContents()), Toast.LENGTH_LONG).show();
                dustbinId = Tools.idModifier(result.getContents());

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
                DatabaseReference mDustbinRef = mRootRef.child("dustbins").child("GVMC");
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
                requestMap.put("dustbins/GVMC/" +dustbinId+"/status","clean");
                requestMap.put("dustbins/GVMC/" +dustbinId+"/last_clean",String.valueOf(calendar.getTimeInMillis()));

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


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            fetchZones();
        }


    }


    private void fetchZones(){
        mProgress.setMessage("Syncing data. Please wait...");
        mProgress.show();
        DatabaseReference zonesRef = mRootRef.child("municipalities").child("GVMC").child("zones");

        zonesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i("asdf", dataSnapshot.toString());

                String id, name, userId, type;

                type = dataSnapshot.child("type").getValue().toString();
                if(!type.equals("admin")){
                    userId = dataSnapshot.getKey().toString();
                    id = dataSnapshot.child("id").getValue().toString();
                    name = dataSnapshot.child("name").getValue().toString();
                    Zones zone = new Zones(name, id, userId, type);
                    zoneList.add(zone);
                    zoneNames.add(name);

                    Log.i("asdf", zone.getUserId());
                }
                if(mProgress.isShowing())
                    mProgress.dismiss();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Do nothing
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Do nothing
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


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.action_logout){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to log out from CLEANme Administrator?");
            builder.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAuth.signOut();
                    SharedPreferences sp = getSharedPreferences("cleanme", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("type", "");
                    editor.putString("id", "");
                    editor.putString("name", "");
                    editor.putString("userId", "");
                    editor.commit();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton("CANCEL", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }
}
