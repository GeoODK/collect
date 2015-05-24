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
 * Responsible for creating polygons/polyline with GPS Tracking
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.MapHelper;
import com.geoodk.collect.android.widgets.GeoShapeWidget;
import com.geoodk.collect.android.widgets.GeoTraceWidget;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


public class GeoTraceActivity extends Activity {
	public int zoom_level = 3;
	public Boolean gpsStatus = false;
	private Boolean play_check = false;
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	private DefaultResourceProxyImpl resource_proxy;
	private ITileSource baseTiles;
	public MyLocationNewOverlay mMyLocationOverlay;
	private ImageButton play_button;
	private ImageButton save_button;
	private ImageButton polygon_button;
	private ImageButton clear_button;
	private Button manual_button;
	private ProgressDialog progress;
	private AlertDialog.Builder builder;
	private LayoutInflater inflater;
	private AlertDialog alert;
	private View traceSettingsView;
	private PathOverlay pathOverlay;
	private ArrayList<Marker> map_markers = new ArrayList<Marker>();
	//private GeoPoint current_location;
	private String final_return_string;
	private Integer TRACE_MODE; // 0 manual, 1 is automatic
	//private String auto_time;
	//private String auto_time_scale;
	private Boolean inital_location_found = false;
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//setGPSStatus();
		super.onResume();
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
		baseTiles = MapHelper.getTileSource(basemap);
		mapView.setTileSource(baseTiles);
		mapView.setUseDataConnection(online);
		setGPSStatus();
		//mMyLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//mMyLocationOverlay.enableMyLocation();
		

		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		disableMyLocation();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		saveGeoTrace();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geotrace_layout);
		setTitle("GeoTrace"); // Setting title of the action
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
		
		baseTiles = MapHelper.getTileSource(basemap);
		
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView = (MapView)findViewById(R.id.geotrace_mapview);
		mapView.setTileSource(baseTiles);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(online);
		mapView.getController().setZoom(zoom_level);
		
		mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(centerAroundFix);
        
        progress = new ProgressDialog(this);
        progress.setTitle("Loading Location");
        progress.setMessage("Wait while loading...");
        progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				play_button.setImageResource(R.drawable.ic_menu_mylocation);
			}
		});
        //progress.setCancelable(false);
        // To dismiss the dialog
        
        clear_button= (ImageButton) findViewById(R.id.geotrace_clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearAndReturnEmpty();
			}
        	
        });
        
        polygon_button = (ImageButton) findViewById(R.id.geotrace_polygon_button);
        polygon_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (map_markers.size()>2){
					openPolygonDialog();
				}else{
					showPolyonErrorDialog();
				}
				
			}
		});
        save_button= (ImageButton) findViewById(R.id.geotrace_save);
        save_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveConfirm();
				
			}
		});
        
        manual_button = (Button)findViewById(R.id.manual_button);
        manual_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addLocationMarker();
				
			}
		});
        
        play_button = (ImageButton)findViewById(R.id.geotrace_play_button);
        //This is the gps button and its functionality
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	//setGPSStatus();
            	if (play_check==false){
            		if (inital_location_found ==false){
            			mMyLocationOverlay.runOnFirstFix(centerAroundFix);
            			progress.show();
            			
            		}else{
            			play_button.setImageResource(R.drawable.stop_button);
            			alert.show();
            			play_check=true;
            		}
            	}else{
            		play_button.setImageResource(R.drawable.play_button);
            		play_check=false;
            		stop_play();
            	}
            }
        });
		overlayMapLayerListner();
        inflater = this.getLayoutInflater();
        traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
        buildDialog();
        
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			if ( intent.hasExtra(GeoTraceWidget.TRACE_LOCATION) ) {
				String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
				play_button.setVisibility(View.GONE);
				clear_button.setVisibility(View.VISIBLE);
				overlayIntentTrace(s);
				zoomToPoints();
			}
		}else{
			setGPSStatus();
			progress.show();

		}

		mapView.invalidate();
	}
	
	public void overlayIntentTrace(String str){
		String s = str.replace("; ",";");
		String[] sa = s.split(";");
		for (int i=0;i<(sa.length);i++){
			int x = i;
			String[] sp = sa[i].split(" ");
			double gp[] = new double[4];
			String lat = sp[0].replace(" ", "");
			String lng = sp[1].replace(" ", "");
			gp[0] = Double.valueOf(lat).doubleValue();
			gp[1] = Double.valueOf(lng).doubleValue();
			
			Marker marker = new Marker(mapView);
			GeoPoint point = new GeoPoint(gp[0], gp[1]);    
			marker.setPosition(point);
			marker.setOnMarkerClickListener(nullmarkerlistner);
			marker.setDraggable(true);
			marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			map_markers.add(marker);
			
			pathOverlay.addPoint(marker.getPosition());
			mapView.getOverlays().add(marker);
			
		}
		mapView.invalidate();
		
	}
	private void zoomToPoints(){
		mapView.getController().setZoom(15);
		mapView.invalidate();
		Handler handler=new Handler();
		Runnable r = new Runnable(){
		    public void run() {
		    	GeoPoint c_marker = map_markers.get(0).getPosition();
		    	mapView.getController().setCenter(c_marker);
		    }
		}; 
		handler.post(r);
		mapView.invalidate();
		
	}
	
	private void setGPSStatus(){
        if(gpsStatus ==false){
            //gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
        	//Toast.makeText(this, " GPS FALSE", Toast.LENGTH_LONG).show();
            upMyLocationOverlayLayers();
            
            //enableMyLocation();
            //zoomToMyLocation();
            gpsStatus = true;
        }else{
        	//Toast.makeText(this, " GPS True", Toast.LENGTH_LONG).show();
            //gps_button.setImageResource(R.drawable.ic_menu_mylocation);
            disableMyLocation();
            gpsStatus = false;
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
  private void upMyLocationOverlayLayers(){
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
    		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
    	      //      1, mLocationListener);
    		overlayMyLocationLayers();
    		//zoomToMyLocation();
    	}else{
    		showGPSDisabledAlertToUser();
    	}

    }
	private void overlayMapLayerListner(){
		pathOverlay= new PathOverlay(Color.RED, this);
		Paint pPaint = pathOverlay.getPaint();
	    pPaint.setStrokeWidth(5);
	    mapView.getOverlays().add(pathOverlay);
		mapView.invalidate();
	}
	
    private void overlayMyLocationLayers(){
        mapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
        
        
    }
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable centerAroundFix = new Runnable() {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    zoomToMyLocation();
                    progress.dismiss();
                    play_button.setImageResource(R.drawable.play_button);
                }
            });
        }
    };
    
    
    private void zoomToMyLocation(){
    	if (mMyLocationOverlay.getMyLocation()!= null){
    		inital_location_found = true;
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
    
    public void setGeoTraceMode(View view){
    	//Toast.makeText(this, " ", Toast.LENGTH_LONG).show();
    	boolean checked = ((RadioButton) view).isChecked();
    	EditText time_number = (EditText) traceSettingsView.findViewById(R.id.trace_number);
    	Spinner time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
    	switch(view.getId()) {
	        case R.id.trace_manual:
	            if (checked){
	            	TRACE_MODE = 0; 
	            	time_number.setText("");
	            	time_number.setVisibility(View.GONE);
	            	time_units.setVisibility(View.GONE);
	            	time_number.invalidate();
	            	time_units.invalidate();
	            }
	                // Pirates are the best
	            	
	            break;
	        case R.id.trace_automatic:
	            if (checked){	         
	            	TRACE_MODE = 1; 
	            	time_number.setVisibility(View.VISIBLE);
	            	time_units.setVisibility(View.VISIBLE);
	            	
	            	time_number.invalidate();
	            	time_units.invalidate();
	            }
	            break;
    	}

    	//builder(RadioGroup)findViewById(R.id.radio_group);
    	//RadioGroup rg = (RadioGroup) findViewById(R.id.radio_group);
    	//int checked = rg.getCheckedRadioButtonId();
    }
    
    
    private void buildDialog(){
    	builder = new AlertDialog.Builder(this);
    	
    	builder.setTitle("GeoTrace Instructions");
    	builder.setMessage("Manual Mode, click Start to begin use the button to record points at your location");

    	builder.setView(traceSettingsView)
        // Add action buttons
               .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   //auto_time_scale
                	   	startGeoTrace();
                       // sign in the user ...
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                       reset_trace_settings();
                   }
               })
               .setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					reset_trace_settings();
				}
               });
 
    	
    	alert = builder.create();
        //alert.show();
       
    }
    
    private void openPolygonDialog(){
    	Builder polygonBuilder = new AlertDialog.Builder(this);
    	polygonBuilder.setTitle("Polygon Connector (Non-reversible)");
    	polygonBuilder.setMessage("Select Yes to connect as polygon. For Polyline select cancel and just save");
    	polygonBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TODO Auto-generated method stub
				
					map_markers.add(map_markers.get(0));
					pathOverlay.addPoint(map_markers.get(0).getPosition());
					mapView.invalidate();
					polygon_button.setVisibility(View.GONE);
				
				
			}
		});
    	polygonBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TODO Auto-generated method stub
				
			}
		});
    	AlertDialog aD = polygonBuilder.create();
    	aD.show();
    }
    
    private void reset_trace_settings(){
    	play_button.setImageResource(R.drawable.play_button);
    	play_check=false;
    	//manual_button.setVisibility(View.GONE);
    }
    
    private void startGeoTrace(){
    	TRACE_MODE = 0; // this will change when automatic is implemented
       if (TRACE_MODE ==0){
    	   //Manual Mode
    	   /*Toast.makeText(this, "Manual Mode", Toast.LENGTH_LONG).show();*/
    	   setupManualMode();
       }else if (TRACE_MODE ==1){
    	   //Automatic Mode
    	   //Spinner scale = (Spinner)traceSettingsView.findViewById(R.id.trace_scale);
     	   //EditText time = (EditText)traceSettingsView.findViewById(R.id.trace_number);
     	   //auto_time_scale= scale.getSelectedItem().toString();
     	   //auto_time = time.getText().toString();
     	  //Toast.makeText(this, " "+auto_time+" "+auto_time_scale+" ", Toast.LENGTH_LONG).show();
       }else{
    	   reset_trace_settings();
       }

    	
    }
    private void stop_play(){
    	//Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show();
    	manual_button.setVisibility(View.GONE);
    	play_button.setVisibility(View.GONE);
    	save_button.setVisibility(View.VISIBLE);
    	polygon_button.setVisibility(View.VISIBLE);
    	
    	
    	
    }
    
    private void setupManualMode(){
    	manual_button.setVisibility(View.VISIBLE);
    }
    
    private void addLocationMarker(){
    	Toast.makeText(this, "Add Point", Toast.LENGTH_LONG).show();
    	Marker marker = new Marker(mapView);
    	//marker.setPosition(current_location);
    	marker.setPosition(mMyLocationOverlay.getMyLocation());
    	marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
    	marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		marker.setDraggable(true);
    	map_markers.add(marker);
		marker.setOnMarkerClickListener(nullmarkerlistner);
    	mapView.getOverlays().add(marker);
    	pathOverlay.addPoint(marker.getPosition());
    	mapView.invalidate();
    }
    
    private void saveGeoTrace(){
    	//Toast.makeText(this, "Do Save Stuff", Toast.LENGTH_LONG).show();
    	returnLocation();
    	finish();
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
	private void saveConfirm(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you are done?")
               .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   saveGeoTrace();
                }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
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
                FormEntryActivity.GEOTRACE_RESULTS,
                final_return_string);
            setResult(RESULT_OK, i);
        finish();
    }
    private OnMarkerClickListener nullmarkerlistner= new Marker.OnMarkerClickListener() {
		
		@Override
		public boolean onMarkerClick(Marker arg0, MapView arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	private void clearAndReturnEmpty(){
		final_return_string = "";
		Intent i = new Intent();
		i.putExtra(
                FormEntryActivity.GEOTRACE_RESULTS,
                final_return_string);
            setResult(RESULT_OK, i);
        setResult(RESULT_OK, i);
        finish();
		
	}
}
