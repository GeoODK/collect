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
 * Responsible for being the main mapping activity, GPS, offline mapping, and data point display
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package com.geoodk.collect.android.activities;

/*
 * 06.30.2014
 * Jon Nordling
 * Mathias Karner
 *
 * This activity is to map the data offline
 *
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.spatial.CustomMarkerHelper;
import com.geoodk.collect.android.spatial.GeoRender;
import com.geoodk.collect.android.spatial.MBTileProvider;
import com.geoodk.collect.android.spatial.MapHelper;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.james.mime4j.util.StringArrayMap;

public class GeoODKMainMapActivity extends Activity implements IRegisterReceiver{
    private MapView mapView;
    private MapController myMapController;
    //private ItemizedIconOverlay<OverlayItem> complete_overlays;
    //private ItemizedIconOverlay<OverlayItem> final_overlays;
    //private ItemizedIconOverlay<OverlayItem> defalt_overlays;
    private DefaultResourceProxyImpl resource_proxy;
    private final Context self = this;
    private Marker loc_marker;  //This is the marker used to display the user's location
    private final Criteria criteria = new Criteria(); // ?? Not sure what a criteria is but probably should find out!
    private String provider; //  Gps or Network providor
    //public XmlGeopointHelper geoheler = new XmlGeopointHelper();

    public Location lastLocation;
    private static final String t = "Map";
    //ArrayList<OverlayItem> marker_list = new ArrayList<OverlayItem>();
    private final List<String[]> markerListArray = new ArrayList<String[]>();
    private LocationManager locationManager;
    public MyLocationNewOverlay mMyLocationOverlay;
    public SharedPreferences sharedPreferences;
    private Boolean online;
    private String basemap;

    //This section is used to know the order of a array of instance data in the db cursor
    public static final int pos_url=0;
    public static final int pos_id=1;
    public static final int pos_name=2;
    public static final int pos_status=3;
    public static final int pos_uri=4;
    public static final int pos_geoField=5;
    public int zoom_level =-1;
    public boolean zoom_been_changed = false;

    //This is used to store temp latitude values
    private Double lat_temp;
    private Double lng_temp;

    //Keep Track if GPS button is on or off
    //MyLocationOverlay myLocationOverlay = null;

    public Boolean gpsStatus = true;
    public Boolean layerStatus = false;
    private int selected_layer= -1;

    private MBTileProvider mbprovider;
    private TilesOverlay mbTileOverlay;

    private String[] OffilineOverlays;
    private ITileSource baseTiles;
    private ImageButton gps_button;
    private ImageButton layers_button;
    private ImageButton map_setting_button;
    private ImageButton collect_data;
    private ImageButton edit_data;
    private ImageButton settings_data;
    private GeoRender geoRender;

    XmlPullParserFactory factory;

    private AlertDialog mAlertDialog;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

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

    private final OnMarkerDragListener draglistner = new OnMarkerDragListener(){

        @Override
        public void onMarkerDrag(final Marker m) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMarkerDragEnd(final Marker m) {
            final GeoPoint newlocation = m.getPosition();
            newlocation.getLatitude();
            newlocation.getLongitude();
            GeoODKMainMapActivity.this.loc_marker = new Marker(GeoODKMainMapActivity.this.mapView);
            final String lat = Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
            final String lng = Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
            //Toast.makeText(OSM_Map.this,lat+" "+lng, Toast.LENGTH_LONG).show();
            askToChangePoint(m);
            // TODO Auto-generated method stub
            //Toast.makeText(OSM_Map.this,((CustomMarkerHelper)m).getMarker_url(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onMarkerDragStart(final Marker m) {
            // TODO Auto-generated method stub
            //lat_temp =  Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
            //lng_temp  =  Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
            lat_temp =  ((CustomMarkerHelper)m).getPosition().getLatitude();
            lng_temp  =  ((CustomMarkerHelper)m).getPosition().getLongitude();
            //Toast.makeText(OSM_Map.this,lat+" "+lng, Toast.LENGTH_LONG).show();

        }

    };

    protected void askToChangePoint(final Marker m) {
        final Marker mk = m;
        //final Double lat = ((CustomMarkerHelper)m).getPosition().getLatitude();
        //final Double lng = ((CustomMarkerHelper)m).getPosition().getLongitude();
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //loadPublicLegends(mainActivity);

                    try {
                        changeInstanceLocation(mk);
                    } catch (final XmlPullParserException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final ParserConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final SAXException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final TransformerException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Cancel button clicked
                    ((CustomMarkerHelper)mk).setPosition(new GeoPoint(lat_temp, lng_temp));
                    mapView.invalidate();
                    break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(GeoODKMainMapActivity.this);
        builder.setMessage(
                "Are you sure you want to change the location of this point?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public void changeInstanceLocation(final Marker mk) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException, TransformerException{
        String url = ((CustomMarkerHelper) mk).getMarker_url();
        File xmlFile = new File(url);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(xmlFile);
        Node file_value = doc.getElementsByTagName(((CustomMarkerHelper) mk).getMarker_geoField()).item(0).getFirstChild();
        String newLocation = Double.toString((((CustomMarkerHelper)mk).getPosition().getLatitude()))+ " "+ Double.toString((((CustomMarkerHelper)mk).getPosition().getLongitude()))+ " 0.1 0.1"; 
        file_value.setNodeValue(newLocation);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult results = new StreamResult(xmlFile.getPath());
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        transformer.transform(source, results);
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
    private void drawMarkers(){
        geoRender = new GeoRender(this.getApplicationContext(),mapView);
    }


    private String getMBTileFromItem(final int item) {
        final String folderName = OffilineOverlays[item];
        final File dir = new File(Collect.OFFLINE_LAYERS+File.separator+folderName);

        if (dir.isFile()) {
            // we already have a file
            return dir.getAbsolutePath();
        }

        // search first mbtiles file in the directory
        String mbtilePath;
        final File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".mbtiles");
            }
        });

        if (files.length == 0) {
            throw new RuntimeException(Collect.getInstance().getString(R.string.mbtiles_not_found, dir.getAbsolutePath()));
        }
        mbtilePath = files[0].getAbsolutePath();

        return mbtilePath;
    }


    public void hideInfoWindows(){
        final List<Overlay> overlays = mapView.getOverlays();
        for (final Overlay overlay : overlays) {
            if (overlay.getClass() == CustomMarkerHelper.class){
                ((CustomMarkerHelper)overlay).hideInfoWindow();
            }
        }

    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.osmmap_layout); //Setting Content to layout xml
        setTitle(this.getString(R.string.app_name) + " > Mapping"); // Setting title of the action
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

        map_setting_button = (ImageButton) findViewById(R.id.map_setting_button);
        map_setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                final Intent i = new Intent(self, MapSettings.class);
                startActivity(i);

            }
        });

        gps_button = (ImageButton)this.findViewById(R.id.gps_button);
        //This is the gps button and its functionality
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                GeoODKMainMapActivity.this.setGPSStatus();
            }
        });

        layers_button = (ImageButton)this.findViewById(R.id.layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                showLayersDialog();

            }
        });

        final GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        mMyLocationOverlay = new MyLocationNewOverlay(this, this.mapView);
        mMyLocationOverlay.runOnFirstFix(this.centerAroundFix);
        setGPSStatus();

        //Initial Map Setting before Location is found
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                final GeoPoint  point = new GeoPoint(34.08145, -39.85007);
                mapView.getController().setZoom(3);
                mapView.getController().setCenter(point);
            }
        }, 100);

        mapView.invalidate();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        this.disableMyLocation();
        clearMapMarkers();
    }
    
    private void clearMapMarkers() {
        mapView.getOverlays().clear();
        markerListArray.clear();
    }


    @Override
    protected void onResume() {
        //Initializing all the
        super.onResume(); // Find out what this does? bar
        online = this.sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        basemap = this.sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
        hideInfoWindows();
        baseTiles = MapHelper.getTileSource(basemap);
        mapView.setTileSource(this.baseTiles);
        mapView.setUseDataConnection(this.online);
        drawMarkers();
        setGPSStatus();

        mapView.invalidate();
    }

    //This function comes after the onCreate function
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //myMapController.setZoom(4);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        this.disableMyLocation();
    }

    private void overlayMyLocationLayers(){
        this.mapView.getOverlays().add(this.mMyLocationOverlay);
        this.mMyLocationOverlay.setEnabled(true);
        this.mMyLocationOverlay.enableMyLocation();
        this.mMyLocationOverlay.enableFollowLocation();
    }

    private void setGPSStatus(){
        if(this.gpsStatus ==false){
            this.gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
            this.upMyLocationOverlayLayers();
            //enableMyLocation();
            //zoomToMyLocation();
            this.gpsStatus = true;
        }else{
            this.gps_button.setImageResource(R.drawable.ic_menu_mylocation);
            this.disableMyLocation();
            this.gpsStatus = false;
        }
    }
    private void showGPSDisabledAlertToUser(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        .setCancelable(false)
        .setPositiveButton("Enable GPS",
                new DialogInterface.OnClickListener(){
            @Override
            public void onClick(final DialogInterface dialog, final int id){
                // Intent callGPSSettingIntent = new Intent(
                GeoODKMainMapActivity.this.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                //startActivity(callGPSSettingIntent);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
            @Override
            public void onClick(final DialogInterface dialog, final int id){
                dialog.cancel();
            }
        });
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }



    //This is going to be the listner for the devices locations

    private void showLayersDialog() {
        // TODO Auto-generated method stub
        //FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
        //View view=fl.inflate(self, R.layout.showlayers_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GeoODKMainMapActivity.this);
        alertDialog.setTitle("Select Offline Layer");
        //alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
        alertDialog.setSingleChoiceItems(MapHelper.getOfflineLayerList(), this.selected_layer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {

                try {
                    switch (item) {
                        case 0:
                            GeoODKMainMapActivity.this.mapView.getOverlays().remove(GeoODKMainMapActivity.this.mbTileOverlay);
                            GeoODKMainMapActivity.this.layerStatus = false;
                            break;
                        default:
                            GeoODKMainMapActivity.this.mapView.getOverlays().remove(GeoODKMainMapActivity.this.mbTileOverlay);
                            //String mbTileLocation = getMBTileFromItem(item);

                            final String mbFilePath = getMBTileFromItem(item);
                            //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
                            final File mbFile = new File(mbFilePath);
                            GeoODKMainMapActivity.this.mbprovider = new MBTileProvider(GeoODKMainMapActivity.this, mbFile);
                            GeoODKMainMapActivity.this.mbTileOverlay = new TilesOverlay(GeoODKMainMapActivity.this.mbprovider, GeoODKMainMapActivity.this);
                            GeoODKMainMapActivity.this.mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                            GeoODKMainMapActivity.this.mapView.getOverlays().add(GeoODKMainMapActivity.this.mbTileOverlay);
                            GeoODKMainMapActivity.this.drawMarkers();
                            GeoODKMainMapActivity.this.mapView.invalidate();

                    }
                    //This resets the map and sets the selected Layer
                    GeoODKMainMapActivity.this.selected_layer = item;
                    dialog.dismiss();
                } catch (RuntimeException e) {
                    createErrorDialog(e.getMessage(), false);
                    return;
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GeoODKMainMapActivity.this.mapView.invalidate();
                    }
                }, 400);

            }
        });
        alertDialog.show();

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

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog",
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

}
