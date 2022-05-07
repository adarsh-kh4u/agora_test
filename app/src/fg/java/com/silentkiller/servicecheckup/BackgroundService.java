package com.silentkiller.servicecheckup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import java.util.UUID;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BackgroundService extends Service {
    private final String TAG = BackgroundService.class.getSimpleName();

    boolean isStartCalled;
    boolean shouldReLogin = false;

    Disposable connectivityObserver;

    public BackgroundService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(2001, getNotification());
        } else {
            startForeground(1, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        start();
        return Service.START_STICKY;
        //Returning START_STICKY causes code to stick around when app activity has died.
    }

    @Override
    public void onDestroy() {

        Log.e(TAG, "onDestroy()");

        if (connectivityObserver != null && !connectivityObserver.isDisposed()) {
            connectivityObserver.dispose();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BootCompletedIntentReceiver.class);
        this.sendBroadcast(broadcastIntent);

        stopSelf();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BootCompletedIntentReceiver.class);
        this.sendBroadcast(broadcastIntent);

        if (connectivityObserver != null && !connectivityObserver.isDisposed()) {
            connectivityObserver.dispose();
        }

        Log.e(TAG, "END");

        stopSelf();
    }

    public void start() {

        if (isStartCalled) {
            return;
        }

        isStartCalled = true;
        shouldReLogin = false;

        connectivityObserver = ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    //Log.d(TAG, "Network: " + connectivity.toString());
                });
    }

    private Notification getNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    getPackageName(),
                    "FG service running",
                    NotificationManager.IMPORTANCE_LOW
            );

            final NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(getApplicationContext(), getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(true)//persistent notification!
                .setChannelId(getPackageName())
                .setContentTitle("FG service running")   //Title message top row.
                //.setContentText(BuildConfig.VERSION_NAME)  //message when looking at the notification, second row
                .build();
    }

}
