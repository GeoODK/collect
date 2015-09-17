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
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.GeoRender;
import com.geoodk.collect.android.spatial.MapHelper;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GeoODKMapThemeActivity extends Activity {
	private static final String t = "GeoODK";
	private static boolean EXIT = true;
	private AlertDialog mAlertDialog;
	private String[] assestFormList;
	private MapView mapView;
	public SharedPreferences sharedPreferences;
	private DefaultResourceProxyImpl resource_proxy;
	private ITileSource baseTiles;
	private String basemap;
	private Boolean online;
	private ImageButton gps_button;
	private ImageButton grid;
	private ImageButton collect_button;
	private ImageButton layers_button;
	public Boolean gpsStatus = true;
	public MyLocationNewOverlay mMyLocationOverlay;
	private final Context self = this;
	public int zoom_level =-1;

	private GeoRender geoRender;

	
    public static final String FORMS_PATH = Collect.ODK_ROOT + File.separator + "forms";
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.disableMyLocation();
		clearMapMarkers();
	}
	@Override
	protected void onResume() {
		//Initializing all the
		super.onResume(); // Find out what this does? bar
		online = this.sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		basemap = this.sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
//		hideInfoWindows();
		baseTiles = MapHelper.getTileSource(basemap);
		mapView.setTileSource(this.baseTiles);
		mapView.setUseDataConnection(this.online);
		drawMarkers();
		setGPSStatus();

		mapView.invalidate();
	}

	private void clearMapMarkers() {
		mapView.getOverlays().clear();
//		markerListArray.clear();
	}

	private void drawMarkers(){
		geoRender = new GeoRender(this.getApplicationContext(),mapView);
	}
	@Override
	public void finish() {
		ViewGroup view = (ViewGroup) getWindow().getDecorView();
		view.removeAllViews();
		super.finish();
	}
	@Override
	public void onDetachedFromWindow(){
		super.onDetachedFromWindow();
		setVisible(false);
	}
	@Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoodk_maptheme_layout);
        

        Log.i(t, "Starting up, creating directories");
		try {
			Collect.createODKDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}
		assestFormList = getAssetFormList();
		copyForms(assestFormList);

		// Testing



		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		resource_proxy = new DefaultResourceProxyImpl(this.getApplicationContext());
		mapView = (MapView)this.findViewById(R.id.MapViewId);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		//mapView.setUseDataConnection(online);
		mapView.setMapListener(new MapListener() {
			@Override
			public boolean onScroll(final ScrollEvent arg0) {
				return false;
			}
			@Override
			public boolean onZoom(final ZoomEvent zoomLev) {
				zoom_level = zoomLev.getZoomLevel();
				return false;
			}
		});


//		geoRender = new GeoRender(this.getApplicationContext(),mapView);
		//Initial Map Setting before Location is found
		drawMarkers();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms
				final GeoPoint point = new GeoPoint(34.08145, -39.85007);
				mapView.getController().setZoom(4);
				mapView.getController().setCenter(point);
			}
		}, 100);



		grid = (ImageButton) findViewById(R.id.grid);
		grid.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Do something in response to button click
				Collect.getInstance().getActivityLogger().logAction(this, "OpenClassicView", "click");
				Intent i = new Intent(getApplicationContext(), GeoODKClassicActivity.class);
				startActivity(i);
			}
		});

		collect_button = (ImageButton) findViewById(R.id.collect_button);
		collect_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "FormChooserList", "click");
				Intent i = new Intent(getApplicationContext(),	FormChooserList.class);
				startActivity(i);
			}
		});

		gps_button = (ImageButton) findViewById(R.id.gps);
		gps_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				setGPSStatus();
			}
		});

		final GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
		imlp.setLocationUpdateMinDistance(1000);
		imlp.setLocationUpdateMinTime(60000);
		mMyLocationOverlay = new MyLocationNewOverlay(this, this.mapView);
		mMyLocationOverlay.runOnFirstFix(this.centerAroundFix);
		mapView.invalidate();
		setGPSStatus();
    }
	private final Runnable centerAroundFix = new Runnable() {
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					zoomToMyLocation();
				}
			});
		}
	};

	private void zoomToMyLocation(){
		if (this.mMyLocationOverlay.getMyLocation()!= null){
			if (this.zoom_level ==3){
				this.mapView.getController().setZoom(15);
			}else{
				this.mapView.getController().setZoom(this.zoom_level);
			}
			this.mapView.getController().setCenter(this.mMyLocationOverlay.getMyLocation());
			//mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
		}else{
			this.mapView.getController().setZoom(this.zoom_level);
		}

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

	private void upMyLocationOverlayLayers(){
		final LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
			this.overlayMyLocationLayers();
			//zoomToMyLocation();
		}else{
			this.showGPSDisabledAlertToUser();
		}

	}

	private void disableMyLocation(){
		final LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
			mMyLocationOverlay.setEnabled(false);
			mMyLocationOverlay.disableFollowLocation();
			mMyLocationOverlay.disableMyLocation();
			gpsStatus =false;
		}
	}

	private void overlayMyLocationLayers(){
		this.mapView.getOverlays().add(this.mMyLocationOverlay);
		this.mMyLocationOverlay.setEnabled(true);
		this.mMyLocationOverlay.enableMyLocation();
		this.mMyLocationOverlay.enableFollowLocation();
	}

	private void showGPSDisabledAlertToUser(){
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Enable GPS",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int id) {
								// Intent callGPSSettingIntent = new Intent(
								startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
								//startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}


	private String[] getAssetFormList() {
		AssetManager assetManager = getAssets();
		String[] formList = null;
		try {
			formList = assetManager.list("forms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assetManager.list(path);
		// TODO Auto-generated method stub
		return formList;
	}



	private void copyForms(String[] forms){
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		for (int i=0; forms.length>i; i++) {
			String filename = forms[i];
			File form_file = new File(FORMS_PATH,filename);
			if (!form_file.exists()){
				try {
					in = assetManager.open("forms/"+filename);
					out = new FileOutputStream(FORMS_PATH+File.separator+filename);
					copyFile(in, out);
					in.close();
		            out.flush();
		            out.close();
		            in = null;
		            out = null;
					
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + FORMS_PATH+File.separator+forms[i], e);
			}
				
			}
			 System.out.println(forms[i]);
		}
		
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException
	{
	      byte[] buffer = new byte[1024];
	      int read;
	      while((read = in.read(buffer)) != -1)
	      {
	            out.write(buffer, 0, read);
	      }
	}
	
	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "createErrorDialog", "show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "createErrorDialog",
									shouldExit ? "exitApplication" : "OK");
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	


	
	
}
