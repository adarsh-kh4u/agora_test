package com.silentkiller.servicecheckup;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    final String TAG = BootCompletedIntentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "Receiver " + intent.getAction());

        startService(context);
    }

    private void startService(Context context) {
        if (!isMyServiceRunning(BackgroundService.class, context)) {

            //Intent foregroundService = new Intent(context, ForegroundService.class);
            Intent backgroundService = new Intent(context, BackgroundService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(backgroundService);
            } else {
                //lower then Oreo, just start the service.
                context.startService(backgroundService);
            }
        }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
