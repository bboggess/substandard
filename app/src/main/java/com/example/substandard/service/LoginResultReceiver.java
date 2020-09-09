package com.example.substandard.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class LoginResultReceiver extends ResultReceiver {
    private LoginReceiver receiver;

    public interface LoginReceiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public LoginResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(LoginReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (null != receiver) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    @Override
    public void send(int resultCode, Bundle resultData) {
        super.send(resultCode, resultData);
    }
}
