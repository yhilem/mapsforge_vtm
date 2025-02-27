/*
 * Copyright 2013 Hannes Janetzek
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
package org.oscim.tiling.source.mapnik;

import org.oscim.core.GeometryBuffer.GeometryType;
import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.tiling.ITileDataSink;
import org.oscim.tiling.source.PbfDecoder;
import org.oscim.utils.pool.Inlist;
import org.oscim.utils.pool.Pool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

public class TileDecoder extends PbfDecoder {
    private static final Logger log = Logger.getLogger(TileDecoder.class.getName());

    private static final int TAG_TILE_LAYERS = 3;

    private static final int TAG_LAYER_VERSION = 15;
    private static final int TAG_LAYER_NAME = 1;
    private static final int TAG_LAYER_FEATURES = 2;
    private static final int TAG_LAYER_KEYS = 3;
    private static final int TAG_LAYER_VALUES = 4;
    private static final int TAG_LAYER_EXTENT = 5;

    private static final int TAG_FEATURE_ID = 1;
    private static final int TAG_FEATURE_TAGS = 2;
    private static final int TAG_FEATURE_TYPE = 3;
    private static final int TAG_FEATURE_GEOMETRY = 4;

    private static final int TAG_VALUE_STRING = 1;
    private static final int TAG_VALUE_FLOAT = 2;
    private static final int TAG_VALUE_DOUBLE = 3;
    private static final int TAG_VALUE_LONG = 4;
    private static final int TAG_VALUE_UINT = 5;
    private static final int TAG_VALUE_SINT = 6;
    private static final int TAG_VALUE_BOOL = 7;

    private static final int TAG_GEOM_UNKNOWN = 0;
    private static final int TAG_GEOM_POINT = 1;
    private static final int TAG_GEOM_LINE = 2;
    private static final int TAG_GEOM_POLYGON = 3;

    private short[] mTmpTags = new short[1024];

    private Tile mTile;
    private final String mLocale = "de";
    private ITileDataSink mMapDataCallback;

    private static final float REF_TILE_SIZE = 4096.0f;
    private float mScale;

    @Override
    public boolean decode(Tile tile, ITileDataSink mapDataCallback, InputStream is)
            throws IOException {

        if (debug)
            log.fine(tile + " decode");

        setInputStream(new InflaterInputStream(is));
        mTile = tile;
        mMapDataCallback = mapDataCallback;
        mScale = REF_TILE_SIZE / Tile.SIZE;

        int val;

        while (hasData() && (val = decodeVarint32()) > 0) {
            // read tag and wire type
            int tag = (val >> 3);

            switch (tag) {
                case TAG_TILE_LAYERS:
                    decodeLayer();
                    break;

                default:
                    error(mTile + " invalid type for tile: " + tag);
                    return false;
            }
        }

        if (hasData()) {
            error(tile + " invalid tile");
            return false;
        }
        return true;
    }

    private boolean decodeLayer() throws IOException {

        //int version = 0;
        //int extent = 4096;

        int bytes = decodeVarint32();

        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();

        String name = null;
        int numFeatures = 0;
        ArrayList<Feature> features = new ArrayList<Feature>();

        int end = position() + bytes;
        while (position() < end) {
            // read tag and wire type
            int val = decodeVarint32();
            if (val == 0)
                break;

            int tag = (val >> 3);

            switch (tag) {
                case TAG_LAYER_KEYS:
                    keys.add(decodeString());
                    break;

                case TAG_LAYER_VALUES:
                    values.add(decodeValue());
                    break;

                case TAG_LAYER_FEATURES:
                    numFeatures++;
                    decodeFeature(features);
                    break;

                case TAG_LAYER_VERSION:
                    //version =
                    decodeVarint32();
                    break;

                case TAG_LAYER_NAME:
                    name = decodeString();
                    break;

                case TAG_LAYER_EXTENT:
                    //extent =
                    decodeVarint32();
                    break;

                default:
                    error(mTile + " invalid type for layer: " + tag);
                    break;
            }

        }

        Tag layerTag = new Tag(name, Tag.VALUE_YES);

        if (numFeatures == 0)
            return true;

        int[] ignoreLocal = new int[20];
        int numIgnore = 0;

        int fallBackLocal = -1;
        int matchedLocal = -1;

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (!key.startsWith(Tag.KEY_NAME))
                continue;
            int len = key.length();
            if (len == 4) {
                fallBackLocal = i;
                continue;
            }
            if (len < 7) {
                ignoreLocal[numIgnore++] = i;
                continue;
            }

            if (mLocale.equals(key.substring(5))) {
                //log.debug("found local " + key);
                matchedLocal = i;
            } else
                ignoreLocal[numIgnore++] = i;

        }

        for (Feature f : features) {

            if (f.elem.type == GeometryType.NONE)
                continue;

            f.elem.tags.clear();
            f.elem.tags.add(layerTag);

            boolean hasName = false;
            String fallbackName = null;

            tagLoop:
            for (int j = 0; j < (f.numTags << 1); j += 2) {
                int keyIdx = f.tags[j];
                for (int i = 0; i < numIgnore; i++)
                    if (keyIdx == ignoreLocal[i])
                        continue tagLoop;

                if (keyIdx == fallBackLocal) {
                    fallbackName = values.get(f.tags[j + 1]);
                    continue;
                }

                String key;
                String val = values.get(f.tags[j + 1]);

                if (keyIdx == matchedLocal) {
                    hasName = true;
                    f.elem.tags.add(new Tag(Tag.KEY_NAME, val, false));

                } else {
                    key = keys.get(keyIdx);
                    f.elem.tags.add(new Tag(key, val));
                }
            }

            if (!hasName && fallbackName != null)
                f.elem.tags.add(new Tag(Tag.KEY_NAME, fallbackName, false));

            // FIXME extract layer tag here
            f.elem.setLayer(5);
            mMapDataCallback.process(f.elem);
            f = mFeaturePool.release(f);
        }

        return true;
    }

    private final Pool<Feature> mFeaturePool = new Pool<Feature>() {
        int count;

        @Override
        protected Feature createItem() {
            count++;
            return new Feature();
        }

        @Override
        protected boolean clearItem(Feature item) {
            if (count > 100) {
                count--;
                return false;
            }

            item.elem.tags.clear();
            item.elem.clear();
            item.tags = null;
            item.type = 0;
            item.numTags = 0;

            return true;
        }
    };

    static class Feature extends Inlist<Feature> {
        short[] tags;
        int numTags;
        int type;

        final MapElement elem;

        Feature() {
            elem = new MapElement();
        }

        boolean match(short otherTags[], int otherNumTags, int otherType) {
            if (numTags != otherNumTags)
                return false;

            if (type != otherType)
                return false;

            for (int i = 0; i < numTags << 1; i++) {
                if (tags[i] != otherTags[i])
                    return false;
            }
            return true;
        }

    }

    private void decodeFeature(ArrayList<Feature> features) throws IOException {
        int bytes = decodeVarint32();
        int end = position() + bytes;

        int type = 0;
        //long id;

        lastX = 0;
        lastY = 0;

        mTmpTags[0] = -1;

        Feature curFeature = null;
        int numTags = 0;

        //log.debug("start feature");
        while (position() < end) {
            // read tag and wire type
            int val = decodeVarint32();
            if (val == 0)
                break;

            int tag = (val >>> 3);

            switch (tag) {
                case TAG_FEATURE_ID:
                    //id =
                    decodeVarint32();
                    break;

                case TAG_FEATURE_TAGS:
                    mTmpTags = decodeUnsignedVarintArray(mTmpTags);

                    for (; numTags < mTmpTags.length && mTmpTags[numTags] >= 0; )
                        numTags += 2;

                    numTags >>= 1;

                    break;

                case TAG_FEATURE_TYPE:
                    type = decodeVarint32();
                    //log.debug("got type " + type);
                    break;

                case TAG_FEATURE_GEOMETRY:

                    for (Feature f : features) {
                        if (f.match(mTmpTags, numTags, type)) {
                            curFeature = f;
                            break;
                        }
                    }

                    if (curFeature == null) {
                        curFeature = mFeaturePool.get();
                        curFeature.tags = new short[numTags << 1];
                        System.arraycopy(mTmpTags, 0, curFeature.tags, 0, numTags << 1);
                        curFeature.numTags = numTags;
                        curFeature.type = type;

                        features.add(curFeature);
                    }

                    decodeCoordinates(type, curFeature);
                    break;

                default:
                    error(mTile + " invalid type for feature: " + tag);
                    break;
            }
        }
    }

    private static final int CLOSE_PATH = 0x07;
    private static final int MOVE_TO = 0x01;
    //private static final int LINE_TO = 0x02;

    private int lastX, lastY;

    private int decodeCoordinates(int type, Feature feature) throws IOException {
        int bytes = decodeVarint32();
        fillBuffer(bytes);

        if (feature == null) {
            bufferPos += bytes;
            return 0;
        }

        MapElement elem = feature.elem;
        boolean isPoint = false;
        boolean isPoly = false;
        boolean isLine = false;

        if (type == TAG_GEOM_LINE) {
            elem.startLine();
            isLine = true;
        } else if (type == TAG_GEOM_POLYGON) {
            elem.startPolygon();
            isPoly = true;
        } else if (type == TAG_GEOM_POINT) {
            isPoint = true;
            elem.startPoints();
        } else if (type == TAG_GEOM_UNKNOWN)
            elem.startPoints();

        boolean even = true;
        int val;

        int curX = 0;
        int curY = 0;
        int prevX = 0;
        int prevY = 0;

        int cmd = 0;
        int num = 0, cnt = 0;

        boolean first = true;
        boolean lastClip = false;

        // test bbox for outer..
        boolean isOuter = true;
        boolean simplify = mTile.zoomLevel < 14;
        int pixel = simplify ? 7 : 3;

        int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;

        for (int end = bufferPos + bytes; bufferPos < end; ) {
            val = decodeVarint32Filled();

            if (num == 0) {
                // number of points
                num = val >>> 3;
                cnt = 0;
                // path command
                cmd = val & 0x07;

                if (isLine && lastClip) {
                    elem.addPoint(curX / mScale, curY / mScale);
                    lastClip = false;
                }

                if (cmd == CLOSE_PATH) {
                    num = 0;
                    continue;
                }
                if (first) {
                    first = false;
                    continue;
                }
                if (cmd == MOVE_TO) {
                    if (type == TAG_GEOM_LINE)
                        elem.startLine();
                    else if (type == TAG_GEOM_POLYGON) {
                        isOuter = false;
                        elem.startHole();
                    }
                }
                continue;
            }
            // zigzag decoding
            int s = ((val >>> 1) ^ -(val & 1));

            if (even) {
                // get x coordinate
                even = false;
                curX = lastX = lastX + s;
                continue;
            }
            // get y coordinate and add point to geometry
            num--;

            even = true;
            curY = lastY = lastY + s;

            int dx = (curX - prevX);
            int dy = (curY - prevY);

            if (isPoly && num == 0 && cnt > 0) {
                prevX = curX;
                prevY = curY;

                // only add last point if it is di
                int ppos = cnt * 2;
                if (elem.points[elem.pointNextPos - ppos] != curX
                        || elem.points[elem.pointNextPos - ppos + 1] != curY)
                    elem.addPoint(curX / mScale, curY / mScale);

                lastClip = false;
                continue;
            }

            if ((isPoint || cmd == MOVE_TO)
                    || (dx > pixel || dx < -pixel)
                    || (dy > pixel || dy < -pixel)
                    // hack to not clip at tile boundaries
                    || (curX <= 0 || curX >= 4095)
                    || (curY <= 0 || curY >= 4095)) {

                prevX = curX;
                prevY = curY;
                elem.addPoint(curX / mScale, curY / mScale);
                cnt++;

                if (simplify && isOuter) {
                    if (curX < xmin)
                        xmin = curX;
                    if (curX > xmax)
                        xmax = curX;

                    if (curY < ymin)
                        ymin = curY;
                    if (curY > ymax)
                        ymax = curY;
                }

                lastClip = false;
                continue;
            }
            lastClip = true;
        }

        if (isPoly && isOuter && simplify && !testBBox(xmax - xmin, ymax - ymin)) {
            //log.debug("skip small poly "+ elem.indexPos + " > "
            // +  (xmax - xmin) * (ymax - ymin));
            elem.pointNextPos -= elem.index[elem.indexCurrentPos];
            if (elem.indexCurrentPos > 0) {
                elem.indexCurrentPos -= 2;
                elem.index[elem.indexCurrentPos + 1] = -1;
            } else {
                elem.type = GeometryType.NONE;
            }
            return 0;
        }

        if (isLine && lastClip)
            elem.addPoint(curX / mScale, curY / mScale);

        return 1;
    }

    private static boolean testBBox(int dx, int dy) {
        return dx * dy > 64 * 64;
    }

    private String decodeValue() throws IOException {
        int bytes = decodeVarint32();

        String value = null;

        int end = position() + bytes;

        while (position() < end) {
            // read tag and wire type
            int val = decodeVarint32();
            if (val == 0)
                break;

            int tag = (val >> 3);

            switch (tag) {
                case TAG_VALUE_STRING:
                    value = decodeString();
                    break;

                case TAG_VALUE_UINT:
                    value = String.valueOf(decodeVarint32());
                    break;

                case TAG_VALUE_SINT:
                    value = String.valueOf(deZigZag(decodeVarint32()));
                    break;

                case TAG_VALUE_LONG:
                    value = String.valueOf(decodeVarint64());
                    break;

                case TAG_VALUE_FLOAT:
                    value = String.valueOf(decodeFloat());
                    break;

                case TAG_VALUE_DOUBLE:
                    value = String.valueOf(decodeDouble());
                    break;

                case TAG_VALUE_BOOL:
                    value = decodeBool() ? "yes" : "no";
                    break;
                default:
                    break;
            }

        }
        return value;
    }
}
