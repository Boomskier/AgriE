package com.example.imagedetector;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.phonenumberui.PhoneNumberActivity;

public class Main3Activity extends AppCompatActivity {
    EditText eText;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);



        btn = findViewById(R.id.btnEnter);
        eText = (EditText) findViewById(R.id.edittext);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = eText.getText().toString();
                if(str.length() == 0)
                {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(Main3Activity.this);
                    builder1.setMessage("Please Enter Your Name");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(),"Please Enter Your Name",Toast.LENGTH_LONG).show();

                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                else {
                    if (str.length() > 20) {
                        Toast.makeText(getApplicationContext(), "Only 20 Characters are allowed\nPLEASE ReEnter Your Name", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = getIntent();
                        String strDataP;
                        String strDataID;

                        strDataP = intent.getStringExtra("key");
                        strDataID = intent.getStringExtra("UID");
                        //Log.d("UIDFB", strDataID + strDataP);
                        DatabaseReference mDatabase;
                        mDatabase = FirebaseDatabase.getInstance("https://agriexpert11.firebaseio.com").getReference();
                        User user = new User(str, strDataP);
                        mDatabase.child("users").child(strDataID).setValue(user);
                        Intent main2 = new Intent(Main3Activity.this, Main2Activity.class);
                        main2.putExtra("key", strDataP);
                        main2.putExtra("name", str);
                        startActivity(main2);
                        finish();
                    }
                }
            }
        });
    }
}
