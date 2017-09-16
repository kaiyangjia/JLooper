package com.jiakaiyang.jlooper.core;

/**
 * Created by jia on 2017/9/12.
 * MessageQueue for Java
 * <p>
 * see android.os.MessageQueue
 */
public class JMessageQueue {

    boolean enqueueMessage(JMessage msg, long when) {
        return true;
    }

    boolean hasMessages(JHandler h, int what, Object object) {
        return true;
    }

    boolean hasMessages(JHandler h, Runnable r, Object object) {
        return true;
    }

    void removeMessages(JHandler h, int what, Object object) {

    }

    void removeMessages(JHandler h, Runnable r, Object object) {

    }

    void removeCallbacksAndMessages(JHandler h, Object object) {

    }

}
