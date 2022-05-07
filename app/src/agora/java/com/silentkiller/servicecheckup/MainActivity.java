package com.silentkiller.servicecheckup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text_tv);
        textView.setText("Contains just Agora RTM module. No background/foreground service");

        doLogin();
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

        String uniqueID = UUID.randomUUID().toString();

        mRtmClient.login(null, uniqueID, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                retriesLeft = 5;
                Log.d(TAG, "Login success! - " + uniqueID);
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