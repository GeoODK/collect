package com.geoodk.collect.android.activities;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.CustomMarkerHelper;
import com.geoodk.collect.android.spatial.CustomPopupMaker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class GeoShapeActivity extends Activity {
	private MapView mapView;
	private ArrayList<GeoPoint> map_points =new ArrayList<GeoPoint>();
	private ArrayList<Marker> map_markers = new ArrayList<Marker>();
	private PathOverlay myOverlay= new PathOverlay(Color.RED, this);
	private ITileSource baseTiles;
	private DefaultResourceProxyImpl resource_proxy;
	public int zoom_level = 10;
	String point1;
	String point2;
	String point3;
	String point4;
	public String final_return_string;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geo_shape_layout);
		ImageButton return_button = (ImageButton) findViewById(R.id.geoshape_Button);
		
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
		mapView.getController().setCenter(new GeoPoint(13.002798, 77.580000));
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
		MapEventsReceiver mReceive = new MapEventsReceiver() {

			@Override
			public boolean longPressHelper(GeoPoint point) {
				// TODO Auto-generated method stub
				//Toast.makeText(GeoShapeActivity.this, point.getLatitude()+" ", Toast.LENGTH_LONG).show();
				//map_points.add(point);
				Marker marker = new Marker(mapView);
				marker.setPosition(point);
				marker.setDraggable(true);
				marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
				map_markers.add(marker);
				setMarkers();
				//mapView.getOverlays().add(marker);
				//mapView.invalidate();
				return false;
			}

			@Override
			public boolean singleTapConfirmedHelper(GeoPoint arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		MapEventsOverlay OverlayEventos = new MapEventsOverlay(getBaseContext(), mReceive);
		mapView.getOverlays().add(OverlayEventos);
		
		
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
		
	    int diff=1000;

	    GeoPoint pt1=new GeoPoint(13.002798, 77.580000);
	    GeoPoint pt2= new GeoPoint(pt1.getLatitudeE6()+diff, pt1.getLongitudeE6());
	    GeoPoint pt3= new GeoPoint(pt1.getLatitudeE6()+diff, pt1.getLongitudeE6()+diff);
	    GeoPoint pt4= new GeoPoint(pt1.getLatitudeE6(), pt1.getLongitudeE6()+diff);
	    GeoPoint pt5= new GeoPoint(pt1);


	    //PathOverlay myOverlay= new PathOverlay(Color.RED, this);
	    //myOverlay.getPaint().setStyle(Paint.Style.FILL);

	    myOverlay.addPoint(pt1);
	    myOverlay.addPoint(pt2);
	    myOverlay.addPoint(pt3);
	    myOverlay.addPoint(pt4);
	    myOverlay.addPoint(pt5);

	    mapView.getOverlays().add(myOverlay);
	    mapView.invalidate();
		
		
		
	}
	private void setMarkers() {
		// TODO Auto-generated method stub
		
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
		}else if(basemap.equals("MAPQUESTOSM")){
			baseTiles = TileSourceFactory.MAPQUESTOSM;
		}else if(basemap.equals("MAPQUESTAERIAL")){
			baseTiles = TileSourceFactory.MAPQUESTAERIAL;
		}else{
			baseTiles = TileSourceFactory.MAPQUESTOSM;
		}
	
		
	}
}
