package com.DinukaNavaratna.SHAREtheFARE.networkService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    final public static String NET_ACTION = "NET_ACTION";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        String status = NetworkUtil.getConnectivityStatusString(context);

        Intent intentx = new Intent();
        intentx.setAction(NET_ACTION);
        intentx.putExtra("STATUS", status);
        context.sendBroadcast(intentx);
    }
}
