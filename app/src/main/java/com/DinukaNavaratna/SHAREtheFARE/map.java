package com.DinukaNavaratna.SHAREtheFARE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ligl.android.widget.iosdialog.IOSDialog;
import com.ligl.android.widget.iosdialog.IOSSheetDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;


public class map extends FragmentActivity implements OnMapReadyCallback, LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private FrameLayout frameLayout, WebViewFrame;
    private ArrayList<requestInfo> requestInfo = new ArrayList<requestInfo>();
    private ArrayList<Marker> pinList = new ArrayList<Marker>();
    private Button back_btn;
    private WebView WebView;
    private ProgressBar WebViewLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        back_btn = findViewById(R.id.map_back_btn);
        frameLayout = findViewById(R.id.map_frame);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Context context = appBase.getAppContext();
        SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
        Cursor resultSet = database.rawQuery("Select * from requests WHERE timestamp > '\"+date+\" 00:00:00' AND donation_count<3 ORDER BY id;", null); //Select * from requests WHERE donation_count<3 ORDER BY id;
        resultSet.moveToFirst();
        if(resultSet.getCount() > 0) {
            do {
                requestInfo.add(new requestInfo(
                    resultSet.getInt(0),
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getInt(4),
                    resultSet.getString(5),
                    resultSet.getDouble(6),
                    resultSet.getDouble(7),
                    resultSet.getString(8)
                ));
            } while (resultSet.moveToNext());
        } else {
            new IOSDialog.Builder(map.this)
                    .setMessage("No pending requests available...")
                    .setPositiveButton("OK", null).show();
        }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent myIntent = new Intent(map.this, MainActivity.class);
                map.this.startActivity(myIntent);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(int i = 0 ; i < requestInfo.size() ; i++) {
            LatLng latLng = new LatLng(requestInfo.get(i).getLat(), requestInfo.get(i).getLng());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(requestInfo.get(i).getName()+" - "+requestInfo.get(i).getDonation_count());
            markerOptions.snippet(requestInfo.get(i).getTimestamp());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            pinList.add(mMap.addMarker(markerOptions));
            //createMarker(repairList.get(i).getLat(), repairList.get(i).getLng(), repairList.get(i).getScaleID()+"\n"+repairList.get(i).getCustomer_nic()+"\n"+repairList.get(i).getYear()+"/"+repairList.get(i).getMonth()+"/"+repairList.get(i).getDay(), "", 0);
        }
//
//        // Add a marker in Sydney and move the camera
//        LatLng sriLanka = new LatLng(7.8731, 80.7718);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, Float.parseFloat("7.5")));

        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

        googleMap.setOnInfoWindowClickListener(this);
    }

    private void buildGoogleApiClient() {
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        String str = marker.getId();
        String[] idArr = str.split("m", 2);
        final int id = Integer.parseInt(idArr[1]);
        IOSSheetDialog.SheetItem[] items = new IOSSheetDialog.SheetItem[4];
        items[0] = new IOSSheetDialog.SheetItem("Call", IOSSheetDialog.SheetItem.BLUE);
        items[1] = new IOSSheetDialog.SheetItem("Direction", IOSSheetDialog.SheetItem.BLUE);
        items[2] = new IOSSheetDialog.SheetItem("View Details", IOSSheetDialog.SheetItem.BLUE);
        items[3] = new IOSSheetDialog.SheetItem("Donate", IOSSheetDialog.SheetItem.BLUE);
        IOSSheetDialog dialog2 = new IOSSheetDialog.Builder(map.this).setData(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int optionNumber) {
                    if(optionNumber == 0){
                        String number = requestInfo.get(id).getMobile();
                        Toast.makeText(map.this, "Calling: " + number, Toast.LENGTH_SHORT).show();
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + number));
                        if (ActivityCompat.checkSelfPermission(map.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(map.this, "Please provide make call permissions for this app...", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(callIntent);
                    } else if(optionNumber == 1){
                        Toast.makeText(map.this, "Opening google maps...", Toast.LENGTH_SHORT).show();
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f", requestInfo.get(id).getLat(), requestInfo.get(id).getLng(), requestInfo.get(id).getLat(), requestInfo.get(id).getLng());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        map.this.startActivity(intent);
                    } else if(optionNumber == 2){
                        View loadView1 = LayoutInflater.from(map.this).inflate(R.layout.food_requests, frameLayout, false);
                        frameLayout.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            TransitionManager.beginDelayedTransition(frameLayout, new Slide(Gravity.TOP));
                        }
                        frameLayout.addView(loadView1);
                        back_btn.setVisibility(GONE);


                        WebView = findViewById(R.id.WebView);
                        WebViewLoading = findViewById(R.id.WebViewLoading);
                        WebViewFrame = findViewById(R.id.WebViewFrame);

                        WebView.removeAllViews();
                        WebView.setVisibility(View.INVISIBLE);
                        WebViewFrame.setVisibility(View.VISIBLE);
                        WebView.getSettings().setJavaScriptEnabled(true);
                        WebView.loadUrl("https://infotechdesigners.com/SHARE%20the%20FARE/food_request_info.php?id="+requestInfo.get(id).getId());
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                WebViewLoading.setVisibility(View.INVISIBLE);
                                WebView.setVisibility(View.VISIBLE);
                            }
                        },500);


                        Button close_info = findViewById(R.id.info_close_btn);
                        close_info.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                TransitionManager.beginDelayedTransition(frameLayout, new Slide(Gravity.TOP));
                                frameLayout.removeAllViews();
                                frameLayout.setVisibility(GONE);
                                back_btn.setVisibility(View.VISIBLE);
                                WebView.removeAllViews();
                                WebView.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else if(optionNumber == 3){
                        IOSDialog.Builder builder = new IOSDialog.Builder(map.this);
                        builder.setTitle("Enter Passcode");
                        // Set up the input
                        final EditText input = new EditText(map.this);
                        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        input.setCursorVisible(true);
                        builder.setContentView(input);
                        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Context context = appBase.getAppContext();
                                SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
                                Cursor resultSet = database.rawQuery("Select passcode from requests WHERE id="+requestInfo.get(id).getId()+";", null);
                                resultSet.moveToFirst();
                                if(resultSet.getCount() == 1) {
                                    if((resultSet.getString(0)).equals(md5(input.getText().toString()))){
                                        IOSDialog.Builder builder = new IOSDialog.Builder(map.this);
                                        builder.setTitle("Enter Food Details");
                                        // Set up the input
                                        final EditText food = new EditText(map.this);
                                        food.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        food.setSingleLine(false);
                                        food.setCursorVisible(true);
                                        builder.setContentView(food);
                                        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            String qry = "INSERT INTO donations (request_id, food) VALUES ("+requestInfo.get(id).getId()+", '" +food.getText().toString()+ "" + "');";
                                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                            nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                            insertData data = new insertData("http://infotechdesigners.com/SHARE%20the%20FARE/insert_update.php?code=159753", nameValuePairs);
                                            refreshPage();
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", null);
                                        builder.show();
                                    } else {
                                        new IOSDialog.Builder(map.this)
                                                .setTitle("Wrong Passcode. Try again...")
                                                .setMessage("Let's not cheat on feeding hungry people...")
                                                .setNegativeButton("Try Again", null)
                                                .show();
                                        Toast.makeText(context, "Wrong Passcode. Try again...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.show();
                    }
            }
        }).show();
    }



    @Override
    public void onBackPressed(){
        /*if(frameLayout.getVisibility() == View.VISIBLE){
            closeMenu();
        } else {
            Intent intent = new Intent(map.this, MainActivity.class);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            startActivity(intent);
            finish();
        }*/
    }


    public void refreshPage(){
        final String[] tables = {"requests"};
        com.DinukaNavaratna.SHAREtheFARE.dataStoring.localDB localdb = new localDB(map.this);
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
}
