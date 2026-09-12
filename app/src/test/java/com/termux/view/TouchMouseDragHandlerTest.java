package com.termux.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.MotionEvent;

import com.termux.terminal.GhosttyTerminal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class TouchMouseDragHandlerTest {

    @Test
    public void inactiveHandlerDoesNotConsumeScrollingGesture() {
        List<Integer> actions = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> actions.add(action));

        assertFalse(handler.onTouchEvent(
            event(MotionEvent.ACTION_DOWN, 10, 20)));
        assertFalse(handler.onTouchEvent(
            event(MotionEvent.ACTION_MOVE, 10, 40)));
        assertFalse(handler.onTouchEvent(
            event(MotionEvent.ACTION_UP, 10, 40)));

        assertTrue(actions.isEmpty());
    }

    @Test
    public void aimThenDragUsesAdjustedOriginAndRelativeMovement() {
        List<MouseEvent> events = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> events.add(new MouseEvent(action,
                event.getX(), event.getY())));

        handler.start(event(MotionEvent.ACTION_DOWN, 10, 20), 300, 200, 8);
        assertTrue(handler.onTouchEvent(
            event(MotionEvent.ACTION_MOVE, 40, 80)));
        assertTrue(handler.onTouchEvent(
            event(MotionEvent.ACTION_UP, 40, 80)));
        assertTrue(events.isEmpty());
        assertTrue(handler.isReady());

        handler.onTouchEvent(event(MotionEvent.ACTION_DOWN, 180, 120));
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, 188, 120));
        assertTrue(events.isEmpty());
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, 210, 105));
        assertTrue(handler.onTouchEvent(
            event(MotionEvent.ACTION_UP, 216, 111)));

        assertFalse(handler.isActive());
        assertEquals(Arrays.asList(
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_PRESS, 30, 60),
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_MOTION, 60, 45),
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_RELEASE, 66, 51)
        ), events);
    }

    @Test
    public void cancelReleasesActiveDrag() {
        List<Integer> actions = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> actions.add(action));

        handler.start(event(MotionEvent.ACTION_DOWN, 10, 20), 300, 200, 8);
        handler.onTouchEvent(event(MotionEvent.ACTION_UP, 10, 20));
        handler.onTouchEvent(event(MotionEvent.ACTION_DOWN, 100, 120));
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, 130, 120));
        assertTrue(handler.onTouchEvent(
            event(MotionEvent.ACTION_CANCEL, 50, 60)));
        handler.cancel();
        assertFalse(handler.onTouchEvent(
            event(MotionEvent.ACTION_MOVE, 70, 80)));

        assertFalse(handler.isActive());
        assertEquals(Arrays.asList(
            GhosttyTerminal.MOUSE_ACTION_PRESS,
            GhosttyTerminal.MOUSE_ACTION_MOTION,
            GhosttyTerminal.MOUSE_ACTION_RELEASE
        ), actions);
    }

    @Test
    public void cancelledAimAndUnconfirmedTapSendNothing() {
        List<Integer> actions = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> actions.add(action));
        handler.start(event(MotionEvent.ACTION_DOWN, 20, 40), 300, 200, 8);
        handler.click(); // Not available until the origin is fixed.
        handler.onTouchEvent(event(MotionEvent.ACTION_UP, 20, 40));
        handler.onTouchEvent(event(MotionEvent.ACTION_DOWN, 100, 100));
        handler.onTouchEvent(event(MotionEvent.ACTION_UP, 100, 100));
        assertTrue(handler.isReady());
        handler.cancel(); // Used by More, Back, loss of focus, and session changes.
        assertFalse(handler.isActive());
        assertTrue(actions.isEmpty());
    }

    @Test
    public void explicitClickUsesClampedAimAndReleasesExactlyOnce() {
        List<MouseEvent> events = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> events.add(new MouseEvent(action, event.getX(), event.getY())));
        handler.start(event(MotionEvent.ACTION_DOWN, 10, 20), 100, 80, 8);
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, -50, 320));
        assertEquals(0, handler.getX(), 0);
        assertEquals(79, handler.getY(), 0);
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, -20, 290));
        handler.onTouchEvent(event(MotionEvent.ACTION_UP, -20, 290));
        handler.click();
        handler.click();
        handler.cancel();
        assertEquals(Arrays.asList(
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_PRESS, 20, 59),
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_RELEASE, 20, 59)
        ), events);
        assertFalse(handler.isActive());
    }

    @Test
    public void secondFingerCancelsWithoutStartingADrag() {
        List<Integer> actions = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> actions.add(action));
        handler.start(event(MotionEvent.ACTION_DOWN, 10, 20), 300, 200, 8);
        handler.onTouchEvent(event(MotionEvent.ACTION_POINTER_DOWN, 10, 20));
        assertFalse(handler.isActive());
        assertTrue(actions.isEmpty());
    }

    @Test
    public void initialTargetMatchesFingerForFineMovementAndClick() {
        List<MouseEvent> events = new ArrayList<>();
        TouchMouseDragHandler handler = new TouchMouseDragHandler(
            (event, action) -> events.add(new MouseEvent(action, event.getX(), event.getY())));
        MotionEvent down = event(MotionEvent.ACTION_DOWN, 80, 180);
        handler.start(down, 300, 400, 8);
        assertEquals(80, handler.getX(), 0);
        assertEquals(180, handler.getY(), 0);
        assertEquals(180, down.getY(), 0);
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, 110, 165));
        handler.onTouchEvent(event(MotionEvent.ACTION_UP, 110, 165));
        assertTrue(events.isEmpty());
        handler.click();
        assertEquals(Arrays.asList(
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_PRESS, 100, 170),
            new MouseEvent(GhosttyTerminal.MOUSE_ACTION_RELEASE, 100, 170)
        ), events);
        down.recycle();
    }

    @Test
    public void initialTargetMatchesFingerNearTopEdge() {
        TouchMouseDragHandler handler = new TouchMouseDragHandler((event, action) -> {});
        handler.start(event(MotionEvent.ACTION_DOWN, 80, 30), 300, 400, 8);
        assertEquals(30, handler.getY(), 0);
        handler.onTouchEvent(event(MotionEvent.ACTION_MOVE, 80, 60));
        assertEquals(50, handler.getY(), 0);
        handler.cancel();
    }

    private static MotionEvent event(int action, float x, float y) {
        return MotionEvent.obtain(0, 0, action, x, y, 0);
    }

    private static final class MouseEvent {
        final int action;
        final float x;
        final float y;

        MouseEvent(int action, float x, float y) {
            this.action = action;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MouseEvent)) return false;
            MouseEvent other = (MouseEvent) object;
            return action == other.action && x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            int result = action;
            result = 31 * result + Float.floatToIntBits(x);
            return 31 * result + Float.floatToIntBits(y);
        }
    }
}
