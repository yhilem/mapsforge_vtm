/*
 * Copyright 2012 Hannes Janetzek
 * Copyright 2016-2020 devemux86
 * Copyright 2018-2019 Gustl22
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import org.oscim.android.MapView;
import org.oscim.map.Map;
import org.oscim.utils.Parameters;

public class DragMapView extends MapView {

    private DragGestureHandler gestureHandler;

    public DragMapView(Context context) {
        super(context);
    }

    public DragMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        if (!Parameters.MAP_EVENT_LAYER2) {
            gestureHandler = new DragGestureHandler(mMap);
            mGestureDetector = new GestureDetector(context, gestureHandler);
            mGestureDetector.setOnDoubleTapListener(gestureHandler);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent motionEvent) {
        if (!isClickable()) {
            return false;
        }

        if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
            if (gestureHandler.isScrolling()) {
                gestureHandler.setScrolling(false);
                return ((Map) mMap).handleGesture(DragGestureHandler.END_DRAG, mMotionEvent.wrap(motionEvent));
            }
        }

        return super.onTouchEvent(motionEvent);
    }
}
