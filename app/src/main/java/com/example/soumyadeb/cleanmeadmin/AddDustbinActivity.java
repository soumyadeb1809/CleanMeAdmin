package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddDustbinActivity extends AppCompatActivity {

    // Declare UI instances:
    Toolbar toolbar;
    private Button btnScanQR, btnSelectLocation, btnAddDustbin;
    private TextInputLayout tilDustbinId;
    private TextView tvLocationDetails;
    private ProgressDialog mProgress;
    private Spinner zonesSpinner;

    // Declare Firebase instances:
    private DatabaseReference mRootRef, mDatabase;


    // Declare data members:
    private LatLng latLng;
    private final int PLACE_PICKER_REQUEST = 1;
    private Place place = null;
    private String city = "NA";
    private String locality = "NA";
    ArrayAdapter zonesSpinnerAdapter;
    SharedPreferences sp;
    private String type = "NA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dustbin);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Add Dustbin");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sp = getSharedPreferences("cleanme", MODE_PRIVATE);
        type = sp.getString("type", "NA");


        // Initialize UI instances:
        btnScanQR = (Button) findViewById(R.id.scan_qr);
        btnSelectLocation = (Button) findViewById(R.id.btn_select_location);
        btnAddDustbin = (Button) findViewById(R.id.btn_add_dustbin);
        tilDustbinId = (TextInputLayout)findViewById(R.id.til_dustbin_id);
        tvLocationDetails = (TextView)findViewById(R.id.txt_location_details);
        mProgress = new ProgressDialog(this);

        zonesSpinner = (Spinner) findViewById(R.id.spinner);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = mRootRef.child("dustbins").child("GVMC");




        // Button onClick handlers:
        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(AddDustbinActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setBeepEnabled(true);
                integrator.setCameraId(0);
                integrator.setPrompt("Scan QR Code");
                integrator.setBarcodeImageEnabled(false);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });


        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(AddDustbinActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        mProgress.setMessage("Uploading data...");


        btnAddDustbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(TextUtils.isEmpty(tilDustbinId.getEditText().getText().toString()) || place == null){

                    Toast.makeText(AddDustbinActivity.this, "Please select all the details.", Toast.LENGTH_LONG).show();
                }
                else {

                    mProgress.show();

                    // Get location details from latitude and longitude:
                    List<Address> addresses = new ArrayList<>();
                    Geocoder geocoder = new Geocoder(AddDustbinActivity.this, Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        city = addresses.get(0).getLocality();
                        locality = addresses.get(0).getSubLocality();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Prepare the data to be uploaded:
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("latitude", String.valueOf(latLng.latitude));
                    data.put("longitude", String.valueOf(latLng.longitude));
                    if(city == null)
                        city = "NA";
                    if (locality == null)
                        locality = "NA";
                    data.put("city", city);
                    data.put("locality", locality);
                    data.put("municipality", "GVMC");
                    data.put("status", "clean");
                    Calendar calendar = Calendar.getInstance();
                    data.put("last_clean", String.valueOf(calendar.getTimeInMillis()));

                    int pos = zonesSpinner.getSelectedItemPosition();
                    data.put("zone", HomeActivity.zoneList.get(pos).getUserId());

                    // Upload data to the database:
                    mDatabase.child(tilDustbinId.getEditText().getText().toString()).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddDustbinActivity.this, "Dustbin added successfully.", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDustbinActivity.this, "Error occurred, please try again.", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    });
                }
            }
        });

        zonesSpinnerAdapter = new ArrayAdapter(AddDustbinActivity.this, android.R.layout.simple_spinner_item, HomeActivity.zoneNames);
        zonesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zonesSpinner.setAdapter(zonesSpinnerAdapter);

        if(!type.equals("admin")){
            int pos = Tools.getZonePos(sp.getString("id", "NA"));
            if(pos != -1)
                zonesSpinner.setSelection(pos);
            zonesSpinner.setEnabled(false);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handler for place picker activity:
        if(requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                 place = PlacePicker.getPlace(data, this);
                //String toastMsg = String.format("Place: %s", place.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                latLng = place.getLatLng();
                String address="Name:"+place.getName()+"Address: "+place.getAddress().toString();
                tvLocationDetails.setText(address);
            }
        }

        // Handler for QR Scanner activity:
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() != null) {
                    Toast.makeText(this, "Scan result: " + result.getContents(), Toast.LENGTH_LONG).show();
                    tilDustbinId.getEditText().setText(Tools.idModifier(result.getContents()));
                } else {
                    Toast.makeText(this, "Scan cancelled.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


}
