package com.fleetshipdigitalboard.model;


import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConectionDetector {
    Context context;

    public ConectionDetector(Context context) {
        this.context = context;
    }

    public boolean isConnection()
    {
        ConnectivityManager connectivity=(ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if(connectivity!=null)
        {
            NetworkInfo info=connectivity.getActiveNetworkInfo();
            if(info!=null)
            {
                if(info.getState()== NetworkInfo.State.CONNECTED)
                {
                    return true;

                }
            }
        }

        return false;
    }
}
