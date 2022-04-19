package com.silentkiller.servicecheckup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

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

    private RtmClient mRtmClient;

    private int retriesLeft = 5;

    ConnectivityManager.NetworkCallback networkCallback;
    ConnectivityManager connectivityManager;

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

        connectivityManager.unregisterNetworkCallback(networkCallback);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BootCompletedIntentReceiver.class);
        this.sendBroadcast(broadcastIntent);

        stopSelf();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        /*Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BootCompletedIntentReceiver.class);
        this.sendBroadcast(broadcastIntent);*/

        if (connectivityObserver != null && !connectivityObserver.isDisposed()) {
            connectivityObserver.dispose();
        }

        Log.e(TAG, "END");

        stopSelf();
    }

    public void start() {

        ChatManager mChatManager = AGApplication.the().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

        isStartCalled = true;
        shouldReLogin = false;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "onAvailable: " + network);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "onLost: " + network);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
            }
        };

        connectivityManager =
                (ConnectivityManager) getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);

        doLogin();

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                shouldReLogin = true;
            }
        }, 5000);
    }

    private Notification getNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    getPackageName(),
                    "Agora connected",
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
                .setContentTitle("Agora connected")   //Title message top row.
                //.setContentText(BuildConfig.VERSION_NAME)  //message when looking at the notification, second row
                .build();
    }

    private void doLogin(){
        mRtmClient.logout(new ResultCallback<Void>() { // to avoid LOGIN_ERR_ALREADY_LOGIN
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
            }
        });

        mRtmClient.login(null, "123321", new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                retriesLeft = 5;
                Log.d(TAG, "Login success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "Login failed! " + errorInfo.toString());

                if (retriesLeft > 0) {
                    retriesLeft--;
                    doLogin();
                }
            }
        });
    }
}
