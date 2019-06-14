package com.example.imagedetector;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.phonenumberui.PhoneNumberActivity;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private String mobileNumber = "";
    private Button btnVerify;
    private static final int REQUEST_PHONE_VERIFICATION = 1080;
    private static String name;
    private static User us;
    SharedPreferences sp ;
    SharedPreferences uid ;
    SharedPreferences uname ;
    SharedPreferences nameset;
    SharedPreferences phoneno;





    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET,Manifest.permission.RECEIVE_SMS};


    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS, grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                //initialize();
                break;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("login",MODE_PRIVATE);
        uid = getSharedPreferences("uid",MODE_PRIVATE);
        uname = getSharedPreferences("uname",MODE_PRIVATE);
        nameset = getSharedPreferences("login",MODE_PRIVATE);
        phoneno = getSharedPreferences("phoneno",MODE_PRIVATE);
        Log.d("--","--");
        Intent intent = getIntent();
        int key= intent.getIntExtra("logkey",0);
        //Log.d("SHAREP",String.valueOf(key));
        if(key == 1)
        {
            sp.edit().putBoolean("logged",false).apply();
        }

        btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean i = isNetworkAvailable();
                if (i) {
                    if (sp.getBoolean("logged", false)) {
                        if (nameset.getBoolean("nameset", true)) {
                            String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            //Log.d("UIDFB in run",currentuser);
                            DatabaseReference mDatabase;
                            mDatabase = FirebaseDatabase.getInstance("https://agriexpert11.firebaseio.com").getReference();
                            DatabaseReference ref = mDatabase.child("users").child(currentuser);
                            //Query phoneQuery = ref.equalTo("JO");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    us = dataSnapshot.getValue(User.class);
                                   // Log.d("UIDFB", us.username);
                                    name = us.username;
                                    uname.edit().putString("uname", name).apply();
                                    Intent main = new Intent(MainActivity.this, Main2Activity.class);
                                    main.putExtra("key", phoneno.getString("phoneno",""));
                                    main.putExtra("name",name);
                                    //Log.d("unameF", name);
                                    startActivity(main);
                                    Toast.makeText(getApplicationContext(), "Your are Logged in as " +name, Toast.LENGTH_LONG).show();
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("UIDFB", "onCancelled", databaseError.toException());
                                }
                            });
                            nameset.edit().putBoolean("nameset", false).apply();
                        }
                        else {
                            Log.d("IN ELSE","--------------");
                            Intent main = new Intent(MainActivity.this, Main2Activity.class);
                            main.putExtra("key", phoneno.getString("phoneno",""));
                            String iname = uname.getString("uname", "");
                            main.putExtra("name",iname);
                            //Log.d("uname1", iname);
                            startActivity(main);
                            Toast.makeText(getApplicationContext(), "Your are Logged in as " +iname, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else {
                        Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                        //Optionally you can add toolbar title
                        intent.putExtra("TITLE", getResources().getString(R.string.app_name));
                        //Optionally you can pass phone number to populate automatically.
                        intent.putExtra("PHONE_NUMBER", "");
                        startActivityForResult(intent, REQUEST_PHONE_VERIFICATION);
                    }
                }
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage("Make sure you are connected to Network!.");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Please get connected to the Network", Toast.LENGTH_LONG).show();

                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PHONE_VERIFICATION:
// If mobile number is verified successfully then you get your phone number to perform further operations.
                if (data != null && data.hasExtra("PHONE_NUMBER") && data.getStringExtra("PHONE_NUMBER") != null) {
                    sp.edit().putBoolean("logged",true).apply();
                    final String phoneNumber = data.getStringExtra("PHONE_NUMBER");
                    mobileNumber = phoneNumber;
                    String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   // Log.d("UIDFB",currentuser);
                    uid.edit().putString("uid",currentuser).apply();
                    phoneno.edit().putString("phoneno",phoneNumber).apply();
                    //Log.d("UIDFBuid",uid.getString("uid",""));
                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance("https://agriexpert11.firebaseio.com").getReference();
                    DatabaseReference ref = mDatabase.child("users").child(currentuser);
                    //Query phoneQuery = ref.equalTo("JO");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           if (dataSnapshot.exists()) {
                               us = dataSnapshot.getValue(User.class);
                               //Log.d("UIDFB", us.username);
                               name = us.username;
                               uname.edit().putString("uname", name).apply();
                               Intent main = new Intent(MainActivity.this, Main2Activity.class);
                               main.putExtra("key", phoneNumber);
                               main.putExtra("name",name);
                               //Log.d("unameF", name);
                               startActivity(main);
                               Toast.makeText(getApplicationContext(), "Your are Logged in as " +name, Toast.LENGTH_LONG).show();
                               finish();
                           }
                           else
                           {
                               String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                               //Log.d("UIDFB",currentuser);
                               Intent main = new Intent(MainActivity.this, Main3Activity.class);
                               main.putExtra("key",mobileNumber);
                               main.putExtra("UID",currentuser);
                               startActivity(main);
                               finish();
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {
                           Log.e("UIDFB", "onCancelled", databaseError.toException());
                       }
                   });

                }
                else {
                    // If mobile number is not verified successfully You can hendle according to your requirement.
                    Toast.makeText(MainActivity.this,"Mobile Verification fails!",Toast.LENGTH_LONG);
                }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
