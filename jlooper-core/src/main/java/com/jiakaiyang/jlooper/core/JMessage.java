package com.jiakaiyang.jlooper.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jia on 2017/9/12.
 */
public class JMessage implements Serializable {


    public int what;


    public int arg1;


    public int arg2;


    public Object obj;


    // TODO: 2017/9/13
//    public Messenger replyTo;

    public int sendingUid = -1;


    /*package*/ static final int FLAG_IN_USE = 1 << 0;

    /**
     * If set message is asynchronous
     */
    /*package*/ static final int FLAG_ASYNCHRONOUS = 1 << 1;

    /**
     * Flags to clear in the copyFrom method
     */
    /*package*/ static final int FLAGS_TO_CLEAR_ON_COPY_FROM = FLAG_IN_USE;

    /*package*/ int flags;

    /*package*/ long when;

    /*package*/ Map<String, Object> data;

    /*package*/ JHandler target;

    /*package*/ Runnable callback;

    // sometimes we store linked lists of these things
    /*package*/ JMessage next;

    private static final Object sPoolSync = new Object();
    private static JMessage sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 50;

    private static boolean gCheckRecycle = true;

    /**
     * Return a new JMessage instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     */
    public static JMessage obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                JMessage m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0; // clear in-use flag
                sPoolSize--;
                return m;
            }
        }
        return new JMessage();
    }

    /**
     * Same as {@link #obtain()}, but copies the values of an existing
     * message (including its target) into the new one.
     *
     * @param orig Original message to copy.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JMessage orig) {
        JMessage m = obtain();
        m.what = orig.what;
        m.arg1 = orig.arg1;
        m.arg2 = orig.arg2;
        m.obj = orig.obj;
        m.sendingUid = orig.sendingUid;
        if (orig.data != null) {
            m.data = new HashMap<String, Object>(orig.data);
        }
        m.target = orig.target;
        m.callback = orig.callback;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the value for the <em>target</em> member on the JMessage returned.
     *
     * @param h JHandler to assign to the returned JMessage object's <em>target</em> member.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h) {
        JMessage m = obtain();
        m.target = h;

        return m;
    }

    /**
     * Same as {@link #obtain(JHandler)}, but assigns a callback Runnable on
     * the JMessage that is returned.
     *
     * @param h        JHandler to assign to the returned JMessage object's <em>target</em> member.
     * @param callback Runnable that will execute when the message is handled.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h, Runnable callback) {
        JMessage m = obtain();
        m.target = h;
        m.callback = callback;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values for both <em>target</em> and
     * <em>what</em> members on the JMessage.
     *
     * @param h    Value to assign to the <em>target</em> member.
     * @param what Value to assign to the <em>what</em> member.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h, int what) {
        JMessage m = obtain();
        m.target = h;
        m.what = what;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values of the <em>target</em>, <em>what</em>, and <em>obj</em>
     * members.
     *
     * @param h    The <em>target</em> value to set.
     * @param what The <em>what</em> value to set.
     * @param obj  The <em>object</em> method to set.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h, int what, Object obj) {
        JMessage m = obtain();
        m.target = h;
        m.what = what;
        m.obj = obj;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values of the <em>target</em>, <em>what</em>,
     * <em>arg1</em>, and <em>arg2</em> members.
     *
     * @param h    The <em>target</em> value to set.
     * @param what The <em>what</em> value to set.
     * @param arg1 The <em>arg1</em> value to set.
     * @param arg2 The <em>arg2</em> value to set.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h, int what, int arg1, int arg2) {
        JMessage m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values of the <em>target</em>, <em>what</em>,
     * <em>arg1</em>, <em>arg2</em>, and <em>obj</em> members.
     *
     * @param h    The <em>target</em> value to set.
     * @param what The <em>what</em> value to set.
     * @param arg1 The <em>arg1</em> value to set.
     * @param arg2 The <em>arg2</em> value to set.
     * @param obj  The <em>obj</em> value to set.
     * @return A JMessage object from the global pool.
     */
    public static JMessage obtain(JHandler h, int what,
                                  int arg1, int arg2, Object obj) {
        JMessage m = obtain();
        m.target = h;
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = obj;

        return m;
    }

    /**
     * Return a JMessage instance to the global pool.
     * <p>
     * You MUST NOT touch the JMessage after calling this function because it has
     * effectively been freed.  It is an error to recycle a message that is currently
     * enqueued or that is in the process of being delivered to a JHandler.
     * </p>
     */
    public void recycle() {
        if (isInUse()) {
            if (gCheckRecycle) {
                throw new IllegalStateException("This message cannot be recycled because it "
                        + "is still in use.");
            }
            return;
        }
        recycleUnchecked();
    }

    /**
     * Recycles a JMessage that may be in-use.
     * Used internally by the MessageQueue and Looper when disposing of queued Messages.
     */
    void recycleUnchecked() {
        // Mark the message as in use while it remains in the recycled object pool.
        // Clear out all other details.
        flags = FLAG_IN_USE;
        what = 0;
        arg1 = 0;
        arg2 = 0;
        obj = null;
        sendingUid = -1;
        when = 0;
        target = null;
        callback = null;
        data = null;

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    /**
     * Make this message like o.  Performs a shallow copy of the data field.
     * Does not copy the linked list fields, nor the timestamp or
     * target/callback of the original message.
     */
    public void copyFrom(JMessage o) {
        this.flags = o.flags & ~FLAGS_TO_CLEAR_ON_COPY_FROM;
        this.what = o.what;
        this.arg1 = o.arg1;
        this.arg2 = o.arg2;
        this.obj = o.obj;
        this.sendingUid = o.sendingUid;

        if (o.data != null) {
            this.data = new HashMap<String, Object>(o.data);
        } else {
            this.data = null;
        }
    }

    /**
     * Return the targeted delivery time of this message, in milliseconds.
     */
    public long getWhen() {
        return when;
    }

    public void setTarget(JHandler target) {
        this.target = target;
    }

    public JHandler getTarget() {
        return target;
    }


    public Runnable getCallback() {
        return callback;
    }


    public Map getData() {
        if (data == null) {
            data = new HashMap<String, Object>();
        }

        return data;
    }

    /**
     * Like getData(), but does not lazily create the Bundle.  A null
     * is returned if the Bundle does not already exist.  See
     * {@link #getData} for further information on this.
     */
    public Map peekData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }


    public void sendToTarget() {
        target.sendMessage(this);
    }


    public boolean isAsynchronous() {
        return (flags & FLAG_ASYNCHRONOUS) != 0;
    }

    public void setAsynchronous(boolean async) {
        if (async) {
            flags |= FLAG_ASYNCHRONOUS;
        } else {
            flags &= ~FLAG_ASYNCHRONOUS;
        }
    }

    /*package*/ boolean isInUse() {
        return ((flags & FLAG_IN_USE) == FLAG_IN_USE);
    }

    /*package*/ void markInUse() {
        flags |= FLAG_IN_USE;
    }

    /**
     * Constructor (but the preferred way to get a JMessage is to call {@link #obtain() JMessage.obtain()}).
     */
    public JMessage() {
    }

    @Override
    public String toString() {
//        return toString(SystemClock.uptimeMillis());

        // there is no such method liske SystemClock.uptimeMillis() in JVM platform,
        // so we use currentTimeMillis().
        return toString(System.currentTimeMillis());
    }

    String toString(long now) {
        StringBuilder b = new StringBuilder();
        b.append("{ when=");
        // TODO: 2017/9/16
        b.append(String.valueOf(when));

        if (target != null) {
            if (callback != null) {
                b.append(" callback=");
                b.append(callback.getClass().getName());
            } else {
                b.append(" what=");
                b.append(what);
            }

            if (arg1 != 0) {
                b.append(" arg1=");
                b.append(arg1);
            }

            if (arg2 != 0) {
                b.append(" arg2=");
                b.append(arg2);
            }

            if (obj != null) {
                b.append(" obj=");
                b.append(obj);
            }

            b.append(" target=");
            b.append(target.getClass().getName());
        } else {
            b.append(" barrier=");
            b.append(arg1);
        }

        b.append(" }");
        return b.toString();
    }
}
