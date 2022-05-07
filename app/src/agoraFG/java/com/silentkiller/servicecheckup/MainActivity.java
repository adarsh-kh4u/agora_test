package com.silentkiller.servicecheckup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text_tv);
        textView.setText("This app includes Agora RTM and a foreground service to keep agora connected.\n\nKeep the phone connected to data. Press back and keep in background");

        if (!isMyServiceRunning(BackgroundService.class, MainActivity.this)) {
            Intent intent = new Intent(MainActivity.this, BackgroundService.class);
            startService(intent);
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}