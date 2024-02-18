/*
 * Copyright 2018-2020 devemux86
 * Copyright 2020 Meibes
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
package org.oscim.android.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.*;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

@SuppressWarnings("deprecation")
public class MapFragment extends android.app.Fragment implements XmlRenderThemeMenuCallback {

    // Request code for selecting a map file / theme file
    private static final int SELECT_MAP_FILE = 0;
    private static final int SELECT_THEME_FILE = 1;

    private MapView mapView;
    private IRenderTheme theme;

    public static MapFragment newInstance() {
        MapFragment instance = new MapFragment();

        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = new MapView(getActivity());
        RelativeLayout relativeLayout = view.findViewById(R.id.mapView);
        relativeLayout.addView(mapView);

        // Open map
        Toast.makeText(getActivity(), "Select a map file", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_MAP_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_MAP_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                loadMap(uri);

                // Open theme
                Toast.makeText(getActivity(), "Map file selected. Now, optionally select a theme.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, SELECT_THEME_FILE);
            }
        } else if (requestCode == SELECT_THEME_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            loadTheme(uri);
        }
    }

    private void loadMap(Uri mapUri) {
        try {
            // Tile source
            MapFileTileSource tileSource = new MapFileTileSource();
            FileInputStream fis = (FileInputStream) getActivity().getContentResolver().openInputStream(mapUri);
            tileSource.setMapFileInputStream(fis);

            // Vector layer
            VectorTileLayer tileLayer = mapView.map().setBaseMap(tileSource);

            // Building layer
            mapView.map().layers().add(new BuildingLayer(mapView.map(), tileLayer));

            // Label layer
            mapView.map().layers().add(new LabelLayer(mapView.map(), tileLayer));

            if (theme == null) {
                theme = mapView.map().setTheme(VtmThemes.DEFAULT);
            }

            // Scale bar
            MapScaleBar mapScaleBar = new DefaultMapScaleBar(mapView.map());
            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
            mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
            mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
            mapView.map().layers().add(mapScaleBarLayer);

            // initial position
            MapInfo info = tileSource.getMapInfo();
            if (!info.boundingBox.contains(mapView.map().getMapPosition().getGeoPoint())) {
                MapPosition pos = new MapPosition();
                pos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
                mapView.map().setMapPosition(pos);
            }

        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
    }

    private void loadTheme(Uri themeUri) {
        try {
            if (theme != null) {
                theme.dispose();
            }

            // Render theme (themeUri may be null, using default theme then)
            if (themeUri != null) {
                final List<String> xmlThemes = ZipXmlThemeResourceProvider.scanXmlThemes(new ZipInputStream(new BufferedInputStream(getActivity().getContentResolver().openInputStream(themeUri))));
                if (xmlThemes.isEmpty()) {
                    return;
                }
                ThemeFile themeFile = new ZipRenderTheme(xmlThemes.get(0), new ZipXmlThemeResourceProvider(new ZipInputStream(new BufferedInputStream(getActivity().getContentResolver().openInputStream(themeUri)))));
                theme = mapView.map().setTheme(themeFile);
            } else {
                theme = mapView.map().setTheme(VtmThemes.DEFAULT);
            }

        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getCategories(final XmlRenderThemeStyleMenu menu) {
        // ignore theme settings for now
        return new HashSet<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        if (theme != null) {
            theme.dispose();
            theme = null;
        }
        super.onDestroyView();
    }
}
