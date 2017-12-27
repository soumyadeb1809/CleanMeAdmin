package com.example.soumyadeb.cleanmeadmin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private GoogleMap mMap;
    private DatabaseReference mRootRef, mDatabase;

    private ArrayList<Dustbin> dustbinList;

    private Toolbar toolbar;

    private final double VISHAKHAPATNAM_LAT = 17.6868;
    private final double VISHAKHAPATNAM_LONG = 83.2185;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dustbinList = new ArrayList<>();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = mRootRef.child("dustbins").child("GVMC");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey().toString();
                String latitude = dataSnapshot.child("latitude").getValue().toString();
                String longitude = dataSnapshot.child("longitude").getValue().toString();
                String city = dataSnapshot.child("city").getValue().toString();
                String locality;
                if (dataSnapshot.child("locality") != null)
                    locality = dataSnapshot.child("locality").getValue().toString();
                else
                    locality = "NA";
                String last_clean = dataSnapshot.child("last_clean").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String municipality = dataSnapshot.child("municipality").getValue().toString();

                Dustbin dustbin = new Dustbin(id, latitude, longitude, city, locality, last_clean, status, municipality);
                dustbinList.add(dustbin);
                int MARKER_RESOURCE=R.drawable.ic_marker_dustbin_clean;
                if(status.equals("clean")){
                    MARKER_RESOURCE = R.drawable.ic_marker_dustbin_clean;
                }
                else {
                    MARKER_RESOURCE = R.drawable.ic_marker_dustbin_full;
                }
                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                mMap.addMarker(new MarkerOptions().position(latLng).title("Dustbin ID: "+id)
                        .icon(Tools.bitmapDescriptorFromVector(MapsActivity.this, MARKER_RESOURCE)));


                Log.d("asdf", ""+dataSnapshot);
                Log.d("asdf", "id :"+id);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setPadding(0,150,0,0);

        LatLng latLng = new LatLng(VISHAKHAPATNAM_LAT, VISHAKHAPATNAM_LONG);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11);
        mMap.animateCamera(cameraUpdate);

        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

    @Override
    public void onLocationChanged(Location location) {

    }


}

