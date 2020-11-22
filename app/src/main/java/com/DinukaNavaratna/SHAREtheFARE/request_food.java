package com.DinukaNavaratna.SHAREtheFARE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ligl.android.widget.iosdialog.IOSDialog;
import com.ligl.android.widget.iosdialog.IOSSheetDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class request_food extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private ImageView imageView;
    private String imagedata, imgQry = "";
    private Button add_location, back_btn, request;
    private Double lat = null, lng = null;
    private GoogleMap mMap;
    private RelativeLayout mapFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_food);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        getLocation();
        imageView = findViewById(R.id.imageView4);
        add_location = findViewById(R.id.add_location);
        mapFrame = findViewById(R.id.mapFrame);
        back_btn = findViewById(R.id.request_back_btn);
        request = findViewById(R.id.request);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent myIntent = new Intent(request_food.this, MainActivity.class);
                request_food.this.startActivity(myIntent);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name1 = findViewById(R.id.textView10);
                EditText mobile1 = findViewById(R.id.textView11);
                EditText comment1 = findViewById(R.id.textView12);
                EditText passcode1 = findViewById(R.id.textView9);

                String name = name1.getText().toString();
                String mobile = mobile1.getText().toString();
                String comment = comment1.getText().toString();
                String passcode = passcode1.getText().toString();

                if(lat == null || lng == null || lat == 0 || lng ==0){
                    new IOSDialog.Builder(request_food.this)
                        .setTitle("Set Location")
                        .setMessage("Providing your location helps the donators to find you easily...")
                        .setNegativeButton("Ok", null)
                        .show();
                } else {
                    if (!name.equals("") || !passcode.equals("")) {
                        if(mobile.equals("")){
                            mobile = "0";
                        }
                        if(comment.equals("")){
                            comment = "N/A";
                        }
                        String qry = "INSERT INTO requests (name, mobile, passcode, comments, lat, lng) VALUES ('" + name + "', '" + mobile + "', '" + md5(passcode) + "', '" + comment + "', " + lat + ", " + lng + ");";
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                        insertData data = new insertData("http://infotechdesigners.com/SHARE%20the%20FARE/insert_update.php?code=159753", nameValuePairs);

                        ImageUploadToServerFunction();
                        refreshPage();
                    } else {
                        new IOSDialog.Builder(request_food.this)
                            .setTitle("Please fill the details")
                            .setMessage("Providing your name helps the donators to find you easily while the passcode will be helping you to confirm the donation...")
                            .setNegativeButton("Ok", null)
                            .show();
                    }
                }
            }
        });

        add_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                }
                if (lat == null || lng == null) {
                    lat = 6.927079;
                    lng = 79.861244;
                }
                mapMarker();
                mapFrame.setVisibility(View.VISIBLE);
                mapFrame.bringToFront();
                back_btn.setVisibility(View.GONE);

                final TextView mapSearchTxt = ((TextView) findViewById(R.id.mapSearchTxt));
                Button mapSearch = findViewById(R.id.mapSearch);
                Button mapClose = findViewById(R.id.mapClose);

                mapSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                        }
                        if (Geocoder.isPresent()) {
                            try {
                                String city = ((EditText)findViewById(R.id.mapSearchTxt)).getText().toString();
                                if(!city.equals("")) {
                                    Geocoder gc = new Geocoder(request_food.this);
                                    List<Address> addresses = gc.getFromLocationName(city, 5); // get the found Address Objects

                                    List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                                    for (Address a : addresses) {
                                        if (a.hasLatitude() && a.hasLongitude()) {
                                            lat = a.getLatitude();
                                            lng = a.getLongitude();
                                            LatLng loc;
                                            loc = new LatLng(lat, lng);
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                                            Toast.makeText(request_food.this, a.getLatitude() + " || " + a.getLongitude(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            lat = 6.927079;
                                            lng = 79.861244;
                                        }
                                    }
                                } else {
                                    Toast.makeText(request_food.this, "Please enter a valid keyword!", Toast.LENGTH_SHORT).show();
                                    lat = 6.927079;
                                    lng = 79.861244;
                                }
                                mapMarker();
                            } catch (IOException e) {
                                lat = 6.927079;
                                lng = 79.861244;
                            }
                        }
                    }
                });

                mapClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                        }
                        mapFrame.setVisibility(View.GONE);
                        back_btn.setVisibility(View.VISIBLE);
                        mapSearchTxt.setText("");
                        LatLng mapCenter = mMap.getCameraPosition().target;
                        lat = mapCenter.latitude;
                        lng = mapCenter.longitude;
                        if (lat == null && lng == null) {
                            Toast.makeText(request_food.this, "Location not set. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IOSDialog.Builder(request_food.this)
                    .setMessage("Select image...")
                    .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(Intent.createChooser(takePicture, "Take Picture"), 0);
                        }
                    })
                    .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
                        }
                    })
                    .show();
            }
        });
    }

    //----------------------------- Image Upload --------------------------------------
    Bitmap bitmap;

    @Override
    protected void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);
        if ((RC == 1 || RC == 0) && RQC == RESULT_OK && I != null && I.getData() != null) {
            Uri uri = I.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStreamObject ;
                byteArrayOutputStreamObject = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
                byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
                final String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
                imagedata = ConvertImage;
            } catch (Exception e) {
                Log.i("Image Upload", e.toString());
                e.printStackTrace();
            }
        }
    }


    // ------------------------- Location ----------------------------------------------------------
    private Criteria criteria;
    public String bestProvider;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;

    private void getLocation() {

        if (isLocationEnabled(request_food.this)) {
            locationManager = (LocationManager) request_food.this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationEnabled(this);
                return;
            } else{
                locationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
            }

            //You can still do this if you like, you might get lucky:
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                this.lat = latitude;
                this.lng = longitude;
            } else {
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, (LocationListener) this);
            }
        } else {
            isLocationEnabled(request_food.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLocationEnabled(request_food.this)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationEnabled(this);
                return;
            } else {
                try {
                    locationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
                } catch (Exception e){
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates((LocationListener) this);

    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates((LocationListener) this);
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Please turn on GPS...", Toast.LENGTH_SHORT).show();
    }

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new IOSDialog.Builder(request_food.this)
                    .setTitle("Enable Location")
                    .setMessage("Providing your location helps the donators to find you easily...")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return false;
        } else {
            return true;
        }
    }
    // ------------------------- End Location ----------------------------------------------------------

    // -------------------------- Map ---------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void mapMarker() {
        if(lat == null || lng == null){
            lat = mMap.getMyLocation().getLatitude();
            lng = mMap.getMyLocation().getLongitude();
        }
        LatLng loc;
        loc = new LatLng(lat, lng);
        try {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(loc).draggable(false));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        } catch(Exception x){}
    }

    private void buildGoogleApiClient() {
    }
    // ------------------------------- Map -------------------------------------------------------


    public void ImageUploadToServerFunction(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //progressDialog = ProgressDialog.show(paymentsContainer.this,"Image is Uploading","Please Wait",false,false);
            }

            @Override
            protected void onPostExecute(String string1) {
                super.onPostExecute(string1);
                // Dismiss the progress dialog after done uploading.
                // progressDialog.dismiss();
                // Printing uploading success message coming from server on android app.
                // Toast.makeText(paymentsContainer.this,string1,Toast.LENGTH_LONG).show();
                // Setting image as transparent after done uploading.
            }

            @Override
            protected String doInBackground(Void... params) {
                if(!imagedata.equals("")) {
                    imgQry = imagedata;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("imageData", imgQry));
                insertData data = new insertData("http://infotechdesigners.com/SHARE%20the%20FARE/upload_doc.php?code=159753", nameValuePairs);
                refreshPage();
                }
                Log.i("Image", imgQry);
                return "Image Upload Completed...";
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }
    //----------------------------- End Image Upload --------------------------------------


    @Override
    public void onBackPressed() {
    }

    // ---------------------- Encryption --------------------------
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    // ---------------------- End Encryption --------------------------

    public void refreshPage(){
        final String[] tables = {"requests"};
        com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB localdb = new localDB(request_food.this);
        localdb.getAllData(tables);
        if (isOnline()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(getIntent());
                }
            }, 3000);
        } else {
            Toast.makeText(this, "Refresh failed!\nNot connected to internet.", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Refresh failed!\nNot connected to internet.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}