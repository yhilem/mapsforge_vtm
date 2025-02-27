/*
 * Copyright 2014 Hannes Janetzek
 * Copyright 2016-2020 devemux86
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
package org.oscim.test;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.gdx.GdxMapApp;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.tiling.source.bitmap.DefaultSources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineTexTest extends GdxMapApp {

    private static final boolean ANIMATION = true;

    private List<PathLayer> mPathLayers = new ArrayList<>();
    private TextureItem tex;

    @Override
    public void createLayers() {
        UrlTileSource tileSource = DefaultSources.OPENSTREETMAP
                .httpFactory(new OkHttpEngine.OkHttpFactory())
                .build();
        tileSource.setHttpRequestHeaders(Collections.singletonMap("User-Agent", "vtm-playground"));
        mMap.layers().add(new BitmapTileLayer(mMap, tileSource));

        mMap.setMapPosition(0, 0, 1 << 2);

        try {
            tex = new TextureItem(CanvasAdapter.getBitmapAsset("", "patterns/pike.png", null));
            //tex.mipmap = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLayers(1, true);

        if (ANIMATION)
            mMap.events.bind(new Map.UpdateListener() {
                @Override
                public void onMapEvent(Event e, MapPosition mapPosition) {
                    //if (e == Map.UPDATE_EVENT) {
                    long t = System.currentTimeMillis();
                    float pos = t % 20000 / 10000f - 1f;
                    createLayers(pos, false);
                    mMap.updateMap(true);
                    //}
                }
            });
    }

    void createLayers(float pos, boolean init) {

        int i = 0;

        for (double lat = -90; lat <= 90; lat += 5) {
            List<GeoPoint> pts = new ArrayList<>();

            for (double lon = -180; lon <= 180; lon += 2) {
                //pts.add(new GeoPoint(lat, lon));
                //                double longitude = lon + (pos * 180);
                //                if (longitude < -180)
                //                    longitude += 360;
                //                if (longitude > 180)
                //                    longitude -= 360;
                double longitude = lon;

                double latitude = lat + (pos * 90);
                if (latitude < -90)
                    latitude += 180;
                if (latitude > 90)
                    latitude -= 180;

                latitude += Math.sin((Math.abs(pos) * (lon / Math.PI)));

                pts.add(new GeoPoint(latitude, longitude));
            }
            PathLayer pathLayer;
            if (init) {
                int c = Color.fade(Color.rainbow((float) (lat + 90) / 180), 0.5f);
                Style style = Style.builder()
                        .stippleColor(c)
                        .stipple(24)
                        .stippleWidth(1)
                        .strokeWidth(12)
                        .strokeColor(c)
                        .fixed(true)
                        .texture(tex)
                        .randomOffset(false)
                        .build();
                pathLayer = new PathLayer(mMap, style);
                mMap.layers().add(pathLayer);
                mPathLayers.add(pathLayer);
            } else {
                pathLayer = mPathLayers.get(i++);
            }

            pathLayer.setPoints(pts);
        }

    }

    public static void main(String[] args) {
        GdxMapApp.init();
        GdxMapApp.run(new LineTexTest());
    }
}
