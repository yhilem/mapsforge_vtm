/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 devemux86
 * Copyright 2021 Frank Knoll
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.android.drag;

import android.view.MotionEvent;
import org.oscim.android.input.AndroidMotionEvent;
import org.oscim.android.input.GestureHandler;
import org.oscim.event.Gesture;
import org.oscim.map.Map;

class DragGestureHandler extends GestureHandler {

    public static final Gesture START_DRAG = new Gesture() {
    };
    public static final Gesture ONGOING_DRAG = new Gesture() {
    };
    public static final Gesture END_DRAG = new Gesture() {
    };

    private final AndroidMotionEvent mMotionEvent;
    private final Map mMap;
    private boolean scrolling = false;

    public DragGestureHandler(Map map) {
        super(map);
        mMotionEvent = new AndroidMotionEvent();
        mMap = map;
    }

    public boolean isScrolling() {
        return scrolling;
    }

    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        scrolling = true;
        mMap.handleGesture(START_DRAG, mMotionEvent.wrap(e));
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrolling = true;
        return mMap.handleGesture(ONGOING_DRAG, mMotionEvent.wrap(e2));
    }
}
