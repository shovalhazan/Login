package com.example.login;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {
    Context context;
    public Utils(Context context ){
        this.context= context;
    }
    //permission checks
    public boolean checkForReadContactPermission(){
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    public boolean checkForFineLocationPermission(){
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    public boolean checkForCoarseLocationPermission(){
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    public boolean checkForBackgroundLocationPermission(){
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    //number contact exist
    public boolean contactExists( String number) {
        if (number != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
            Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
            try {
                if (cur.moveToFirst()) {
                    return true;
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        } else {
            return false;
        }
    }
    //some application is installed
    public  boolean isPackageInstalled(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    //buttery life in precent
    public int getButteryLevel(){
        if (Build.VERSION.SDK_INT >= 21) {

            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        } else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;

            return (int) (batteryPct * 100);
        }
    }
    public void getCurrentVolume() {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String deviceVolume = "" + (100 * currentVolume / maxVolume);
        int intDeviceVolume = (100 * currentVolume / maxVolume);
        Log.d("pttt", "getCurrentVolume: " + currentVolume + "%"
        + deviceVolume + "  "+intDeviceVolume);


    }

    public int getMaxVolume(){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        Log.d("pttt", "getDeviceVolume: " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) + "%");
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
    //get city by specific location
    public String getCity(Location currentLocation) throws IOException {

        Log.d("pttt", "getCity: ");
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        Log.d("pttt", "getCity: " + address);
        String city = addresses.get(0).getLocality();
        Log.d("pttt", "getCity: " + city);
        return city;
    }

    public boolean isNightMode() {
        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
            return true;
        else
            return false;
    }
    public int brightnessLevel() throws Settings.SettingNotFoundException {
        ContentResolver cResolver = context.getContentResolver();
        return Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
    }
}
