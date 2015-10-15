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
 * [2015/07/05] Created by:
 *
 * @author Marco Foi (foimarco@gmail.com)
 * 
 * Original template Activity by:
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.MBTileProvider;
import com.geoodk.collect.android.spatial.MapHelper;
import com.geoodk.collect.android.utilities.InfoLogger;
import com.geoodk.collect.android.widgets.GeoPointNewWidget;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class GeoPointMapNewActivity extends Activity implements IRegisterReceiver {
	private MapView mapView;
	private ITileSource baseTiles;
	public DefaultResourceProxyImpl resource_proxy;
	public int zoom_level = 3;
	public static final int stroke_width = 5;
	public String final_return_string;
	private MapEventsOverlay overlayEventos;
	private ImageButton clear_button;
	private ImageButton save_button;
	private SharedPreferences sharedPreferences;
	public Boolean layerStatus = false;
	private int selected_layer= -1;
	private ProgressDialog progress;

	private MBTileProvider mbprovider;
	private TilesOverlay mbTileOverlay;
	public Boolean gpsStatus = true;
	private ImageButton gps_button;
	private String[] offilineOverlays;
	public MyLocationNewOverlay mMyLocationOverlay;
	public Boolean data_loaded = false;

    public final static String LOCATION_MARKER_LAT = "LOCATION_MARKER_LAT";
    public final static String LOCATION_MARKER_LON = "LOCATION_MARKER_LON";
	public final static String CURRENT_MODE = "CURRENT_MODE";
	public final static String MODE_MANUAL = "MANUAL";
	public final static String MODE_AUTO = "AUTO";
	public String currentMode = MODE_AUTO;
	public double targetAccuracy = GeoPointNewWidget.UNSET_LOCATION_ACCURACY;
	public Marker locationMarker;
    public TextView textViewTargetAccuracy;
    private Drawable locationMarkerIcon;
    public Drawable markerRed;
    public Drawable markerGreen;

    @Override
	protected void onResume() {
		super.onResume();
		Boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
		String basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "Default");
		baseTiles = MapHelper.getTileSource(basemap);
		mapView.setTileSource(baseTiles);
		mapView.setUseDataConnection(online);
		setGPSStatus();
	}


	@Override
	public void onBackPressed() {
		saveGeoPoint();
	}

	@Override
	protected void onPause() {
		super.onPause();
		disableMyLocation();
	}
	@Override
	protected void onStop() {
		super.onStop();
		disableMyLocation();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting Content & initiating the main button id for the activity

        setContentView(R.layout.geopointmapnew_layout);
        setTitle(getString(R.string.geopoint_title)); // Setting title of the action
        save_button = (ImageButton) findViewById(R.id.geopoint_button);
        clear_button = (ImageButton) findViewById(R.id.clear_button);
        // The text view on top of map for displaying current GPS accuracy and Target Accuracy from XForm accuracyThreshold
        textViewTargetAccuracy = ((TextView) findViewById(R.id.textViewTargetAccuracy));

        //Defining the System prefereces from the mapSetting

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        String baeMap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
        baseTiles = MapHelper.getTileSource(baeMap);
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());

        mapView = (MapView) findViewById(R.id.geopoint_mapview);
        mapView.setTileSource(baseTiles);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setUseDataConnection(online);
        mapView.getController().setZoom(zoom_level);
        mapView.setMapListener(mapViewListner);

        // Set default icon for locationMarker
        markerRed = getResources().getDrawable(R.drawable.map_marker_red);
        markerGreen = getResources().getDrawable(R.drawable.map_marker);
        locationMarkerIcon = markerGreen;

        // Use eventual data from saved state to restore position of marker
        if (savedInstanceState != null) {
            double lat = savedInstanceState.getDouble(LOCATION_MARKER_LAT);
            double lon = savedInstanceState.getDouble(LOCATION_MARKER_LON);
            currentMode = savedInstanceState.getString(CURRENT_MODE);
            targetAccuracy = savedInstanceState.getDouble(GeoPointNewWidget.ACCURACY_THRESHOLD);
            refreshClearButtonVisibility();
            GeoPoint point = new GeoPoint(lat, lon);
            repositionLocationMarkerAt(point);
        }

        // Register a listener for catching touch events and forwarding them to receiver for handling
        setupOverlayPointListner();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });

        ImageButton layers_button = (ImageButton) findViewById(R.id.geopoint_layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayersDialog();
            }
        });

        gps_button = (ImageButton) findViewById(R.id.geopoint_gps_button);
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setGPSStatus();
            }
        });

        // Use a custom GpsLocationProvider for controlling dispatched LocationChange events basing on their location accuracy
        // Controlling accracy is from XForm accuracyThreshold or, if null, from default as specified in GeoPointNewWidget.UNSET_LOCATION_ACCURACY
        CustomGpsMyLocationProvider gpsLocationProvider = new CustomGpsMyLocationProvider(this);

        mMyLocationOverlay = new MyLocationNewOverlay(this, gpsLocationProvider, mapView);


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            //Retrieve target accuracy from calling intent (GeopointNewWidget)ACCURACY_THRESHOLD
            if ( intent.hasExtra(GeoPointNewWidget.ACCURACY_THRESHOLD) ) {
                targetAccuracy = intent.getDoubleExtra(GeoPointNewWidget.ACCURACY_THRESHOLD, GeoPointNewWidget.UNSET_LOCATION_ACCURACY);
            }
			if ( intent.hasExtra(GeoPointNewWidget.POINT_LOCATION) ) {
				data_loaded = true;
				String s = intent.getStringExtra(GeoPointNewWidget.POINT_LOCATION);
				overlayIntentPoint(s);
				zoomToCentroid();
			}
		}else{
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					//Do something after 100ms
					GeoPoint point = new GeoPoint(34.08145, -39.85007);
					mapView.getController().setZoom(3);
					mapView.getController().setCenter(point);
				}
			}, 100);
		}

        //Sets executing a runnable for zooming to location and dismissing progress dialog on first fix.
        mMyLocationOverlay.runOnFirstFix(centerAroundFixAndDisplayLocMarker);

        progress = new ProgressDialog(this);
        // Progress dialog is shown just if user specified accuracyThreshold in their XForm
        // and data is not being reviewed from previuos survey
        progress.setTitle(getString(R.string.getting_location));
        progress.setMessage(getString(R.string.please_wait_long));
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //play_button.setImageResource(R.drawable.ic_menu_mylocation);
            }
        });
        if (targetAccuracy != GeoPointNewWidget.UNSET_LOCATION_ACCURACY && data_loaded == false) {
            progress.setTitle(getString(R.string.getting_location));
            progress.setMessage(buildProgressMessage(mMyLocationOverlay, targetAccuracy));
        }
        progress.show();
        this.setGPSStatus();



        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                // If no accuracyThreshold is set, fire plain clearing point logic
//                if (targetAccuracy == GeoPointNewWidget.UNSET_LOCATION_ACCURACY ) {
//                    currentMode = MODE_AUTO;
//                    resetLocationPointAtCurrentPosition();
//                    refreshClearButtonVisibility();
//                } else {
//                    showClearDialog();
//                }
            }
        });


        // Set TextView for informin about current accuracy
        this.updateTextViewTargetAccuracy((CustomGpsMyLocationProvider)mMyLocationOverlay.getMyLocationProvider());

        mapView.invalidate();
	}
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current locationMarker if any
        if (locationMarker != null){
            double lat = locationMarker.getPosition().getLatitude();
            double lon = locationMarker.getPosition().getLongitude();
            savedInstanceState.putDouble(LOCATION_MARKER_LAT, lat);
            savedInstanceState.putDouble(LOCATION_MARKER_LON, lon);
            savedInstanceState.putCharSequence(CURRENT_MODE, currentMode);
            savedInstanceState.putDouble(GeoPointNewWidget.ACCURACY_THRESHOLD, targetAccuracy);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * Set the drawable for che locationMarker
     * @param icon
     */
    private void setCurrentMarkerIcon(Drawable icon) {
        locationMarkerIcon = icon;
    }

    /**
     * Update text view "textViewTargetAccuracy" with current GPS Accuracy
     * @param customGpsLocationProvider
     */
    protected void updateTextViewTargetAccuracy(CustomGpsMyLocationProvider customGpsLocationProvider) {
        CustomGpsMyLocationProvider cGPSLP = ((CustomGpsMyLocationProvider) customGpsLocationProvider);
        if(targetAccuracy == GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
            textViewTargetAccuracy.setTextColor(Color.rgb(54,120,0));
            textViewTargetAccuracy.setText(
                    "Current GPS Accuracy: " + cGPSLP.getCurrentAccuracyAsIntString() + " m");

        } else { // User asked for a specific accuracyThreshold
            if(cGPSLP.getCurrentAccuracy() <= targetAccuracy) {
                // Accuracy is good: use green
                textViewTargetAccuracy.setTextColor(Color.rgb(54, 120, 0));
            } else {
                // Accuracy is bad: use red
                textViewTargetAccuracy.setTextColor(Color.rgb(230, 0, 0));
            }
            textViewTargetAccuracy.setText(
                    "Target GPS accuracy: " + Double.toString(targetAccuracy) + " m" +
                    "\nCurrent GPS accuracy: " + cGPSLP.getCurrentAccuracyAsIntString() + " m");
        }
    }

    /**
     * Create the message to keep updated the progress dialgo while ser is  waiting for icreasing GPS accuracy
     * @param mMyLocationOverlay
     * @param targetAccuracy
     * @return String message
     */
    private String buildProgressMessage(MyLocationNewOverlay mMyLocationOverlay, double targetAccuracy) {

        String message = "";
        String accuracyString = ((CustomGpsMyLocationProvider) mMyLocationOverlay.getMyLocationProvider()).getCurrentAccuracyAsIntString();
        if (targetAccuracy != GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
            message = "Target Accuracy: " + Double.toString(targetAccuracy) + " m\n" +
                    "Current Accuracy: " + accuracyString + " m\n" +
                    //getString(R.string.please_wait_long);
                    "\n >> Wait for automatic capture.\n >> Tap ouside dialog for manual.";
        } else {
            message = "Current Accuracy: " + accuracyString + " m";
        }
        return message;
    }

    /**
     * Pareses a GeoPoint string representation back to a GeoPoint then to a Marker for finally adding the latter to MapView
     * @param geoPointAsString
     */
    private void overlayIntentPoint(String geoPointAsString) {
        String[] sp = geoPointAsString.split(" ");
        double gp[] = new double[4];
        String lat = sp[0].replace(" ", "");
        String lng = sp[1].replace(" ", "");
        gp[0] = Double.parseDouble(lat);
        gp[1] = Double.parseDouble(lng);
        GeoPoint point = new GeoPoint(gp[0], gp[1]);
        currentMode = MODE_MANUAL;
        repositionLocationMarkerAt(point);
        refreshClearButtonVisibility();
     }

    /**
     * Since the point of the widget is to collect location, we should always have the GPS one
     *
     *
     */
	private void setGPSStatus(){
//        if(gpsStatus == false){
//            gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
//            upMyLocationOverlayLayers();
//            gpsStatus = true;
//        }else{
//            gps_button.setImageResource(R.drawable.ic_menu_mylocation);
//            disableMyLocation();
//            gpsStatus = false;
//        }
        upMyLocationOverlayLayers();
        gpsStatus = true;
         
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * This runnable performs zooming to location and is run just on first fix hence
     * checks if locationMarker already exists from savedInstanceState
     */
    private Runnable centerAroundFixAndDisplayLocMarker = new Runnable() {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
					zoomToMyLocation();
                    if (locationMarker == null) {
                        currentMode = MODE_AUTO;
                        refreshClearButtonVisibility();
                        resetLocationPointAtCurrentPosition();
                    } else {
                        //LocationMarker is already on map from SavedInstanceState or from loading previous data so:
						refreshClearButtonVisibility();
                    }
                }
            });
        }
    };


	/**
	 * Put the LocationMarker on map at provided GeoPoint:
	 * This marker will hold the location that is used to define the GeoPoint.
     * If a null point is provided, just last LocationMareker is delted but no new is created.
     * @param "GeoPoint" point
	 */
	private void repositionLocationMarkerAt(GeoPoint point){
        mapView.getOverlays().remove(locationMarker);
        if (point != null) {
            locationMarker = new Marker(mapView);
            locationMarker.setPosition(point);
            locationMarker.setDraggable(true);
            locationMarker.setIcon(locationMarkerIcon);
            locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            locationMarker.setOnMarkerDragListener(draglistner);
            locationMarker.setOnMarkerClickListener(nullmarkerlistner);
            mapView.getOverlays().add(locationMarker);
        } else {
            Toast.makeText(this, "No GPS position to use for new Marker!",Toast.LENGTH_LONG).show();
        }
		mapView.invalidate();
	}

    /**
     * Create or reposition to current location the Marker used to store the coordinates that
     * are returned, at Activity exit, to the calling Widget as a GeoPoint.
     * This method also takes care of
     * - invalidating the map
     * - hiding the clear_button (if vsible)
     */
    private void resetLocationPointAtCurrentPosition() {
        // Attempt to reset to current location
        GeoPoint point = mMyLocationOverlay.getMyLocation();
        if (point == null) {
            // Since no current location exist, attempt with last known location
            Location location = mMyLocationOverlay.getLastFix();
            if (location != null) {
                point = new GeoPoint(location.getLatitude(), location.getLongitude());
            } else {
                // Since no current nor last-know location exist, default to nowhere!
                point = null;
            }
        }
        repositionLocationMarkerAt(point);
        clear_button.setVisibility(View.GONE);
        mapView.invalidate();
    }


    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        .setCancelable(false)
        .setPositiveButton("Enable GPS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Intent callGPSSettingIntent = new Intent(
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        //startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * If GPS Hardware is enabled enables MyLocationOverlay (that also starts map-pan for following location)
     * Else show GPS-is-disabled Alert
     */
    private void upMyLocationOverlayLayers(){
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
    		overlayMyLocationLayers();
//            progress.dismiss();
    		//zoomToMyLocation();
    	} else {
    		showGPSDisabledAlertToUser();
    	}
    }

    /**
     * Enables MyLocationOverlay, starts "follow location"
     * and adds MyLocationOverlay to MapView overlays.
     */
    private void overlayMyLocationLayers(){
        mapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.setEnabled(true);
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
    }

    /**
     * Zooms and centers to current location (retrieved from MyLocationOverlay)
     */
    private void zoomToMyLocation(){
    	if (mMyLocationOverlay.getMyLocation()!= null){
    		if (zoom_level == 3){
    			mapView.getController().setZoom(15);
    		}else{
    			mapView.getController().setZoom(zoom_level);
    		}
    		mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
            progress.dismiss();
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
        	gpsStatus = false;
    	}
    }


	private void saveGeoPoint(){
		//Toast.makeText(this, "Do Save Stuff", Toast.LENGTH_LONG).show();
		returnLocation();
		finish();
	}

    /**
     * Create a MapEventOverlay and add it to MapView,
     * defining a receiver of the caught events
     */
	private void setupOverlayPointListner(){
		overlayEventos = new MapEventsOverlay(getBaseContext(), mReceiver);
		mapView.getOverlays().add(overlayEventos);
		mapView.invalidate();
	}


    /**
     * Clear High-accuracy point.
     * The method is used just when accuracyThreshold is set in user XForm.
     */
	private void showClearDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("High-Accuracy GPS point already created. Would you like to CLEAR the feature?")
               .setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       currentMode = MODE_AUTO;
                       data_loaded = false; //The point will be not from previous survey any more
                       resetLocationPointAtCurrentPosition();
                       refreshClearButtonVisibility();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               }).show();
	}


    /**
     * Using the locationMarker as source of data, this method builds and retrunrs a string in the form of
     * "45.932 9.245 0.0 0.0" - without any other trailin or leading char.
     * Returns null if no locationMarcker exists or if user asked for specific accuracyThreshold but this not met by current GPS fix
     * @return String
     */
	private String generateReturnString() {
        if (locationMarker != null) {
            // GPS has already made a fix or a Manual point was set
            String temp_string = "";
            // User did not ask for a specific accuracyThreshold: return any data from locationMarker
            if(targetAccuracy == GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
                String lat = Double.toString(locationMarker.getPosition().getLatitude());
                String lng = Double.toString(locationMarker.getPosition().getLongitude());
                String alt = "0.0";
                String acu = "0.0";
                temp_string = temp_string + lat + " " + lng + " " + alt + " " + acu;
            } else
            // User asked for a specific accuracyThreshold
            {
                if (data_loaded == true) {
                    // The user is displaying a point loaded from previous capture:
                    // since accuracyThrshold is requested, the displayd point is a good point..
                    // ..so do not care about current accuracy and just return the same point
                    String lat = Double.toString(locationMarker.getPosition().getLatitude());
                    String lng = Double.toString(locationMarker.getPosition().getLongitude());
                    String alt = "0.0";
                    String acu = "0.0";
                    temp_string = temp_string + lat + " " + lng + " " + alt + " " + acu;
                } else {
                    // The displayed point is surely from GPS but since accuracyThreshold is required, we must chech before returning a point
                    double currentAccuracy = ((CustomGpsMyLocationProvider) mMyLocationOverlay.getMyLocationProvider()).getCurrentAccuracy();
                    // Accuracy meet requirements
                    if (currentAccuracy <= targetAccuracy) {
                        // Return data from GPS placed marker
                        String lat = Double.toString(locationMarker.getPosition().getLatitude());
                        String lng = Double.toString(locationMarker.getPosition().getLongitude());
                        String alt = "0.0";
                        String acu = "0.0";
                        temp_string = temp_string + lat + " " + lng + " " + alt + " " + acu;
                    } else {
                        Toast.makeText(this, "Insufficient GPS accuracy.\nNothing returned.", Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }
            return temp_string;
        } else {
            // GPS has NOT already made a fix nor any manual point was set on Map
            Toast.makeText(this, "No GPS location to return.", Toast.LENGTH_SHORT).show();
            return null;
        }
	}

    /**
     * Finishes the current Activity by returning the acquired GeoPoint in the form of a string,
     * as created by <@link>generateReturnString()</@link> method.
     */
    private void returnLocation(){
    		final_return_string = generateReturnString();
            Intent i = new Intent();
            i.putExtra(
                FormEntryActivity.LOCATION_RESULT,
                final_return_string);
            setResult(RESULT_OK, i);
        finish();
    }

    /**
     * This event-receiver handles long-presses on Map and fires placing of LocationMarker
     * as well as setting of marker mode placement to MANUAL and clear_button visibility refresh.
     * Also, if targetAccuracy is not set, this method sets data_loaded to false, so pointing
     * that the if any pre-existing point on map was loaded from former survey, that oint is now
     * replaced by a manually placed one.
     */
    private MapEventsReceiver mReceiver = new MapEventsReceiver() {
        @Override
        public boolean longPressHelper(GeoPoint point) {
            if (targetAccuracy == GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
                currentMode = MODE_MANUAL;
                data_loaded = false;
                refreshClearButtonVisibility();
                repositionLocationMarkerAt(point);
                return false;
            }
            //
            else {
                Toast.makeText(GeoPointMapNewActivity.this,
                        "Manual points cannot be created if accuracyThreshold is set!", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        @Override
        public boolean singleTapConfirmedHelper(GeoPoint arg0) {
            return false;
        }
    };

    /**
     * This listener retrieves zoom levelew and sotres it in zom_level field
     */
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

    public GeoPoint locationMarkerBeforeDragPoint;
    /**
     * This Listener handles map marker dragging event, setting marker placement to MANUAL
     * and refresching clear_button visibility.
     */
	private OnMarkerDragListener draglistner = new OnMarkerDragListener() {
		@Override
		public void onMarkerDragStart(Marker marker) {
            //Store orginal position of marker for restoring if manual mode is forbidden due to accuracyThreshold
            locationMarkerBeforeDragPoint = locationMarker.getPosition();
        }
		@Override
		public void onMarkerDragEnd(Marker marker) {
            if (targetAccuracy == GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
                // No accuracyThreshold : switch to manual mode
                currentMode = MODE_MANUAL;
                // Set that the displayed point is surely not loaded from former survey, in case it was.
                // This is needed to handle the case when points with high accuracy are justte re-displayed for review,
                // and so should not be deleted.
                data_loaded = false;
                refreshClearButtonVisibility();
            } else {
                // accuracyThreshold is set : restore locatinMarcker at original position
                GeoPointMapNewActivity.this.repositionLocationMarkerAt(locationMarkerBeforeDragPoint);
                // Restore marker green color as it is restored at original location
                locationMarker.setImage(markerGreen);
                mapView.invalidate();
                Toast.makeText(GeoPointMapNewActivity.this, "Manual points cannot be created if accuracyThreshold is set!", Toast.LENGTH_LONG).show();
            }
		}
		@Override
		public void onMarkerDrag(Marker marker) {
            if (targetAccuracy != GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
                // If accuracyThreshold is set, make marker red as soon as is dragged
                marker.setImage(markerRed);
                mapView.invalidate();
            }
        }
	};

	/**
	 * Refresh visibility of clear_button depending on current point placement mode:
	 *  AUTO = from GPS
	 *  MANUAL = from map-LongPress or Marker Drag
	 */
	private void refreshClearButtonVisibility() {
		if (currentMode == MODE_AUTO) {
			clear_button.setVisibility(View.GONE);
            save_button.setVisibility(View.VISIBLE);
		} else if ( currentMode == MODE_MANUAL) {
			clear_button.setVisibility(View.VISIBLE);
            if (data_loaded == true) {
                save_button.setVisibility(View.GONE);
            } else {
                save_button.setVisibility(View.VISIBLE);
            }
		}
	}

	private void showLayersDialog() {
		//FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
		//View view=fl.inflate(self, R.layout.showlayers_layout, null);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Select Offline Layer");
		offilineOverlays = getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
		//alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
		alertDialog.setSingleChoiceItems(offilineOverlays,selected_layer,new  DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int item) {
		            switch(item){
		            case 0 :
		            	mapView.getOverlays().remove(mbTileOverlay);
		            	layerStatus = false;
                        // Reset max zoom level to max level of baseMap tile layer
                        int baseMapMaxZoomLevel = baseTiles.getMaximumZoomLevel();
                        mapView.setMaxZoomLevel(baseMapMaxZoomLevel);
		            	break;
		            default:
                        layerStatus = true;
                        mapView.getOverlays().remove(mbTileOverlay);
                        String mbFilePath = getMBTileFromItem(item);
                        File mbFile = new File(mbFilePath);
                        mbprovider = new MBTileProvider(GeoPointMapNewActivity.this, mbFile);
                        int newMaxZoomLevel = mbprovider.getMaximumZoomLevel();
                        mbTileOverlay = new TilesOverlay(mbprovider,GeoPointMapNewActivity.this);
                        mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                        mapView.getOverlays().add(mbTileOverlay);
                        updateMapOverLayOrder();
                        mapView.setMaxZoomLevel(newMaxZoomLevel);
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
		if (layerStatus){
			mapView.getOverlays().remove(mbTileOverlay);
			mapView.getOverlays().add(mbTileOverlay);
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

    private String[] getOfflineLayerList() {
        File files = new File(Collect.OFFLINE_LAYERS);
        ArrayList<String> results = new ArrayList<>();
        results.add("None");
        for(String folder : files.list()){
            results.add(folder);
        }
        String[] finala = new String[results.size()];
        finala = results.toArray(finala);
        return finala;
    }

	private String getMBTileFromItem(int item) {
		String foldername = offilineOverlays[item];
		File dir = new File(Collect.OFFLINE_LAYERS+File.separator+foldername);
		String mbtilePath;
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".mbtiles");
		    }
		});
		mbtilePath =Collect.OFFLINE_LAYERS+File.separator+foldername+File.separator+files[0].getName();

		return mbtilePath;
	}

    private OnMarkerClickListener nullmarkerlistner= new OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker arg0, MapView arg1) {
            return false;
        }
    };

    private void zoomToCentroid(){

        //Calculate Centroid of between current location and current LocationMarker

        //----- This should be hard coded but based on the extent of the points
        mapView.getController().setZoom(15);
        //-----

        mapView.invalidate();
        Handler handler=new Handler();
        Runnable r = new Runnable(){
            public void run() {
                double x_value = 0.0;
                double y_value = 0.0;
                GeoPoint lmPoint = locationMarker.getPosition();
                double x_marker = lmPoint.getLatitude();
                double y_marker = lmPoint.getLongitude();
                x_value += x_marker;
                y_value += y_marker;
                GeoPoint currentLocationPoint = mMyLocationOverlay.getMyLocation();
                if (currentLocationPoint != null) {
                    double x_marker2 = currentLocationPoint.getLatitude();
                    double y_marker2 = currentLocationPoint.getLongitude();
                    x_value += x_marker2;
                    y_value += y_marker2;
                    x_value = x_value / 2;
                    y_value = y_value / 2;
                }
                GeoPoint centroid = new GeoPoint(x_value,y_value);
                mapView.getController().setCenter(centroid);
            }
        };
        handler.post(r);
        mapView.invalidate();

    }



    /**
     * Build a Custom GpsMyLocationProvider to catch location changes that do not satisfy
     * required positioning accuracy
     */
    class CustomGpsMyLocationProvider extends GpsMyLocationProvider {

        Context context;
        double currentAccuracy = 500.0;

        public CustomGpsMyLocationProvider(android.content.Context context) {
            super(context);
            this.context = context;
        }
        /**
         * Return accuracy of last location change
         * @return double Accuracy
         */
        public double getCurrentAccuracy() {
            return currentAccuracy;
        }

        /**
         * Returns current accuracy, usually a very long double like 39.7748394399,
         * as something like "39.0"
         * @return
         */
        public String getCurrentAccuracyAsIntString() {
            int accInt = (int) (this.getCurrentAccuracy() + 0.5);
            String accuracyString = Integer.toString(accInt) + ".0";
            return accuracyString;
        }

        @Override
        public void onLocationChanged(Location location) {
            //GeoPointNewWidget.UNSET_LOCATION_ACCURACY
            if (location != null) {

                // Update accuracy
                currentAccuracy = location.getAccuracy();

                // User requested a specific accuracyThreshold in his XForm
                if (targetAccuracy != GeoPointNewWidget.UNSET_LOCATION_ACCURACY) {
                    // Catch location changes that have not user desired accuracy
                    if (location.getAccuracy() <= targetAccuracy) {
                        // Use GREEN marker: current accuracy is equal/lower than minimum expected
                        locationMarkerIcon = markerGreen;
                        setCurrentMarkerIcon(locationMarkerIcon);
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        InfoLogger.geolog("GeoPointMapNewActivity: " + System.currentTimeMillis() + " onLocationChanged" + " acc: " + location.getAccuracy());
                    } else {
                        // Use RED marker
                        locationMarkerIcon = markerRed;
                        setCurrentMarkerIcon(locationMarkerIcon);
                        // If location accuracy is not enough eventually udate progress dialog, log event
                        if (progress.isShowing()) {
                            String message = ((GeoPointMapNewActivity) this.context).buildProgressMessage(((GeoPointMapNewActivity) this.context).mMyLocationOverlay, targetAccuracy);
                            progress.setMessage(message);
                        }
                        InfoLogger.geolog("GeoPointMapNewActivity: " + System.currentTimeMillis() + " onLocationChanged" + " acc: " + location.getAccuracy());
                    }

                    // In case of Automatic mode locationMarker placement
                    if (((GeoPointMapNewActivity) this.context).currentMode == GeoPointMapNewActivity.MODE_AUTO)
                        ((GeoPointMapNewActivity) this.context).repositionLocationMarkerAt(new GeoPoint(location.getLatitude(), location.getLongitude()));

                }
                // User did not request for a specific accuracyThreshold in his XForm
                else {
                    // Use GREEN marker
                    locationMarkerIcon = markerGreen;
                    setCurrentMarkerIcon(locationMarkerIcon);
                    InfoLogger.geolog("GeoPointMapNewActivity: " + System.currentTimeMillis() + " onLocationChanged" + " acc: " + location.getAccuracy());
                }

                // Update text view, placed at top of Map, for displaying current GPS accuracy
                ((GeoPointMapNewActivity) context).updateTextViewTargetAccuracy(this);

                // Whatever accuracy is, forward event: this will trgger also
                // mMyLocationOverlay.runOnFirstFix(centerAroundFixAndDisplayLocMarker);
                super.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            super.onStatusChanged(provider, status, extras);
        }
    }


}