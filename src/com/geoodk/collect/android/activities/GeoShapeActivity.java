package com.geoodk.collect.android.activities;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.preferences.MapSettings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class GeoShapeActivity extends Activity {
	private MapView mapView;
	private ITileSource baseTiles;
	private DefaultResourceProxyImpl resource_proxy;
	public int zoom_level = 3;
	String point1;
	String point2;
	String point3;
	String point4;
	public String final_return_string;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geo_shape_layout);
		Button return_button = (Button) findViewById(R.id.geoshape_Button);
		
		//Map Settings
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, false);
				String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
				setbasemapTiles(basemap);
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		
		mapView = (MapView)findViewById(R.id.geoshape_mapview);
		mapView.setTileSource(baseTiles);
		//final CustomTileSource tileSource = new CustomTileSource(Environment.getExternalStorageDirectory().getPath()+ "/osmdroid/tiles/MyMap", null);
		//mapView.setTileSource(tileSource);
		//String h = Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles";
		//File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(online);
		mapView.setMapListener(new MapListener() {
			@Override
			public boolean onZoom(ZoomEvent zoomLev) {
				zoom_level = zoomLev.getZoomLevel();
				return false;
			}
			@Override
			public boolean onScroll(ScrollEvent arg0) {
				return false;
			}
		});
		mapView.getController().setZoom(zoom_level);
		mapView.invalidate();
		point1 = "36.15524344399181 -81.80068351328373 0.0 0.0;";
		point2 = "37.092621633465136 -98.3268303796649 0.0 0.0;";
		point3 = "31.962375302600794 -92.09862772375345 0.0 0.0;";
		point4 = "36.15524344399181 -81.80068351328373 0.0 0.0;";
		
		final_return_string = point1+point2+point3;
		//final_return_string = point1;
		return_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				returnLocation();
				
			}
		});
		
	}
	
    private void returnLocation() {
            Intent i = new Intent();
            i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                final_return_string);
            setResult(RESULT_OK, i);
        finish();
    }
    
	private void setbasemapTiles(String basemap) {
		// TODO Auto-generated method stub
		
		if (basemap.equals("MAPNIK")){
			baseTiles = TileSourceFactory.MAPNIK;
		}else if (basemap.equals("CYCLEMAP")){
			baseTiles = TileSourceFactory.CYCLEMAP;
		}else if (basemap.equals("PUBLIC_TRANSPORT")){
			baseTiles = TileSourceFactory.PUBLIC_TRANSPORT;
		}else if(basemap.equals("CLOUDMADESTANDARDTILES")){
			baseTiles = TileSourceFactory.CLOUDMADESTANDARDTILES;
		}else if(basemap.equals("CLOUDMADESMALLTILES")){
			baseTiles = TileSourceFactory.CLOUDMADESMALLTILES;
		}else if(basemap.equals("MAPQUESTOSM")){
			baseTiles = TileSourceFactory.MAPQUESTOSM;
		}else if(basemap.equals("MAPQUESTAERIAL")){
			baseTiles = TileSourceFactory.MAPQUESTAERIAL;
		}else{
			baseTiles = TileSourceFactory.MAPQUESTOSM;
		}
	
		
	}
}
