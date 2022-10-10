package com.su00n.mydetailedlocation;

import static android.content.ContentValues.TAG;

import static com.su00n.mydetailedlocation.Helpers.LocationCalculation.calculateDistance;
import static com.su00n.mydetailedlocation.Helpers.LocationCalculation.locDistance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.su00n.mydetailedlocation.Location.LocationService;
import com.su00n.mydetailedlocation.wifi.WifiDistanceData;
import com.su00n.mydetailedlocation.wifi.WifiLocationColectionModel;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;

@SuppressLint("MissingPermission")
public class WifiScanReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    StringBuilder sb;
    ListView wifiDeviceList;
    FirebaseDatabase database;
    DatabaseReference dbRef;
    private static final int DEFALT_UPDATE_INTERVAL = 1;
    private static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSION_FINE_LOCATION = 99;
    int i = 0;


    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss.SSS");
    String date;
    WifiDistanceData wfd;
    private ArrayList<WifiDistanceData> group = new ArrayList<>();
    DatabaseReference membersRef;

    public void setGroup(ArrayList<WifiDistanceData> group) {
        this.group = group;
    }

    public ArrayList<WifiDistanceData> getGroup() {
        return this.group;
    }

    public void emptyGroup() {
        this.group.clear();
    }

    public WifiScanReceiver(WifiManager wifiManager, ListView wifiDeviceList) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
        database = FirebaseDatabase.getInstance();
        //this.act = activity;
        // wfd = new WifiDistanceData();


    }

    public void onReceive(Context context, Intent intent) {

        dbRef = database.getReference("test_wifi_data");

        double lat, lon, altitu;
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();
            ArrayList<String> deviceList = new ArrayList<>();
            double distance = 0.0;

            //Looping for the
            for (ScanResult scanResult : wifiList) {

                sb.append("\n").append(scanResult.SSID).append(" - ").append(scanResult.capabilities);
                distance = calculateDistance(scanResult.level, scanResult.frequency);
                lat = LocationService.latitude;
                lon = LocationService.longitude;
                altitu = LocationService.altitude;
                deviceList.add("SSID :-   " + scanResult.SSID + "\nDistance :-   " + distance + " meter");
                 dbRef.child(scanResult.BSSID).child("actual").setValue(new WifiLocationColectionModel(scanResult.SSID, scanResult.BSSID, 0, 0, 0));
                if (lat != 0 || lon != 0) {
                    dbRef.child(scanResult.BSSID).child("wfd").child(String.valueOf(i)).setValue(new WifiDistanceData(lat, lon, altitu, distance));
                    i++;
                }

            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());
            wifiDeviceList.setAdapter(arrayAdapter);
        }
    }



}

