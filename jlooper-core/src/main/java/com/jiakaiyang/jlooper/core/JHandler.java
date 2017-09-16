package com.jiakaiyang.jlooper.core;

/**
 * Created by jia on 2017/9/12.
 * The JHandler for Java
 * <p>
 * see android.os.Handler
 */
public class JHandler {

    /*
     * Set this flag to true to detect anonymous, local or member classes
     * that extend this JHandler class and that are not static. These kind
     * of classes can potentially create leaks.
     */
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    private static final String TAG = "JHandler";


    public interface Callback {
        public boolean handleMessage(JMessage msg);
    }

    /**
     * Subclasses must implement this to receive messages.
     */
    public void handleMessage(JMessage msg) {
    }

    /**
     * Handle system messages here.
     */
    public void dispatchMessage(JMessage msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }

    public JHandler() {
        this(null, false);
    }

    public JHandler(Callback callback) {
        this(callback, false);
    }

    public JHandler(JLooper looper) {
        this(looper, null, false);
    }


    public JHandler(JLooper looper, Callback callback) {
        this(looper, callback, false);
    }


    public JHandler(boolean async) {
        this(null, async);
    }

    public JHandler(Callback callback, boolean async) {
        if (FIND_POTENTIAL_LEAKS) {
            // TODO: 2017/9/16 do some log
        }

        mLooper = JLooper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
                    "Can't create handler inside thread that has not called JLooper.prepare()");
        }
        mQueue = mLooper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }

    public JHandler(JLooper looper, Callback callback, boolean async) {
        mLooper = looper;
        mQueue = looper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }

    /**
     * {@hide}
     */
    public String getTraceName(JMessage message) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(": ");
        if (message.callback != null) {
            sb.append(message.callback.getClass().getName());
        } else {
            sb.append("#").append(message.what);
        }
        return sb.toString();
    }

    public String getMessageName(JMessage message) {
        if (message.callback != null) {
            return message.callback.getClass().getName();
        }
        return "0x" + Integer.toHexString(message.what);
    }


    public final JMessage obtainMessage() {
        return JMessage.obtain(this);
    }

    public final JMessage obtainMessage(int what) {
        return JMessage.obtain(this, what);
    }

    public final JMessage obtainMessage(int what, Object obj) {
        return JMessage.obtain(this, what, obj);
    }

    public final JMessage obtainMessage(int what, int arg1, int arg2) {
        return JMessage.obtain(this, what, arg1, arg2);
    }

    public final JMessage obtainMessage(int what, int arg1, int arg2, Object obj) {
        return JMessage.obtain(this, what, arg1, arg2, obj);
    }

    public final boolean post(Runnable r) {
        return sendMessageDelayed(getPostMessage(r), 0);
    }

    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r), uptimeMillis);
    }


    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r, token), uptimeMillis);
    }


    public final boolean postDelayed(Runnable r, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r), delayMillis);
    }


    public final boolean postAtFrontOfQueue(Runnable r) {
        return sendMessageAtFrontOfQueue(getPostMessage(r));
    }


    public final boolean runWithScissors(final Runnable r, long timeout) {
        if (r == null) {
            throw new IllegalArgumentException("runnable must not be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be non-negative");
        }

        if (JLooper.myLooper() == mLooper) {
            r.run();
            return true;
        }

        BlockingRunnable br = new BlockingRunnable(r);
        return br.postAndWait(this, timeout);
    }


    public final void removeCallbacks(Runnable r) {
        mQueue.removeMessages(this, r, null);
    }


    public final void removeCallbacks(Runnable r, Object token) {
        mQueue.removeMessages(this, r, token);
    }


    public final boolean sendMessage(JMessage msg) {
        return sendMessageDelayed(msg, 0);
    }


    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }


    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        JMessage msg = JMessage.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }


    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        JMessage msg = JMessage.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(JMessage msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, +delayMillis);
    }


    public boolean sendMessageAtTime(JMessage msg, long uptimeMillis) {
        JMessageQueue queue = mQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            // TODO: 2017/9/16 add warn log
            return false;
        }
        return enqueueMessage(queue, msg, uptimeMillis);
    }


    public final boolean sendMessageAtFrontOfQueue(JMessage msg) {
        JMessageQueue queue = mQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            // TODO: 2017/9/16 add warn log
            return false;
        }
        return enqueueMessage(queue, msg, 0);
    }

    private boolean enqueueMessage(JMessageQueue queue, JMessage msg, long uptimeMillis) {
        msg.target = this;
        if (mAsynchronous) {
            msg.setAsynchronous(true);
        }
        return queue.enqueueMessage(msg, uptimeMillis);
    }

    public final void removeMessages(int what) {
        mQueue.removeMessages(this, what, null);
    }


    public final void removeMessages(int what, Object object) {
        mQueue.removeMessages(this, what, object);
    }


    public final void removeCallbacksAndMessages(Object token) {
        mQueue.removeCallbacksAndMessages(this, token);
    }


    public final boolean hasMessages(int what) {
        return mQueue.hasMessages(this, what, null);
    }


    public final boolean hasMessages(int what, Object object) {
        return mQueue.hasMessages(this, what, object);
    }

    public final boolean hasCallbacks(Runnable r) {
        return mQueue.hasMessages(this, r, null);
    }

    // if we can get rid of this method, the handler need not remember its loop
    // we could instead export a getMessageQueue() method... 
    public final JLooper getLooper() {
        return mLooper;
    }

    @Override
    public String toString() {
        return "JHandler (" + getClass().getName() + ") {"
                + Integer.toHexString(System.identityHashCode(this))
                + "}";
    }

/*    final IMessenger getIMessenger() {
        synchronized (mQueue) {
            if (mMessenger != null) {
                return mMessenger;
            }
            mMessenger = new MessengerImpl();
            return mMessenger;
        }
    }

    private final class MessengerImpl extends IMessenger.Stub {
        public void send(JMessage msg) {
            msg.sendingUid = Binder.getCallingUid();
            JHandler.this.sendMessage(msg);
        }
    }*/

    private static JMessage getPostMessage(Runnable r) {
        JMessage m = JMessage.obtain();
        m.callback = r;
        return m;
    }

    private static JMessage getPostMessage(Runnable r, Object token) {
        JMessage m = JMessage.obtain();
        m.obj = token;
        m.callback = r;
        return m;
    }

    private static void handleCallback(JMessage message) {
        message.callback.run();
    }

    final JLooper mLooper;
    final JMessageQueue mQueue;
    final Callback mCallback;
    final boolean mAsynchronous;
    // TODO: 2017/9/16 provide IPC implements for this Handler
//    IMessenger mMessenger;

    private static final class BlockingRunnable implements Runnable {
        private final Runnable mTask;
        private boolean mDone;

        public BlockingRunnable(Runnable task) {
            mTask = task;
        }

        public void run() {
            try {
                mTask.run();
            } finally {
                synchronized (this) {
                    mDone = true;
                    notifyAll();
                }
            }
        }

        public boolean postAndWait(JHandler handler, long timeout) {
            if (!handler.post(this)) {
                return false;
            }

            synchronized (this) {
                if (timeout > 0) {
                    final long expirationTime = System.currentTimeMillis() + timeout;
                    while (!mDone) {
                        long delay = expirationTime - System.currentTimeMillis();
                        if (delay <= 0) {
                            return false; // timeout
                        }
                        try {
                            wait(delay);
                        } catch (InterruptedException ex) {
                        }
                    }
                } else {
                    while (!mDone) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            return true;
        }
    }
}
