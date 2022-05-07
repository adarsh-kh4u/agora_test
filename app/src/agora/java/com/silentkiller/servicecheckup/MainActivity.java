package com.silentkiller.servicecheckup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;

public class MainActivity extends AppCompatActivity {

    final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text_tv);
        textView.setText("Contains just Agora RTM module. No background/foreground service");

        doLogin();
    }

    private void doLogin(){
        ChatManager mChatManager = AGApplication.the().getChatManager();
        RtmClient mRtmClient = mChatManager.getRtmClient();

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
                Log.d(TAG, "Login success! - " + uniqueID);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.e(TAG, "Login failed! " + errorInfo.toString());
            }
        });
    }
}