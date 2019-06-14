package com.example.imagedetector;

import com.phonenumberui.PhoneNumberActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;


//import com.google.api.services.storage.Storage;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.provider.Contacts.SettingsColumns.KEY;

public class Main2Activity extends AppCompatActivity {


    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private TextView textView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private int imgcount;
    private static InputStream pkc12Stream;

    private static String strData;

    private String mobileNumber = "";
    private static final int REQUEST_PHONE_VERIFICATION = 1080;

    public static String picturePath;
    public static String uname;

    ProgressDialog progressDialog1;
    ProgressDialog progressDialog;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;



    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    public final static int PICK_PHOTO_CODE = 1046;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.READ_EXTERNAL_STORAGE};


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
                    } else {
                        try {

                            //Toast.makeText(getApplicationContext(), "WELCOME!", Toast.LENGTH_SHORT).show();
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            String s = storageDir.toString();
                            s = s.replace("/Pictures", "");
                            //Log.d("aaaaa",s);
                            File file = new File(s + "/count.txt");
                            if (!file.exists()) {
                                FileWriter fileWriter = new FileWriter(file);
                                fileWriter.write("1");
                                fileWriter.close();
                            }
                        }
                        catch (Exception e) {
                        }
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
        setContentView(R.layout.activity_main2);
        checkPermissions();
        Toolbar toolbar = findViewById(R.id.too);
        toolbar.setTitle("Agri Expert");
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        strData= intent.getStringExtra("key");
        Log.d("SHAREP",strData);
        uname = intent.getStringExtra("name");
        Log.d("SHAREP",uname);

        TextView textView = (TextView) findViewById(R.id.txt1);
        textView.setText("Hi "+uname);
        TextView textView1 = (TextView) findViewById(R.id.txt);
        textView1.setText("Click the Photo or Gallery Icon to find out the disease");


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    boolean i = isNetworkAvailable();
                    if (i) {
                        //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        //startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        dispatchTakePictureIntent();
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Main2Activity.this);
                        builder1.setMessage("Make sure you are connected to Network!.");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Toast.makeText(getApplicationContext(),"Please get connected to the Network",Toast.LENGTH_LONG).show();

                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();

                    }
                }
            }
        });



        FloatingActionButton fabb = findViewById(R.id.fabb);
        fabb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    boolean i = isNetworkAvailable();
                    if (i) {
                        onPickPhoto(view);
                    }
                    else
                        {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Main2Activity.this);
                        builder1.setMessage("Make sure you are connected to Network!.");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Toast.makeText(getApplicationContext(),"Please get connected to the Network",Toast.LENGTH_LONG).show();

                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null ;
            try {
                photoFile = createImageFile();

            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.d("aaaaa",ex.getMessage());
            }
            Log.d("aaaaa","after if");

            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
                catch(Exception e)
                {
                    Log.d("aaaaa",e.getMessage());

                }
            }
        }
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                try {
                    File imgFile = new  File(currentPhotoPath);
                    Log.d("Alkamli RESULT", currentPhotoPath);
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        ImageView myImage = (ImageView) findViewById(R.id.img);
                        myImage.setImageBitmap(myBitmap);
                    }
                }
                catch(Exception e)
                {
                    Log.d("aaaaaerr", e.getMessage());
                }

                progressDialog = new ProgressDialog(Main2Activity.this);
                progressDialog.setMessage("Algorithm running...."); // Setting Message
                progressDialog.setTitle("AgriExpert"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);
                Thread thread1 = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Utility utility = new Utility("https://app-test-agri.appspot.com/api/predict");
                            int i = imgcount - 1;
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File file = new File(storageDir.getAbsolutePath()  + "/img_" + i + ".jpg");
                            Log.d("Alkamli RESULT", file.getPath());
                            utility.addFilePart("media", file);
                            String response = utility.finish();
                            Gson g = new Gson();
                            Predict pre = g.fromJson(response,Predict.class);
                            TextView textView = (TextView) findViewById(R.id.txt1);
                            textView.setText("Label: "+pre.getClassifiedLabel());
                            TextView textView1 = (TextView) findViewById(R.id.txt);
                            textView1.setText("Accuracy: "+pre.getPrediction());
                            Log.d("Alkamli RESULT", response);
                            Log.d("Alkamli RESULT", pre.getPrediction());
                            Log.d("Alkamli RESULT", pre.getClassifiedLabel());
                        } catch (Exception e) {
                            Log.d("Alkamli in activity_main for predict inside", e.getMessage());
                        }
                        progressDialog.dismiss();
                    }
                });
                thread1.start();


                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            //Your code goes here
                            pkc12Stream = getAssets().open("agriculture-ai-project-d569f33e438c.json");
                            Storage storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(pkc12Stream)).setProjectId("agriculture-ai-project").build().getService();
                            String bucketName = "agribucket";
                            Log.d("alkamli", storage.toString());
                            int i = imgcount - 1;
                            BlobId blobId = BlobId.of(bucketName, "img_" + i);
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File file = new File(storageDir.getAbsolutePath()  + "/img_" + i + ".jpg");
                            //BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpg").build();
                            Log.d("alkamli for filepath", file.getPath());
                            //File fi = new File("myfile.jpeg");
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, "PaddyData/test/"+strData+"/img_" + i + ".jpeg").setContentType("image/jpeg").build();
                            //Blob blob = storage.create(blobInfo,fileContent,"a simple blob".getBytes(UTF_8));
                            Blob blob = storage.create(blobInfo, fileContent);
                            Log.d("alkamli after blob", blob.getBucket());

                        } catch (Exception e) {
                            Log.e("alkamli in catch", e.getMessage());
                        }
                    }
                });
                thread.start();

            } catch (Exception e) {
            }
        }
        else {
            try {
                if (data != null) {
                    Uri photoUri = data.getData();
                    // Do something with the photo based on Uri
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    // Load the selected image into a preview
                    ImageView ivPreview = (ImageView) findViewById(R.id.img);
                    ivPreview.setImageBitmap(selectedImage);
                    Uri selectedImg = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImg, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    cursor.close();


                    progressDialog = new ProgressDialog(Main2Activity.this);
                    progressDialog.setMessage("Algorithm running...."); // Setting Message
                    progressDialog.setTitle("AgriExpert"); // Setting Title
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    progressDialog.show(); // Display Progress Dialog
                    progressDialog.setCancelable(false);


                    Thread thread1 = new Thread(new Runnable() {
                        String p = picturePath;

                        @Override
                        public void run() {
                            try {
                                String picturePath;
                                Utility utility = new Utility("https://app-test-agri.appspot.com/api/predict");
                                Log.d("Alkamli filepath in runnable", p);
                                File file = new File(p);
                                utility.addFilePart("media", file);
                                String response = utility.finish();
                                Gson g = new Gson();
                                Predict pre = g.fromJson(response,Predict.class);
                                TextView textView = (TextView) findViewById(R.id.txt1);
                                textView.setText("Label: "+pre.getClassifiedLabel());
                                TextView textView1 = (TextView) findViewById(R.id.txt);
                                textView1.setText("Accuracy: "+pre.getPrediction());
                                Log.d("Alkamli RESULT", response);
                                Log.d("Alkamli RESULT", pre.getPrediction());
                                Log.d("Alkamli RESULT", pre.getClassifiedLabel());
                            } catch (Exception e) {
                                Log.d("Alkamli in activity_main for predict inside", e.getMessage());
                                Log.d("Alkamli in activity_main for predict inside", e.toString());
                            }
                            progressDialog.dismiss();
                        }
                    });
                    thread1.start();
                }
            }
            catch (Exception e) {}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent main = new Intent(Main2Activity.this, MainActivity.class);
            main.putExtra("logkey",1);
            //Log.d("unameF", name);
            startActivity(main);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String s =storageDir.toString();
        s = s.replace("/Pictures","");
        File file = new File(s +"/count.txt");
        Log.d("num",file.toString());
        String num = new String(Files.readAllBytes(Paths.get(String.valueOf(file))));
        imgcount = Integer.parseInt(num);
        Log.d("num",String.valueOf(imgcount));
        File image = new File(storageDir + "/img_" +imgcount +".jpg");
        imgcount = imgcount + 1;
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(String.valueOf(imgcount));
        fileWriter.close();
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
       // Log.d("aaaaa",currentPhotoPath);
        return image;
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Main2Activity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}
