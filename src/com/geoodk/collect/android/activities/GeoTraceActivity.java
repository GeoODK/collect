package com.geoodk.collect.android.activities;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.MapHelper;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class GeoTraceActivity extends Activity {
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	private DefaultResourceProxyImpl resource_proxy;
	private ITileSource baseTiles;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geotrace_layout);
		setTitle("GeoTrace"); // Setting title of the action
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
		
		setbasemapTiles(basemap);
		
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView = (MapView)findViewById(R.id.geotrace_mapview);
		//mapView.setTileSource(baseTiles);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(online);

		//mapView.setMapListener(mapViewListner);
		//overlayMapLayerListner();
		//mapView.getController().setZoom(zoom_level);
		//ITileSource cctileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png","http://tile.openstreetmap.org/");
		String[] baseURL = new String[]{"http://tiles.osm.moabi.org/moabi_base/"}; 
		ITileSource tile = new XYTileSource("Moabli",null, 4, 17, 256, ".png", baseURL);
		mapView.setTileSource(tile);

		//public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
          //      ResourceProxy.string.mapnik, 0, 18, 256, ".png", "http://tile.openstreetmap.org/");
		//mMapView.setTileSource(tileSource);
		//OnlineTileSourceBase source = new XYTileSource("tiles", ResourceProxy.string.offline_mode, 0, 4, 256, ".png", "");
		
		mapView.invalidate();
	}
	
    private void setbasemapTiles(final String basemap) {
        // TODO Auto-generated method stub
    	baseTiles = MapHelper.getTileSource(basemap);
    }

	
}
