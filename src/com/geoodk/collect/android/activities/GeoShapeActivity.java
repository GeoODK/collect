package com.geoodk.collect.android.activities;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
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
	private PathOverlay pathOverlay;
	private ITileSource baseTiles;
	private DefaultResourceProxyImpl resource_proxy;
	public int zoom_level = 10;
	private static final int stroke_width = 5;
	public String final_return_string;
	private MapEventsOverlay OverlayEventos;
	private boolean polygon_connection = false;
	private boolean clear_button_test = false;
	private ImageButton clear_button;
	private ImageButton return_button;
	private ImageButton polygon_button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geo_shape_layout);
		setTitle("LONG CLICK to Create Point"); // Setting title of the action
		ImageButton return_button = (ImageButton) findViewById(R.id.geoshape_Button);
		ImageButton polygon_button = (ImageButton) findViewById(R.id.polygon_button);
		clear_button = (ImageButton) findViewById(R.id.clear_button);
		
		//Map Settings
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, false);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
		
		setbasemapTiles(basemap);
		
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView = (MapView)findViewById(R.id.geoshape_mapview);
		mapView.setTileSource(baseTiles);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(online);
		mapView.getController().setCenter(new GeoPoint(13.002798, 77.580000));
		mapView.setMapListener(mapViewListner);
		overlayMapLayerListner();
		mapView.getController().setZoom(zoom_level);
		mapView.invalidate();
		return_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnLocation();
			}
		});
		polygon_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (polygon_connection ==true){
					showClearDialog();
				}else{
					if (map_markers.size()>2){
						int p = map_markers.size();
						map_markers.add(map_markers.get(0));
						pathOverlay.addPoint(map_markers.get(0).getPosition());
						mapView.invalidate();
						polygon_connection= true;
					}else{
						showPolyonErrorDialog();
					}
				}
			}
		});
		
		
		clear_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (map_markers.size() != 0){
					if (polygon_connection ==true){
						showClearDialog();
					}else{
						Marker c_mark = map_markers.get(map_markers.size()-1);
						mapView.getOverlays().remove(c_mark);
						map_markers.remove(map_markers.size()-1);
						update_polygon();
						mapView.invalidate();
					}
				}
			}
		});
		
	    mapView.invalidate();
	}
	private void overlayMapLayerListner(){
		OverlayEventos = new MapEventsOverlay(getBaseContext(), mReceive);
		pathOverlay= new PathOverlay(Color.RED, this);
		Paint pPaint = pathOverlay.getPaint();
	    pPaint.setStrokeWidth(5);
	    mapView.getOverlays().add(pathOverlay);
		mapView.getOverlays().add(OverlayEventos);
		mapView.invalidate();
	}
	private void clearFeatures(){
		polygon_connection = false;
		map_markers.clear();
		pathOverlay.clearPath();
		mapView.getOverlays().clear();
		mapView.invalidate();		
		overlayMapLayerListner();
		
	}
	private void showClearDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Polygon already created. Would you like to CLEAR the feature?")
               .setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                	   clearFeatures();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   
                   }
               }).show();
        
	}
	private void showPolyonErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Must have at least 3 points to create Polygon")
               .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                }
               }).show();
		
	}
	private String generateReturnString() {
		String temp_string = "";
		for (int i = 0 ; i < map_markers.size();i++){
			String lat = Double.toString(map_markers.get(i).getPosition().getLatitude());
			String lng = Double.toString(map_markers.get(i).getPosition().getLongitude());
			String alt ="0.0";
			String acu = "0.0";
			temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
		}
		return temp_string;
	}
	
    private void returnLocation(){
    		final_return_string = generateReturnString();
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
	
	private void update_polygon(){
		pathOverlay.clearPath();
		for (int i =0;i<map_markers.size();i++){
			pathOverlay.addPoint(map_markers.get(i).getPosition());
		}
		mapView.invalidate();
	}
	private MapEventsReceiver mReceive = new MapEventsReceiver() {
		@Override
		public boolean longPressHelper(GeoPoint point) {
			// TODO Auto-generated method stub
			//Toast.makeText(GeoShapeActivity.this, point.getLatitude()+" ", Toast.LENGTH_LONG).show();
			//map_points.add(point);
			if (clear_button_test ==false){
				clear_button.setVisibility(View.VISIBLE);
				clear_button_test = true;
			}			
			Marker marker = new Marker(mapView);
			marker.setPosition(point);
			marker.setDraggable(true);
			marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			map_markers.add(marker);
			marker.setDraggable(true);
			marker.setOnMarkerDragListener(draglistner);
			mapView.getOverlays().add(marker);
			pathOverlay.addPoint(marker.getPosition());
			mapView.invalidate();
			return false;
		}

		@Override
		public boolean singleTapConfirmedHelper(GeoPoint arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private MapListener mapViewListner = new MapListener() {
		@Override
		public boolean onZoom(ZoomEvent zoomLev) {
			zoom_level = zoomLev.getZoomLevel();
			return false;
		}
		@Override
		public boolean onScroll(ScrollEvent arg0) {
			return false;
		}
		
	};
	
	private OnMarkerDragListener draglistner = new OnMarkerDragListener() {
		@Override
		public void onMarkerDragStart(Marker marker) {
			
		}
		@Override
		public void onMarkerDragEnd(Marker arg0) {
			// TODO Auto-generated method stub
			update_polygon();
			
		}
		@Override
		public void onMarkerDrag(Marker marker) {
			update_polygon();
			
		}
	};
	
	
	
}
