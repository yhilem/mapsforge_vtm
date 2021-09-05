/*
 * Copyright 2012 osmdroid authors:
 * Copyright 2012 Nicolas Gramlich
 * Copyright 2012 Theodore Hong
 * Copyright 2012 Fred Eisele
 *
 * Copyright 2014 Hannes Janetzek
 * Copyright 2016 devemux86
 * Copyright 2016 Erik Duisters
 * Copyright 2017 Longri
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
import org.oscim.layers.marker.MarkerItem;

public class DraggableMarkerItem extends MarkerItem {

    private final DragAndDropListener dragAndDropListener;

    public DraggableMarkerItem(String title, String description, GeoPoint geoPoint, DragAndDropListener dragAndDropListener) {
        super(title, description, geoPoint);
        this.dragAndDropListener = dragAndDropListener;
    }

    public DragAndDropListener getDragAndDropListener() {
        return dragAndDropListener;
    }
}
