package org.oscim.layers.vector.geometries;

import org.locationtech.jts.geom.Geometry;

public interface Drawable {

    /**
     * @return
     */
    public Style getStyle();

    /**
     * @return
     */
    public Geometry getGeometry();

    /**
     * Priority of drawable, the larger the value, the higher it will appear when drawn in the VectorLayer.
     *
     * @see org.oscim.layers.vector.VectorLayer processFeatures() method
     */
    public int getPriority();
}
