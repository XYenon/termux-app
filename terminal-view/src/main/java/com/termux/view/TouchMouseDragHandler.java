package com.termux.view;

import android.view.MotionEvent;

import com.termux.terminal.GhosttyTerminal;

final class TouchMouseDragHandler {

    interface MouseEventSender {
        void send(MotionEvent event, int action);
    }

    private final MouseEventSender mSender;
    private enum State { IDLE, AIMING, READY, PENDING_DRAG, DRAGGING }
    private State mState = State.IDLE;
    private MotionEvent mPosition;
    private float mLastX, mLastY;
    private float mDownX, mDownY;
    private int mWidth, mHeight, mTouchSlop;

    TouchMouseDragHandler(MouseEventSender sender) {
        mSender = sender;
    }

    void start(MotionEvent event, int width, int height, int touchSlop) {
        if (isActive()) return;
        mWidth = width;
        mHeight = height;
        mTouchSlop = touchSlop;
        mPosition = MotionEvent.obtain(event);
        mLastX = event.getX();
        mLastY = event.getY();
        mState = State.AIMING;
    }

    boolean onTouchEvent(MotionEvent event) {
        if (!isActive()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mLastX = event.getX();
                mDownY = mLastY = event.getY();
                mState = State.PENDING_DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mState == State.PENDING_DRAG) {
                    if (Math.hypot(event.getX() - mDownX, event.getY() - mDownY) <= mTouchSlop)
                        break;
                    mSender.send(mPosition, GhosttyTerminal.MOUSE_ACTION_PRESS);
                    mState = State.DRAGGING;
                }
                if (mState == State.AIMING || mState == State.DRAGGING) {
                    move(event);
                    if (mState == State.DRAGGING)
                        mSender.send(mPosition, GhosttyTerminal.MOUSE_ACTION_MOTION);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mState == State.DRAGGING) {
                    move(event);
                    cancel();
                } else {
                    if (mState == State.AIMING) move(event);
                    mState = State.READY;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }
        return true;
    }

    private void move(MotionEvent event) {
        // Fine positioning while aiming; normal relative speed once dragging starts.
        float scale = mState == State.AIMING ? 2f / 3f : 1f;
        float x = mPosition.getX() + (event.getX() - mLastX) * scale;
        float y = mPosition.getY() + (event.getY() - mLastY) * scale;
        mPosition.setLocation(Math.max(0, Math.min(mWidth - 1, x)),
            Math.max(0, Math.min(mHeight - 1, y)));
        mLastX = event.getX();
        mLastY = event.getY();
    }

    void click() {
        if (!isReady()) return;
        mSender.send(mPosition, GhosttyTerminal.MOUSE_ACTION_PRESS);
        mSender.send(mPosition, GhosttyTerminal.MOUSE_ACTION_RELEASE);
        cancel();
    }

    void cancel() {
        if (mState == State.DRAGGING)
            mSender.send(mPosition, GhosttyTerminal.MOUSE_ACTION_RELEASE);
        mState = State.IDLE;
        if (mPosition != null) {
            mPosition.recycle();
            mPosition = null;
        }
    }

    float getX() { return mPosition.getX(); }
    float getY() { return mPosition.getY(); }

    boolean isReady() {
        return mState == State.READY;
    }

    boolean isActive() {
        return mState != State.IDLE;
    }
}
