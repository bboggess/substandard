package com.example.substandard;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class holding instance for application wide Executor objects, to be used for scheduling
 * tasks on a chosen thread. Uses singleton instantiation, so use getInstance() to get the
 * AppExecutor and then grab whatever Executor you need from it.
 */
public class AppExecutors {

    // Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppExecutors instance;
    private final Executor diskIO;
    private final Executor mainThread;
    private final Executor networkIO;

    public AppExecutors(Executor diskIO, Executor mainThread, Executor networkIO) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
        this.networkIO = networkIO;
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                instance = new AppExecutors(Executors.newSingleThreadExecutor(),
                        new MainThreadExecutor(),
                        Executors.newFixedThreadPool(3));
            }
        }
        return instance;
    }

    /**
     * For running disk I/O tasks, such as reading from a database, off of the main thread
     * @return disk I/O Executor
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * For running tasks on the main thread, such as updating UI. This is only needed when
     * tasks performed in background threads need to perform some action on the main thread.
     * You really should probably never use this, but knock yourself out.
     * @return Executor for main thread
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * For running tasks which require networking, such as sending GET requests to Subsonic server
     * @return network Executor
     */
    public Executor networkIo() {
        return networkIO;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
