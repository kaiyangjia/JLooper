package com.jiakaiyang.jlooper.core;


import com.sun.istack.internal.Nullable;

/**
 * Created by jia on 2017/9/12.
 * The Looper for java
 */
public class JLooper {

    static final ThreadLocal<JLooper> sThreadLocal = new ThreadLocal<JLooper>();
    private static JLooper sMainLooper;  // guarded by Looper.class

    final JMessageQueue mQueue;
    final Thread mThread;


    public static void prepare() {

    }


    public static void prepareMainLooper() {

    }

    public static JLooper getMainLooper() {

        return null;
    }

    public static void loop() {

    }

    public static
    @Nullable
    JLooper myLooper() {
        return sThreadLocal.get();
    }

    public static JMessageQueue myQueue() {
        return myLooper().mQueue;
    }

    private JLooper(boolean quitAllowed) {
        mQueue = new JMessageQueue();
        mThread = Thread.currentThread();
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == mThread;
    }

/*    public void setMessageLogging(@Nullable Printer printer) {
    }*/

    public void quit() {

    }

    public void quitSafely() {
    }


    public Thread getThread() {
        return mThread;
    }


    public JMessageQueue getQueue() {
        return mQueue;
    }

    @Override
    public String toString() {
        return "Looper (" + mThread.getName() + ", tid " + mThread.getId()
                + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}
