/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016-2018 devemux86
 * Copyright 2018-2019 Gustl22
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
package org.oscim.gdx;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.oscim.awt.AwtGraphics;
import org.oscim.backend.DateTime;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.core.Tile;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.oscim.utils.FastMath;

public class GdxMapApp extends GdxMap {

    public static void init() {
        // load native library
        new SharedLibraryLoader().load("vtm-jni");
        // init globals
        AwtGraphics.init();
        GdxAssets.init("assets/");
        DateTimeAdapter.init(new DateTime());
    }

    public static void main(String[] args) {
        init();
        new LwjglApplication(new GdxMapApp(), getConfig(null));
    }

    public static void run(GdxMap map) {
        run(map, null, Tile.SIZE);
    }

    public static void run(GdxMap map, LwjglApplicationConfiguration config, int tileSize) {
        Tile.SIZE = FastMath.clamp(tileSize, 128, 512);

        new LwjglApplication(map, (config == null ? getConfig(map.getClass().getSimpleName()) : config));
    }

    protected static LwjglApplicationConfiguration getConfig(String title) {
        LwjglApplicationConfiguration.disableAudio = true;
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = title != null ? title : "vtm-gdx-lwjgl";

        int[] sizes = new int[]{128, 64, 32, 16};
        for (int size : sizes) {
            String path = "res/ic_vtm_" + size + ".png";
            cfg.addIcon(path, Files.FileType.Internal);
        }

        cfg.width = 1024;
        cfg.height = 768;
        cfg.stencil = 8;
        //cfg.samples = 2;
        cfg.foregroundFPS = 30;
        cfg.backgroundFPS = 10;
        cfg.forceExit = false;
        return cfg;
    }

    @Override
    protected void initGLAdapter(GLVersion version) {
        if (version.getMajorVersion() >= 3)
            GLAdapter.init(new LwjglGL30());
        else
            GLAdapter.init(new LwjglGL20());
    }

    @Override
    public void createLayers() {
        TileSource tileSource = new OSciMap4TileSource();

        initDefaultLayers(tileSource, false, true, true);

        mMap.setMapPosition(0, 0, 1 << 2);
    }

    @Override
    public void dispose() {
        System.exit(0);
    }
}
