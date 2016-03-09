package com.sodha.youtubesearch.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by sodha on 10/3/16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if(status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
        }
    }
}