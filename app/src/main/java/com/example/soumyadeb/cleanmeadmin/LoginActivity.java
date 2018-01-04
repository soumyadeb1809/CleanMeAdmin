package com.example.soumyadeb.cleanmeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //UI Instances:
    private Button btnLogin;
    private TextInputLayout tilUserId, tilPassword;
    private ProgressDialog mProgress;

    //Firebase instances:
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sp = getSharedPreferences("cleanme", MODE_PRIVATE);

        btnLogin = (Button) findViewById(R.id.btn_login);
        tilUserId = (TextInputLayout) findViewById(R.id.til_user_id);
        tilPassword = (TextInputLayout) findViewById(R.id.til_user_password);

        mProgress = new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = tilUserId.getEditText().getText().toString();
                String userPassword = tilPassword.getEditText().getText().toString();
                if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPassword)){
                    Toast.makeText(LoginActivity.this,"Invalid input. Please check your credentials and try again.", Toast.LENGTH_LONG).show();
                }
                else {
                    mProgress.setMessage("Verifying credentials...");
                    mProgress.show();
                    loginUser(v, userId, userPassword);
                }

            }
        });


    }


    private void loginUser(final View v, String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgress.dismiss();
                if(task.isSuccessful())
                {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    final String currentUser = mAuth.getCurrentUser().getUid().toString();

                    mDatabase.child("municipalities").child("GVMC").child("zones").child(currentUser)
                            .child("token").setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mDatabase.child("municipalities").child("GVMC").child("zones").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            String id = dataSnapshot.child("id").getValue().toString();
                                            String type = dataSnapshot.child("type").getValue().toString();
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("type", type);
                                            editor.putString("id", id);
                                            editor.putString("name", name);
                                            editor.putString("userId", currentUser);
                                            editor.commit();

                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                            finish();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this,"Something went wrong, please try again.", Toast.LENGTH_LONG).show();
                        }
                    });



                }
                else {

                    Toast.makeText(LoginActivity.this,"Login failed. Please check your credentials and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
