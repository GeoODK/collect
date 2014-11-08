/*
 * Copyright (C) 2014 GeoODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Responsible for creating polygons
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.CustomMarkerHelper;
import com.geoodk.collect.android.spatial.CustomPopupMaker;
import com.geoodk.collect.android.spatial.MBTileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class GeoShapeActivity extends Activity implements IRegisterReceiver {
	private MapView mapView;
	private ArrayList<GeoPoint> map_points =new ArrayList<GeoPoint>();
	private ArrayList<Marker> map_markers = new ArrayList<Marker>();
	private PathOverlay pathOverlay;
	private ITileSource baseTiles;
	private DefaultResourceProxyImpl resource_proxy;
	public int zoom_level = 3;
	private static final int stroke_width = 5;
	public String final_return_string;
	private MapEventsOverlay OverlayEventos;
	private boolean polygon_connection = false;
	private boolean clear_button_test = false;
	private ImageButton clear_button;
	private ImageButton return_button;
	private ImageButton polygon_button;
	private SharedPreferences sharedPreferences;
	public Boolean layerStatus = false;
	private int selected_layer= -1;
	
	private MBTileProvider mbprovider;
	private TilesOverlay mbTileOverlay;
	public Boolean gpsStatus = true;
	private ImageButton gps_button;
	private String[] OffilineOverlays;
	public MyLocationNewOverlay mMyLocationOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geo_shape_layout);
		setTitle("GeoShape"); // Setting title of the action
		return_button = (ImageButton) findViewById(R.id.geoshape_Button);
		polygon_button = (ImageButton) findViewById(R.id.polygon_button);
		clear_button = (ImageButton) findViewById(R.id.clear_button);
		//Map Settings
		//SharedPreferences sharedPreferences = PreferenceManager
		//		.getDefaultSharedPreferences(this);
		//PreferenceManager.setDefaultValues(this, R.xml.map_preferences, false);
		//sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
		
		setbasemapTiles(basemap);
		
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView = (MapView)findViewById(R.id.geoshape_mapview);
		mapView.setTileSource(baseTiles);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(online);
		mapView.setMapListener(mapViewListner);
		overlayMapLayerListner();
		//mapView.getController().setZoom(zoom_level);
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
						polygon_button.setVisibility(View.GONE);
						mapView.getOverlays().remove(OverlayEventos);
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
						clearFeatures();
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
        ImageButton layers_button = (ImageButton)findViewById(R.id.geoShape_layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showLayersDialog();
				
			}
		});
        
        gps_button = (ImageButton)findViewById(R.id.geoshape_gps_button);
        //This is the gps button and its functionality
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	setGPSStatus();
            }
        });
        
        //Initial Map Setting before Location is found
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //Do something after 100ms
                GeoPoint  point = new GeoPoint(34.08145, -39.85007);               
                mapView.getController().setZoom(3);
                mapView.getController().setCenter(point);
            }
        }, 100);
        
        //geoshape_gps_button
        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(centerAroundFix);
        setGPSStatus();
	    mapView.invalidate();
	}
	
	private void setGPSStatus(){
        if(gpsStatus ==false){
            gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
            upMyLocationOverlayLayers();
            //enableMyLocation();
            //zoomToMyLocation();
            gpsStatus = true;
        }else{
            gps_button.setImageResource(R.drawable.ic_menu_mylocation);
            disableMyLocation();
            gpsStatus = false;
        }
    }
    
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable centerAroundFix = new Runnable() {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    zoomToMyLocation();
                }
            });
        }
    };
    
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        .setCancelable(false)
        .setPositiveButton("Enable GPS",
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
               // Intent callGPSSettingIntent = new Intent(
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                //startActivity(callGPSSettingIntent);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    
    private void upMyLocationOverlayLayers(){
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
    		overlayMyLocationLayers();
    		//zoomToMyLocation();
    	}else{
    		showGPSDisabledAlertToUser();
    	}

    }

    private void overlayMyLocationLayers(){
        mapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
    }
    private void zoomToMyLocation(){
    	if (mMyLocationOverlay.getMyLocation()!= null){
    		if (zoom_level ==3){
    			mapView.getController().setZoom(15);
    		}else{
    			mapView.getController().setZoom(zoom_level);
    		}
    		mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
    		//mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
    	}else{
    		mapView.getController().setZoom(zoom_level);
    	}
    	
    }
    private void disableMyLocation(){
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
        	mMyLocationOverlay.setEnabled(false);
        	mMyLocationOverlay.disableFollowLocation();
        	mMyLocationOverlay.disableMyLocation();
        	gpsStatus =false;
    	}
    }
    
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPNIK");
		setbasemapTiles(basemap);
		mapView.setTileSource(baseTiles);
		mapView.setUseDataConnection(online);
		setGPSStatus();
	}
	
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		disableMyLocation();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		disableMyLocation();
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
		clear_button_test = false;
		map_markers.clear();
		pathOverlay.clearPath();
		mapView.getOverlays().clear();
		mapView.invalidate();
		polygon_button.setVisibility(View.VISIBLE);
		clear_button.setVisibility(View.GONE);
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
            this.baseTiles = TileSourceFactory.MAPNIK;
        }else if (basemap.equals("CYCLEMAP")){
            this.baseTiles = TileSourceFactory.CYCLEMAP;
        }else if (basemap.equals("PUBLIC_TRANSPORT")){
            this.baseTiles = TileSourceFactory.PUBLIC_TRANSPORT;
        }else if(basemap.equals("MAPQUESTOSM")){
            this.baseTiles = TileSourceFactory.MAPQUESTOSM;
        }else if(basemap.equals("MAPQUESTAERIAL")){
            this.baseTiles = TileSourceFactory.MAPQUESTAERIAL;
        }else{
            this.baseTiles = TileSourceFactory.MAPQUESTOSM;
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
	private void showLayersDialog() {
		// TODO Auto-generated method stub
		//FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
		//View view=fl.inflate(self, R.layout.showlayers_layout, null);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(GeoShapeActivity.this);
		alertDialog.setTitle("Select Offline Layer");
		OffilineOverlays = getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
		//alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
		alertDialog.setSingleChoiceItems(OffilineOverlays,selected_layer,new  DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int item) {
            	   //Toast.makeText(OSM_Map.this,item, Toast.LENGTH_LONG).show();
                  // The 'which' argument contains the index position
                   // of the selected item
		           //Toast.makeText(OSM_Map.this,item +" ", Toast.LENGTH_LONG).show();
            	   
		            switch(item){
		            case 0 :
		            	mapView.getOverlays().remove(mbTileOverlay);
		            	layerStatus =false;
		            	//updateMapOverLayOrder();
		            	break;
		            default:
		            		layerStatus = true;
		            	    mapView.getOverlays().remove(mbTileOverlay);
		            		//String mbTileLocation = getMBTileFromItem(item);
		            		String mbFilePath = getMBTileFromItem(item);
		            	    //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
		            		File mbFile = new File(mbFilePath);
		            		mbprovider = new MBTileProvider(GeoShapeActivity.this, mbFile);
			           		mbTileOverlay = new TilesOverlay(mbprovider,GeoShapeActivity.this);
			           		mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
			           		//updateMapOverLayOrder();
				           	mapView.getOverlays().add(mbTileOverlay);
			           		updateMapOverLayOrder();
				           	mapView.invalidate();
			               }
	            	//This resets the map and sets the selected Layer
	            	selected_layer =item;
	            	dialog.dismiss();
	           		final Handler handler = new Handler();
	        		handler.postDelayed(new Runnable() {
	        		  @Override
	        		  public void run() {
	        			  mapView.invalidate();
	        		  }
	        		}, 400);
	           		
		            }             
        	});
		//alertDialog.setView(view);
		alertDialog.show();
		
	}
	
	private void updateMapOverLayOrder(){
		List<Overlay> overlays = mapView.getOverlays();
		if (layerStatus =true){
			mapView.getOverlays().remove(mbTileOverlay);
			mapView.getOverlays().remove(pathOverlay);
			mapView.getOverlays().add(mbTileOverlay);
			mapView.getOverlays().add(pathOverlay);
			
		}
		for (Overlay overlay : overlays){
			//Class x = overlay.getClass();
			final Overlay o = overlay;
			if (overlay.getClass() == Marker.class){
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
				    public void run() {
				    	mapView.getOverlays().remove(o);
				    	mapView.invalidate();
				    }
				}, 100);
				handler.postDelayed(new Runnable() {
				    public void run() {
				    	mapView.getOverlays().add(o);
				    	mapView.invalidate();
				    }
				}, 100);
				//mapView.getOverlays().remove(overlay);
				//mapView.getOverlays().add(overlay);
				
			}
		}
		mapView.invalidate();
		
	}
	
	private String getMBTileFromItem(int item) {
		// TODO Auto-generated method stub
		String foldername = OffilineOverlays[item];
		File dir = new File(Collect.OFFLINE_LAYERS+File.separator+foldername);
		String mbtilePath;
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".mbtiles");
		    }
		});
		mbtilePath =Collect.OFFLINE_LAYERS+File.separator+foldername+File.separator+files[0].getName();
		//returnFile = new File(Collect.OFFLINE_LAYERS+File.separator+foldername+files[0]);
		
		return mbtilePath;
	}
	 private String[] getOfflineLayerList() {
		// TODO Auto-generated method stub
		 File files = new File(Collect.OFFLINE_LAYERS);
		 ArrayList<String> results = new ArrayList<String>();
		 results.add("None");
		 String[] overlay_folders =  files.list();
		 for(int i =0;i<overlay_folders.length;i++){
			 results.add(overlay_folders[i]);
			 //Toast.makeText(self, overlay_folders[i]+" ", Toast.LENGTH_LONG).show();
		 }
		 String[] finala = new String[results.size()]; 
		 finala = results.toArray(finala);
		 /*for(int j = 0;j<finala.length;j++){
			 Toast.makeText(self, finala[j]+" ", Toast.LENGTH_LONG).show();
		 }*/
		return finala;
	}

	
	
}
