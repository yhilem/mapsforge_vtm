/*
 * Copyright 2021 Frank Knoll
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
import org.oscim.event.MotionEvent;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerLayer;
import org.oscim.map.Map;

class ItemDragger {

    private final DraggableItemizedLayer draggableItemizedLayer;
    private final DragAndDropListener dragItemAndRedrawListener;
    private DraggableMarkerItem dragItem;

    public ItemDragger(DraggableItemizedLayer draggableItemizedLayer, Map map) {
        this.draggableItemizedLayer = draggableItemizedLayer;
        this.dragItemAndRedrawListener = createDragItemAndRedrawListener(draggableItemizedLayer, map);
    }

    public boolean startDragItem(MotionEvent event, final GeoPoint geoPoint) {
        dragItem = null;
        return draggableItemizedLayer.activateSelectedItems(
                event,
                new ItemizedLayer.ActiveItem() {
                    @Override
                    public boolean run(int index) {
                        dragItem = (DraggableMarkerItem) draggableItemizedLayer.getMarkerItems().get(index);
                        dragItemAndRedrawListener.startDragItemAtGeoPoint(dragItem, geoPoint);
                        return true;
                    }
                });
    }

    public boolean ongoingDragItemTo(GeoPoint geoPoint) {
        if (dragItem == null) {
            return false;
        }
        dragItemAndRedrawListener.ongoingDragItemToGeoPoint(dragItem, geoPoint);
        return true;
    }

    public boolean dropItemAt(GeoPoint geoPoint) {
        if (dragItem == null) {
            return false;
        }
        dragItemAndRedrawListener.dropItemAtGeoPoint(dragItem, geoPoint);
        return true;
    }

    public void noDrag() {
        dragItem = null;
    }

    private DragAndDropListener createDragItemAndRedrawListener(final MarkerLayer markerLayer, final Map map) {
        return new DragAndDropListener() {
            @Override
            public void startDragItemAtGeoPoint(DraggableMarkerItem item, GeoPoint geoPoint) {
                item.getDragAndDropListener().startDragItemAtGeoPoint(item, geoPoint);
                updateLocationOfMarkerItemAndRedraw(item, geoPoint);
            }

            @Override
            public void ongoingDragItemToGeoPoint(DraggableMarkerItem item, GeoPoint geoPoint) {
                item.getDragAndDropListener().ongoingDragItemToGeoPoint(item, geoPoint);
                updateLocationOfMarkerItemAndRedraw(item, geoPoint);
            }

            @Override
            public void dropItemAtGeoPoint(DraggableMarkerItem item, GeoPoint geoPoint) {
                item.getDragAndDropListener().dropItemAtGeoPoint(item, geoPoint);
                updateLocationOfMarkerItemAndRedraw(item, geoPoint);
            }

            private void updateLocationOfMarkerItemAndRedraw(MarkerItem markerItem, GeoPoint location) {
                markerItem.geoPoint = location;
                markerLayer.populate();
                map.render();
            }
        };
    }
}
