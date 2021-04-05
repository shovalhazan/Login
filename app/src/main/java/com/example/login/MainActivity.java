package com.example.login;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private TextInputLayout login_editLBL;
    private Button login_BTN;
    private boolean isInstalled;
    private Utils utils;
    private static final int MAX_BRIGHTNESS_LEVEL=255;
    private static final int PERMISSION_CONTACTS_REQUEST_CODE = 123;
    private static final int MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE = 124;
    private static final int PERMISSION_REGULAR_LOCATION_REQUEST_CODE = 133;
    private static final int PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 134;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private  Location currentLocation=null;
    private Boolean canLogin=true,contactPer=false,locationPer=false;
    private String input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_editLBL = findViewById(R.id.login_editLBL);
        login_BTN = findViewById(R.id.login_BTN);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        utils = new Utils(this);
        isInstalled = utils.isPackageInstalled("com.whatsapp");
        utils.getCurrentVolume();
        utils.getMaxVolume();
        login_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestForLocation();
                input = login_editLBL.getEditText().getText().toString();

            }
        });
        Log.d("pttt", "buttery Level "+ utils.getButteryLevel());
        getContacts();

    }

    /* check if there is permission to read contact- if not : move to request it*/
    private void getContacts() {
        boolean isGranted = utils.checkForReadContactPermission();

        if (!isGranted) {
            requestPermission();
            return;
        }

    }
    private void requestPermissionWithRationaleCheck(String permission,int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,permission)) {
            Log.d("pttt", "shouldShowRequestPermissionRationale = true");
            // Show user description for what we need the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    code);
        } else {
            Log.d("pttt", "shouldShowRequestPermissionRationale = false");
            openPermissionSettingDialog();
        }
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{ Manifest.permission.READ_CONTACTS},
                PERMISSION_CONTACTS_REQUEST_CODE);
    }
    private void openPermissionSettingDialog() {
        String message = "Setting screen if user have permanently d" +
                "isable the permission by clicking Don't ask again checkbox.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }
    private void requestFirstLocationPermission() {
        // Regular location permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REGULAR_LOCATION_REQUEST_CODE);
    }
    private void requestSecondLocationPermission() {
        // Background location permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE);
    }
    private void requestForLocation() {
        boolean per1 = utils.checkForCoarseLocationPermission();
        boolean per2 = utils.checkForFineLocationPermission();
        boolean per3 = android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || utils.checkForBackgroundLocationPermission();

        if (!per1  ||  !per2) {
            // if i can ask for permission
            requestFirstLocationPermission();
        } else if (!per3) {
            // if i can ask for permission
            requestSecondLocationPermission();
        } else {
            Log.d("pttt", "requestForLocation: ");
            getLocation();
        }
    }
    @SuppressLint("MissingPermission")
    private void getLocation() {
        Log.d("pttt", "getLocation: ");
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("pttt", "onSuccess: LOCATION"+location.toString());
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation = location;
                            try {
                                checkIfCanLogin();
                            } catch (IOException | Settings.SettingNotFoundException e) {
                                e.printStackTrace();
                            }
                            Log.d("pttt", "onSuccess: "+currentLocation);
                        }else {
                            return;
                        }
                    }
                });
        Log.d("pttt", "Location Success !!!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE) {
            getContacts();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CONTACTS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        contactPer=true;
                        requestForLocation();
                } else {
                    requestPermissionWithRationaleCheck(Manifest.permission.READ_CONTACTS,PERMISSION_CONTACTS_REQUEST_CODE);
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSION_REGULAR_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPer = true;
                }else {
                    requestPermissionWithRationaleCheck(Manifest.permission.ACCESS_FINE_LOCATION,PERMISSION_REGULAR_LOCATION_REQUEST_CODE);
                }
                return;
            }
            case PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE: {
                Log.d("pttt", "BACKGROUND_LOCATION_CODE");
                requestPermissionWithRationaleCheck(Manifest.permission.ACCESS_BACKGROUND_LOCATION,PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE);
                return;
            }

        }
    }

    private void checkIfCanLogin() throws IOException, Settings.SettingNotFoundException {
        Log.d("pttt", "checkIfCanLogin: "+currentLocation);
        if(!utils.contactExists("0508383176")){
            Log.d("pttt", "0508383176 NOT EXIST! ");
            Toast.makeText(this, "you have specific contact!", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else if (!utils.isPackageInstalled("com.whatsapp")){
            Log.d("pttt", "APP NOT EXIST! ");
            Toast.makeText(this, "you have Install whatsapp!", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else if(utils.getCity(currentLocation).compareTo("Giv'atayim")!=0&&utils.getCity(currentLocation).compareTo("Tel Aviv")!=0){
            Log.d("pttt", "YOU NOT IN THE LOCATION TO CONNECT ");
            Toast.makeText(this, "YOU NOT IN THE LOCATION TO CONNECT", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else if(!input.contains(Integer.toString(utils.getButteryLevel()))){
            Log.d("pttt", "BUTERRY "+utils.getButteryLevel()+"  "+Integer.toString(utils.getButteryLevel())+"  "+input);
            Toast.makeText(this, "YOU FAIL!", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else if(!utils.isNightMode()){
            Log.d("pttt", "NOT IN NIGHT MOOD! "+utils.getButteryLevel()+"  "+Integer.toString(utils.getButteryLevel())+"  "+input);
            Toast.makeText(this, "night mood!", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else if(utils.brightnessLevel()<MAX_BRIGHTNESS_LEVEL){
            Toast.makeText(this, "brightness is not good!", Toast.LENGTH_SHORT).show();
            canLogin=false;
        }
        else
            Toast.makeText(this, "GRATE !! YOU ENTER A CORRECT PASSWORD! THANKS", Toast.LENGTH_SHORT).show();
    }


}

