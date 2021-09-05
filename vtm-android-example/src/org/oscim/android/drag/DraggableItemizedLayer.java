/*
 * Copyright 2012 osmdroid authors: Nicolas Gramlich, Theodore Hong, Fred Eisele
 *
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016-2021 devemux86
 * Copyright 2016 Stephan Leuschner
 * Copyright 2016 Pedinel
 * Copyright 2019 Carlos Alberto Martínez Gadea
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

import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.List;

import static org.oscim.android.drag.DragGestureHandler.*;

public class DraggableItemizedLayer extends ItemizedLayer implements GestureListener {

    private final ItemDragger itemDragger;

    public DraggableItemizedLayer(Map map,
                                  List<MarkerInterface> markerItems,
                                  MarkerSymbol defaultMarker,
                                  OnItemGestureListener<MarkerInterface> listener) {
        super(map, markerItems, defaultMarker, listener);
        itemDragger = new ItemDragger(this, map);
    }

    @Override
    protected boolean activateSelectedItems(MotionEvent event, ActiveItem task) {
        return super.activateSelectedItems(event, task);
    }

    protected List<MarkerInterface> getMarkerItems() {
        return mItemList;
    }

    @Override
    public boolean onGesture(Gesture gesture, MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        if (gesture == START_DRAG) {
            return itemDragger.startDragItem(event, getGeoPoint(event));
        } else if (gesture == ONGOING_DRAG) {
            return itemDragger.ongoingDragItemTo(getGeoPoint(event));
        } else if (gesture == END_DRAG) {
            return itemDragger.dropItemAt(getGeoPoint(event));
        } else {
            itemDragger.noDrag();
        }

        return super.onGesture(gesture, event);
    }

    private GeoPoint getGeoPoint(MotionEvent event) {
        return map().viewport().fromScreenPoint(event.getX(), event.getY());
    }
}
