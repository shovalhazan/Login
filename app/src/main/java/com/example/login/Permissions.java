package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Permissions {
    Context context;
    public Permissions(Context context ){
        this.context= context;
    }


    public boolean checkForReadContactPermission(){
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


}
