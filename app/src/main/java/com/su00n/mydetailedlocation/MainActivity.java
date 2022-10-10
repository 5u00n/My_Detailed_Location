package com.su00n.mydetailedlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.su00n.mydetailedlocation.Location.Constraints;
import com.su00n.mydetailedlocation.Location.LocationService;
import com.su00n.mydetailedlocation.ViewData.GatheredDataActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView wifiList;
    private WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    WifiScanReceiver receiverWifi;

    private static  final int REQUEST_CODE_LOCATION_PERMISSION=2;


    Button startL, stopL,buttonScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startL= findViewById(R.id.check_data);
        stopL= findViewById(R.id.location_stop);
        wifiList = findViewById(R.id.wifiList);
        buttonScan = findViewById(R.id.scanBtn);



        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }



        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                    ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    startLocationService();
                    wifiManager.startScan();
                }
            }
        });


        startL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent i = new Intent(MainActivity.this, GatheredDataActivity.class);
               startActivity(i);

            }
        });

        stopL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationService();
            }
        });
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiScanReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }


    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(MainActivity.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
           unregisterReceiver(receiverWifi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "LOCATION permission granted", Toast.LENGTH_SHORT).show();
                     wifiManager.startScan();
                } else {
                    Toast.makeText(MainActivity.this, "LOCATION permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;

            case REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "FINE LOCATION permission granted", Toast.LENGTH_SHORT).show();
                    startLocationService();
                } else {
                    Toast.makeText(MainActivity.this, "FINE LOCATION permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager= (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager!=null){
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }


    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent= new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constraints.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"LocationService Started",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent= new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constraints.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"LocationService Stopped",Toast.LENGTH_SHORT).show();
        }
    }
}