/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016-2018 Izumi Kawashima
 * Copyright 2017-2020 devemux86
 * Copyright 2019 Gustl22
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
package org.oscim.web.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtGraphics;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.oscim.backend.*;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.gdx.GdxAssets;
import org.oscim.gdx.GdxMap;
import org.oscim.gdx.client.GwtDateTime;
import org.oscim.gdx.client.GwtGdxGraphics;
import org.oscim.gdx.client.MapConfig;
import org.oscim.gdx.client.MapUrl;
import org.oscim.gdx.poi3d.Poi3DLayer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.buildings.S3DBTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.StreamRenderTheme;
import org.oscim.theme.internal.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.bitmap.DefaultSources;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

class GwtMap extends GdxMap {
    static final Logger log = LoggerFactory.getLogger(GwtMap.class);

    BuildingLayer mBuildingLayer;
    BuildingSolutionControl mBuildingSolutionControl;
    CameraRollControl mCameraRollControl;
    SearchBox mSearchBox;

    @Override
    public void create() {
        MapConfig c = MapConfig.get();

        // stroke text takes about 70% cpu time in firefox:
        // https://bug568526.bugzilla.mozilla.org/attachment.cgi?id=447932
        // <- circle/stroke test 800ms firefox, 80ms chromium..
        // TODO use texture atlas to avoid drawing text-textures
        if (GwtApplication.agentInfo().isLinux() &&
                GwtApplication.agentInfo().isFirefox())
            GwtGdxGraphics.NO_STROKE_TEXT = true;

        GwtGdxGraphics.init();
        GdxAssets.init("");
        DateTimeAdapter.init(new GwtDateTime());
        CanvasAdapter.textScale = 0.7f;
        CanvasAdapter.dpi = (int) (GwtGraphics.getDevicePixelRatioJSNI() * CanvasAdapter.DEFAULT_DPI);
        Tile.SIZE = Tile.calculateTileSize();

        log.debug("GLAdapter.init");
        MapRenderer.setBackgroundColor(0xffffff);
        //Gdx.app.setLogLevel(Application.LOG_DEBUG);

        super.create();

        MapPosition p = new MapPosition();
        p.setZoomLevel(c.getZoom());
        p.setPosition(c.getLatitude(), c.getLongitude());

        MapUrl mapUrl = new MapUrl(mMap);
        mapUrl.parseUrl(p);
        mapUrl.scheduleRepeating(5000);

        mMap.setMapPosition(p);

        String mapName = mapUrl.getParam("map");
        String themeName = mapUrl.getParam("theme");

        VectorTileLayer l = null;

        if (mapName != null) {
            BitmapTileSource ts;

            if ("toner".equals(mapName))
                ts = DefaultSources.STAMEN_TONER.build();
            else if ("osm".equals(mapName)) {
                ts = DefaultSources.OPENSTREETMAP.build();
                ts.setHttpRequestHeaders(Collections.singletonMap("User-Agent", "vtm-web-app"));
            } else if ("watercolor".equals(mapName))
                ts = DefaultSources.STAMEN_WATERCOLOR.build();
            else if ("ne-landcover".equals(mapName))
                ts = DefaultSources.NE_LANDCOVER.build();
            else if ("hikebike".equals(mapName))
                ts = DefaultSources.HIKEBIKE.build();
            else if ("hikebike-hillshade".equals(mapName))
                ts = DefaultSources.HIKEBIKE_HILLSHADE.build();
            else
                ts = DefaultSources.STAMEN_TONER.build();

            mMap.setBaseMap(new BitmapTileLayer(mMap, ts));
        } else {
            TileSource ts = new OSciMap4TileSource();
            l = mMap.setBaseMap(ts);

            if (themeName == null) {
                // Local theme with texture atlas using png
                mMap.setTheme(new StreamRenderTheme("", AssetAdapter.readFileAsStream("vtm/default_atlas.xml")));
            } else {
                if ("osmarender".equals(themeName))
                    mMap.setTheme(VtmThemes.OSMARENDER);
                else if ("tron".equals(themeName))
                    mMap.setTheme(VtmThemes.TRONRENDER);
                else if ("newtron".equals(themeName))
                    mMap.setTheme(VtmThemes.NEWTRON);
                else
                    mMap.setTheme(VtmThemes.DEFAULT);
            }
        }

        boolean s3db = mapUrl.params.containsKey("s3db");
        if (s3db) {
            TileSource ts = OSciMap4TileSource.builder()
                    .url("http://opensciencemap.org/tiles/s3db")
                    .zoomMin(16)
                    .zoomMax(16)
                    .build();
            mMap.layers().add(new S3DBTileLayer(mMap, ts));
        }
        if (l != null) {
            boolean nolabels = mapUrl.params.containsKey("nolabels");
            boolean nobuildings = mapUrl.params.containsKey("nobuildings");

            if (!nobuildings && !s3db) {
                mBuildingLayer = new BuildingLayer(mMap, l);
                mBuildingLayer.getExtrusionRenderer().setZLimit((float) 65536 / 10);
                mMap.layers().add(mBuildingLayer);
            }

            mMap.layers().add(new Poi3DLayer(mMap, l));

            if (!nolabels)
                mMap.layers().add(new LabelLayer(mMap, l));
        }

        mSearchBox = new SearchBox(mMap);
    }

    @Override
    protected void initGLAdapter(GLVersion version) {
        if (version.getMajorVersion() >= 3)
            GLAdapter.init((GL30) Gdx.graphics.getGL30());
        else
            GLAdapter.init((GL) Gdx.graphics.getGL20());
    }

    @Override
    protected void createLayers() {
        mBuildingSolutionControl = new BuildingSolutionControl("#building-solution-input");
        mBuildingSolutionControl.addValueChangeListener(new BuildingSolutionControl.ValueChangeListener() {
            @Override
            public void onValueChange(int val, int max) {
                if (mBuildingLayer == null) {
                    return;
                }

                mBuildingLayer.getExtrusionRenderer().setZLimit((float) val / 10);

                mMap.updateMap(true);
            }
        });

        mCameraRollControl = new CameraRollControl("#camera-roll-input");
        mCameraRollControl.addValueChangeListener(new BuildingSolutionControl.ValueChangeListener() {
            @Override
            public void onValueChange(int val, int max) {
                mMap.viewport().setRoll((float) val / (float) max * 180.0f);
                mMap.updateMap(true);
            }
        });

        mBuildingSolutionControl.init();
        mCameraRollControl.init();
    }
}
