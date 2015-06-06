/*
 * Copyright (C) 2015 GeoODK
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
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import com.geoodk.collect.android.R;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.MapHelper;
import com.geoodk.collect.android.widgets.GeoTraceWidget;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
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


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



public class GeoTraceActivity extends Activity {
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	//private ScheduledFuture beeperHandle;
	private ScheduledFuture schedulerHandler;
	public int zoom_level = 3;
	public Boolean gpsStatus = true;
	private Boolean play_check = false;
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	public DefaultResourceProxyImpl resource_proxy;
	private ITileSource baseTiles;
	public MyLocationNewOverlay mMyLocationOverlay;
	private ImageButton play_button;
	private ImageButton save_button;
	public ImageButton polygon_button;
	public ImageButton clear_button;
	private Button manual_button;
	private ProgressDialog progress;
	public AlertDialog.Builder builder;
	public LayoutInflater inflater;
	private AlertDialog alert;
	private View traceSettingsView;
	private PathOverlay pathOverlay;
	private ArrayList<Marker> map_markers = new ArrayList<>();
	private String final_return_string;
	private Integer TRACE_MODE; // 0 manual, 1 is automatic
	private Boolean inital_location_found = false;
	private	EditText time_number;
	private Spinner time_units;
	private Spinner time_delay;
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
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
		super.onPause();
		mMyLocationOverlay.enableMyLocation();
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		disableMyLocation();
	}

	@Override
	protected void onDestroy() {
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
		setTitle(getString(R.string.geotrace_title)); // Setting title of the action

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
        progress.setTitle(getString(R.string.getting_location));
        progress.setMessage(getString(R.string.please_wait_long));
        progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				play_button.setImageResource(R.drawable.ic_menu_mylocation);
			}
		});
        //progress.setCancelable(false);
        // To dismiss the dialog

        clear_button= (ImageButton) findViewById(R.id.geotrace_clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clearAndReturnEmpty();
			}

        });

        polygon_button = (ImageButton) findViewById(R.id.geotrace_polygon_button);
        polygon_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
				saveConfirm();

			}
		});

        manual_button = (Button)findViewById(R.id.manual_button);
        manual_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addLocationMarker();

			}
		});

        play_button = (ImageButton)findViewById(R.id.geotrace_play_button);
        //This is the gps button and its functionality
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	//setGPSStatus();
            	if (!play_check){
            		if (!inital_location_found){
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
                    try{
                        schedulerHandler.cancel(true);
                    }catch (Exception e){
                        // Do nothing
                    }
                    //beeperHandle.cancel(true);

					disableMyLocation();
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

	/*
		This functions handels the delay and the Runable for
	*/

	public void setGeoTraceScheuler(long delay, TimeUnit units){
		schedulerHandler = scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addLocationMarker();
					}
				});
			}
		},delay, delay, units);

	}



	public void overlayIntentTrace(String str){
		String s = str.replace("; ",";");
		String[] sa = s.split(";");
		for (int i=0;i<(sa.length);i++){
			String[] sp = sa[i].split(" ");
			double gp[] = new double[4];

			String lat = sp[0].replace(" ", "");
			String lng = sp[1].replace(" ", "");
			String altStr = sp[2].replace(" ", "");
			String acu = sp[3].replace(" ", "");

			gp[0] = Double.parseDouble(lat);
			gp[1] = Double.parseDouble(lng);

			Double alt = Double.parseDouble(altStr);
			Marker marker = new Marker(mapView);
			marker.setSubDescription(acu);
			GeoPoint point = new GeoPoint(gp[0], gp[1]);
			point.setAltitude(alt.intValue());
			marker.setPosition(point);
			marker.setOnMarkerClickListener(nullmarkerlistner);
			marker.setDraggable(true);
			marker.setOnMarkerDragListener(draglistner);
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
		upMyLocationOverlayLayers();
		gpsStatus = true;
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
        alertDialogBuilder.setMessage(getString(R.string.enable_gps_message))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.enable_gps),
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
               // Intent callGPSSettingIntent = new Intent(
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                //startActivity(callGPSSettingIntent);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    //This happens on click of the play button
    public void setGeoTraceMode(View view){
    	boolean checked = ((RadioButton) view).isChecked();
		time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
//    	time_number = (EditText) traceSettingsView.findViewById(R.id.trace_number);
    	time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
    	switch(view.getId()) {
	        case R.id.trace_manual:
	            if (checked){
	            	TRACE_MODE = 0;
	            	time_number.setVisibility(View.GONE);
	            	time_units.setVisibility(View.GONE);
					time_delay.setVisibility(View.GONE);
	            	time_number.invalidate();
					time_delay.invalidate();
	            	time_units.invalidate();
	            }
	            break;
	        case R.id.trace_automatic:
	            if (checked){	         
	            	TRACE_MODE = 1; 
	            	//time_number.setVisibility(View.VISIBLE);
	            	time_units.setVisibility(View.VISIBLE);
					time_delay.setVisibility(View.VISIBLE);
	            	//time_number.invalidate();
					time_delay.invalidate();
	            	time_units.invalidate();
	            }
	            break;
    	}
    }
    
    
    private void buildDialog(){
    	builder = new AlertDialog.Builder(this);
    	
    	builder.setTitle(getString(R.string.geotrace_instruction));
    	builder.setMessage(getString(R.string.geotrace_instruction_message));

    	builder.setView(traceSettingsView)
        // Add action buttons
               .setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   	startGeoTrace();
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
					   reset_trace_settings();
				   }
			   });
 
    	
    	alert = builder.create();
        //alert.show();
       
    }
    
    private void openPolygonDialog(){
    	Builder polygonBuilder = new AlertDialog.Builder(this);
    	polygonBuilder.setTitle(getString(R.string.polygon_conection_title));
    	polygonBuilder.setMessage(getString(R.string.polygon_conection_message));
    	polygonBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
					map_markers.add(map_markers.get(0));
					pathOverlay.addPoint(map_markers.get(0).getPosition());
					mapView.invalidate();
					polygon_button.setVisibility(View.GONE);
				
				
			}
		});
    	polygonBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {

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
		RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
		int radioButtonID = rb.getCheckedRadioButtonId();
		View radioButton = rb.findViewById(radioButtonID);
		int idx = rb.indexOfChild(radioButton);
    	TRACE_MODE = idx; // this will change when automatic is implemented
       if (TRACE_MODE ==0){
    	   //Manual Mode
    	   /*Toast.makeText(this, "Manual Mode", Toast.LENGTH_LONG).show();*/
    	   setupManualMode();
       }else if (TRACE_MODE ==1){
		   Toast.makeText(this, "Mode: Automatic", Toast.LENGTH_LONG).show();
    	   //Automatic Mode
		   setupAutomaticMode();
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
		//setGeoTraceScheuler(10,TimeUnit.MINUTES);

    }
	private void setupAutomaticMode(){
		manual_button.setVisibility(View.VISIBLE);
		String delay = time_delay.getSelectedItem().toString();
		String units = time_units.getSelectedItem().toString();
		TimeUnit time_units_value;
		if (units =="Minutes"){
			time_units_value = TimeUnit.MINUTES;
		}else{
			//in Seconds
			time_units_value = TimeUnit.SECONDS;
		}
		Long time_delay = Long.parseLong(delay);
		setGeoTraceScheuler(time_delay, time_units_value);
	}
    
    private void addLocationMarker(){
    	Toast.makeText(this, "Add Point", Toast.LENGTH_LONG).show();
    	Marker marker = new Marker(mapView);
    	//marker.setPosition(current_location);
    	marker.setPosition(mMyLocationOverlay.getMyLocation());
		Float last_know_acuracy = mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
		mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
		marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(draglistner);
		//Place holder to Accuracy
		marker.setSubDescription(Float.toString(last_know_acuracy));
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
					
				}
			}).show();
		
	}
	
	private String generateReturnString() {
		String temp_string = "";
		for (int i = 0 ; i < map_markers.size();i++){
			String lat = Double.toString(map_markers.get(i).getPosition().getLatitude());
			String lng = Double.toString(map_markers.get(i).getPosition().getLongitude());
			String alt = Integer.toString(map_markers.get(i).getPosition().getAltitude());
			String acu = map_markers.get(i).getSubDescription();
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
	private void update_polygon(){
		pathOverlay.clearPath();
		for (int i =0;i<map_markers.size();i++){
			pathOverlay.addPoint(map_markers.get(i).getPosition());
		}
		mapView.invalidate();
	}



	private OnMarkerDragListener draglistner = new Marker.OnMarkerDragListener(){
		@Override
		public void onMarkerDragStart(Marker marker) {

		}
		@Override
		public void onMarkerDragEnd(Marker arg0) {
			update_polygon();

		}
		@Override
		public void onMarkerDrag(Marker marker) {
			update_polygon();

		}

	} ;
}
