package com.example.substandard;

import android.app.Application;

public class SubstandardApplication extends Application {
    private static final Object LOCK = new Object();
    private static SubstandardApplication instance;

    public static SubstandardApplication getInstance() {
        if (null == instance) {
            synchronized (LOCK) {
                instance = new SubstandardApplication();
            }
        }

        return instance;
    }
}
