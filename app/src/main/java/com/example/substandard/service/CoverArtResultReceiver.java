package com.example.substandard.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class CoverArtResultReceiver extends ResultReceiver {
    private CoverArtReceiver receiver;

    public interface CoverArtReceiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public CoverArtResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(CoverArtReceiver receiver) {
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
