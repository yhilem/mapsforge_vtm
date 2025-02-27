/*
 * Copyright 2018-2019 Gustl22
 * Copyright 2018-2019 devemux86
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
package org.oscim.layers.tile.buildings;

import org.oscim.backend.canvas.Color;
import org.oscim.core.*;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.theme.styles.ExtrusionStyle;
import org.oscim.utils.ExtrusionUtils;
import org.oscim.utils.geom.GeometryUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.oscim.renderer.MapRenderer.COORD_SCALE;

/**
 * A layer to display S3DB elements.
 */
public class S3DBLayer extends BuildingLayer {

    private static final Logger log = Logger.getLogger(S3DBLayer.class.getName());

    private final float TILE_SCALE = (ExtrusionUtils.REF_TILE_SIZE / (Tile.SIZE * COORD_SCALE));

    private boolean mColored = true;
    private boolean mTransparent = true;

    public S3DBLayer(Map map, VectorTileLayer tileLayer) {
        this(map, tileLayer, false);
    }

    public S3DBLayer(Map map, VectorTileLayer tileLayer, boolean shadow) {
        this(map, tileLayer, MIN_ZOOM, map.viewport().getMaxZoomLevel(), shadow);
    }

    /**
     * @param map       The map data to add
     * @param tileLayer The vector tile layer which contains the tiles and map elements
     * @param zoomMin   The minimum zoom at which the layer appears
     * @param zoomMax   The maximum zoom at which the layer appears
     * @param shadow    Declare if using shadow renderer
     */
    public S3DBLayer(Map map, VectorTileLayer tileLayer, int zoomMin, int zoomMax, boolean shadow) {
        super(map, tileLayer, zoomMin, zoomMax, true, shadow);
    }

    public boolean isColored() {
        return mColored;
    }

    /**
     * @param colored true: use colour written in '*:colour' tag,
     *                false: use colours of extrusion style
     */
    public void setColored(boolean colored) {
        mColored = colored;
    }

    public boolean isTransparent() {
        return mTransparent;
    }

    /**
     * @param transparent if true coloured buildings blend transparency of extrusion style
     *                    (only in combination with isColored = true)
     */
    public void setTransparent(boolean transparent) {
        mTransparent = transparent;
    }

    @Override
    public void complete(MapTile tile, boolean success) {
        super.complete(tile, success);
        // Do stuff here
    }

    @Override
    public void processElement(MapElement element, ExtrusionStyle extrusion, MapTile tile) {
        float groundScale = tile.getGroundScale();

        int maxHeight = 0; // cm
        int minHeight = 0; // cm
        int roofHeight = 0;

        // Get roof height
        String v = getValue(element, Tag.KEY_ROOF_HEIGHT);
        if (v != null) {
            roofHeight = (int) (Float.parseFloat(v) * 100);
        } else if ((v = getValue(element, Tag.KEY_ROOF_LEVELS)) != null) {
            roofHeight = (int) (Float.parseFloat(v) * BUILDING_LEVEL_HEIGHT);
        } else if ((v = getValue(element, Tag.KEY_ROOF_ANGLE)) != null) {
            Box bb = null;
            for (int k = 0; k < element.index[0]; k += 2) {
                float p1 = element.points[k];
                float p2 = element.points[k + 1];
                if (bb == null)
                    bb = new Box(p1, p2);
                else {
                    bb.add(p1, p2);
                }
            }
            if (bb != null) {
                float minSize = (int) Math.min(bb.getHeight(), bb.getWidth()) * groundScale; // depends on lat
                // Angle is simplified, 40 is an estimated constant
                roofHeight = (int) ((Float.parseFloat(v) / 45.f) * (minSize * 40));
            }
        } else if ((v = getValue(element, Tag.KEY_ROOF_SHAPE)) != null && !v.equals(Tag.VALUE_FLAT)) {
            roofHeight = (2 * BUILDING_LEVEL_HEIGHT);
        }

        // Get building height
        v = getValue(element, Tag.KEY_HEIGHT);
        if (v != null) {
            maxHeight = (int) (Float.parseFloat(v) * 100);
        } else {
            // #TagFromTheme: generalize level/height tags
            if ((v = getValue(element, Tag.KEY_BUILDING_LEVELS)) != null) {
                maxHeight = (int) (Float.parseFloat(v) * BUILDING_LEVEL_HEIGHT);
                maxHeight += roofHeight;
            }
        }
        if (maxHeight == 0)
            maxHeight = extrusion.defaultHeight * 100;

        v = getValue(element, Tag.KEY_MIN_HEIGHT);
        if (v != null)
            minHeight = (int) (Float.parseFloat(v) * 100);
        else {
            // #TagFromTheme: level/height tags
            if ((v = getValue(element, Tag.KEY_BUILDING_MIN_LEVEL)) != null)
                minHeight = (int) (Float.parseFloat(v) * BUILDING_LEVEL_HEIGHT);
        }

        // Get building color
        Integer bColor = null;
        if (mColored) {
            if ((v = getTransformedValue(element, Tag.KEY_BUILDING_COLOR)) != null) {
                bColor = S3DBUtils.getColor(v, extrusion.hsv, false);
            } else if ((v = getTransformedValue(element, Tag.KEY_BUILDING_MATERIAL)) != null) {
                bColor = S3DBUtils.getMaterialColor(v, extrusion.hsv, false);
            }
        }

        if (bColor == null) {
            bColor = extrusion.colorSide;
        } else if (mTransparent) {
            // Multiply alpha channel of extrusion style
            bColor = ExtrusionStyle.blendAlpha(bColor, Color.aToFloat(extrusion.colorSide));
        }

        // Scale x, y and z axis
        ExtrusionUtils.mapPolyCoordScale(element);
        float minHeightS = ExtrusionUtils.mapGroundScale(minHeight, groundScale) * TILE_SCALE;
        float maxHeightS = ExtrusionUtils.mapGroundScale(maxHeight, groundScale) * TILE_SCALE;
        float minRoofHeightS = ExtrusionUtils.mapGroundScale(maxHeight - roofHeight, groundScale) * TILE_SCALE;

        // Process building and roof
        processRoof(element, tile, minRoofHeightS, maxHeightS, bColor, extrusion);
        if (S3DBUtils.calcOutlines(element, minHeightS, minRoofHeightS)) {
            get(tile).addMeshElement(element, groundScale, bColor);
        }
    }

    @Override
    protected void processElements(MapTile tile) {
        if (!mBuildings.containsKey(tile.hashCode()))
            return;

        List<BuildingElement> tileBuildings = mBuildings.get(tile.hashCode());
        Set<BuildingElement> rootBuildings = new HashSet<>();
        for (BuildingElement partBuilding : tileBuildings) {
            if (!partBuilding.element.isBuildingPart())
                continue;

            String refId = getValue(partBuilding.element, Tag.KEY_REF);
            if (!RAW_DATA && refId == null)
                continue;

            TagSet partTags = partBuilding.element.tags;

            // Search buildings which inherit parts
            for (BuildingElement rootBuilding : tileBuildings) {
                if (rootBuilding.element.isBuildingPart())
                    continue;
                if (RAW_DATA) {
                    float[] center = GeometryUtils.center(partBuilding.element.points, 0, partBuilding.element.pointNextPos, null);
                    if (!GeometryUtils.pointInPoly(center[0], center[1], rootBuilding.element.points, rootBuilding.element.index[0], 0))
                        continue;
                } else if (!refId.equals(rootBuilding.element.tags.getValue(Tag.KEY_ID)))
                    continue;

                if ((getValue(rootBuilding.element, Tag.KEY_ROOF_SHAPE) != null)
                        && (getValue(partBuilding.element, Tag.KEY_ROOF_SHAPE) == null)) {
                    partBuilding.element.tags.add(rootBuilding.element.tags.get(getKeyOrDefault(Tag.KEY_ROOF_SHAPE)));
                }

                if (mColored) {
                    TagSet rootTags = rootBuilding.element.tags;

                    for (int i = 0; i < rootTags.size(); i++) {
                        Tag rTag = rootTags.get(i);
                        if ((rTag.key.equals(getKeyOrDefault(Tag.KEY_BUILDING_COLOR))
                                && !partTags.containsKey(getKeyOrDefault(Tag.KEY_BUILDING_MATERIAL))
                                || rTag.key.equals(getKeyOrDefault(Tag.KEY_ROOF_COLOR))
                                && !partTags.containsKey(getKeyOrDefault(Tag.KEY_ROOF_MATERIAL)))
                                && !partTags.containsKey(rTag.key)) {
                            partTags.add(rTag);
                        }
                    }
                }
                rootBuildings.add(rootBuilding);
                break;
            }
        }

        tileBuildings.removeAll(rootBuildings); // root buildings aren't rendered

        for (BuildingElement buildingElement : tileBuildings) {
            processElement(buildingElement.element, buildingElement.style, tile);
        }
        mBuildings.remove(tile.hashCode());
    }

    /**
     * Process the roof parts of building.
     *
     * @param element       the MapElement which needs a roof
     * @param tile          the tile which contains map element
     * @param minHeight     the height of the underlying building
     * @param maxHeight     the height of the roof + minHeight (whole building)
     * @param buildingColor the color of main building
     * @param extrusion     the extrusion style
     */
    private void processRoof(MapElement element, MapTile tile, float minHeight, float maxHeight,
                             int buildingColor, ExtrusionStyle extrusion) {
        int roofColor = extrusion.colorTop;
        String v;

        if (mColored) {
            v = getTransformedValue(element, Tag.KEY_ROOF_COLOR);
            if (v != null)
                roofColor = S3DBUtils.getColor(v, extrusion.hsv, false);
            else if ((v = getTransformedValue(element, Tag.KEY_ROOF_MATERIAL)) != null)
                roofColor = S3DBUtils.getMaterialColor(v, extrusion.hsv, false);
        }

        boolean roofOrientationAcross = false;
        if ((v = getValue(element, Tag.KEY_ROOF_ORIENTATION)) != null) {
            if (v.equals(Tag.VALUE_ACROSS)) {
                roofOrientationAcross = true;
            }
        }

        // Calc roof shape
        v = getValue(element, Tag.KEY_ROOF_SHAPE);
        if (v == null) {
            v = Tag.VALUE_FLAT;
        }

        float groundScale = tile.getGroundScale();

        GeometryBuffer gElement = new GeometryBuffer(element);
        GeometryBuffer specialParts = null;

        if (mTransparent) {
            // Use transparency of default roof color
            roofColor = ExtrusionStyle.blendAlpha(roofColor, Color.aToFloat(extrusion.colorTop));
        }

        boolean success;
        switch (v) {
            case Tag.VALUE_DOME:
            case Tag.VALUE_ONION:
                success = S3DBUtils.calcCircleMesh(gElement, minHeight, maxHeight, v);
                break;
            case Tag.VALUE_ROUND:
            case Tag.VALUE_SALTBOX:
            case Tag.VALUE_GABLED:
            case Tag.VALUE_GAMBREL:
                specialParts = new GeometryBuffer(0, 0); // No data in GeometryBuffer needed
                success = S3DBUtils.calcRidgeMesh(gElement, minHeight, maxHeight, roofOrientationAcross, v, specialParts);
                break;
            case Tag.VALUE_MANSARD:
            case Tag.VALUE_HALF_HIPPED:
            case Tag.VALUE_HIPPED:
                success = S3DBUtils.calcRidgeMesh(gElement, minHeight, maxHeight, roofOrientationAcross, v, null);
                break;
            case Tag.VALUE_SKILLION:
                // ROOF_SLOPE_DIRECTION is not supported yet
                String roofDirection = element.tags.getValue(Tag.KEY_ROOF_DIRECTION);
                float roofDegree = 0;
                if (roofDirection != null) {
                    roofDegree = Float.parseFloat(roofDirection);
                }
                specialParts = new GeometryBuffer(element);
                success = S3DBUtils.calcSkillionMesh(gElement, minHeight, maxHeight, roofDegree, specialParts);
                break;
            case Tag.VALUE_PYRAMIDAL:
                success = S3DBUtils.calcPyramidalMesh(gElement, minHeight, maxHeight);
                break;
            case Tag.VALUE_FLAT:
            default:
                success = S3DBUtils.calcFlatMesh(gElement, minHeight);
                break;
        }

        if (success) {
            get(tile).addMeshElement(gElement, groundScale, roofColor);
            if (specialParts != null) {
                get(tile).addMeshElement(specialParts, groundScale, buildingColor);
            }
        } else {
            log.fine("Roof calculation failed: " + element);
        }
    }
}
