package com.geoodk.collect.android.activities;

/*
 * 06.30.2014
 * Jon Nordling
 * Matias Something?
 *
 * This activity is to map the data offline
 *
 */

import java.io.File;
import java.io.FileReader;
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
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.application.Collect;
import com.geoodk.collect.android.preferences.MapSettings;
import com.geoodk.collect.android.provider.FormsProviderAPI.FormsColumns;
import com.geoodk.collect.android.provider.InstanceProviderAPI;
import com.geoodk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import com.geoodk.collect.android.spatial.CustomMarkerHelper;
import com.geoodk.collect.android.spatial.CustomPopupMaker;
import com.geoodk.collect.android.spatial.MBTileProvider;

public class OSM_Map extends Activity implements IRegisterReceiver{
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
    

    XmlPullParserFactory factory;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.osmmap_layout); //Setting Content to layout xml
        setTitle(getString(R.string.app_name) + " > Mapping"); // Setting title of the action
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
        mapView = (MapView)findViewById(R.id.MapViewId);
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
        
        gps_button = (ImageButton)findViewById(R.id.gps_button);
        //This is the gps button and its functionality
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	setGPSStatus();
            }
        });
        
        layers_button = (ImageButton)findViewById(R.id.layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                showLayersDialog();

            }
        });
        
        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
        imlp.setLocationUpdateMinDistance(1000);
        imlp.setLocationUpdateMinTime(60000);
        mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(centerAroundFix);
        setGPSStatus();
        
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




        //CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        //compassOverlay.enableCompass();
        //mapView.getOverlays().add(compassOverlay);
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
        //Initializing all the
    	super.onResume(); // Find out what this does? bar 
        online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPQUESTOSM");
        hideInfoWindows();
        setbasemapTiles(basemap);
        mapView.setTileSource(baseTiles);
        mapView.setUseDataConnection(online);
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
            loc_marker = new Marker(mapView);
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(OSM_Map.this);
        builder.setMessage(
                "Are you sure you want to change the location of this point?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public void changeInstanceLocation(final Marker mk) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException, TransformerException{
        final String url = ((CustomMarkerHelper)mk).getMarker_url();
        final File xmlFile = new File(url);
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        final Document doc = docBuilder.parse(xmlFile);
        final Node file_value = doc.getElementsByTagName(((CustomMarkerHelper)mk).getMarker_geoField()).item(0).getFirstChild();
        final String temp = Double.toString((((CustomMarkerHelper)mk).getPosition().getLatitude()))+ " "+ Double.toString((((CustomMarkerHelper)mk).getPosition().getLongitude()))+ " 0.1 0.1";
        final String old_loc = Double.toString(lat_temp) +" " +Double.toString(lng_temp);
        file_value.setNodeValue(temp);
        //String old_loc = Double.toString(lat_temp) +" " +Double.toString(lng_temp);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
        final StreamResult results = new StreamResult(xmlFile);
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        transformer.transform(source, results);
        mapView.invalidate();

    }



    public void createMaker (final String[] cur_mark) throws XmlPullParserException, IOException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new FileReader(new File(cur_mark[pos_url])));
        int eventType = xpp.getEventType();

        //For each of the objects in the instance xml <location>
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (xpp.getName()!=null){
                if(xpp.getName().equals(cur_mark[pos_geoField])){
                    if (eventType == XmlPullParser.START_TAG){
                        final String tagname = xpp.getName();
                        eventType = xpp.next();
                        final String value = xpp.getText();
                        if (value != null){
                            final String[] location = xpp.getText().split(" ");
                            final Double lat = Double.parseDouble(location[0]);
                            final Double lng = Double.parseDouble(location[1]);
                            final GeoPoint point = new GeoPoint(lat, lng);
                            final CustomMarkerHelper startMarker = new CustomMarkerHelper(mapView);
                            startMarker.setMarker_name(cur_mark[pos_name]);
                            startMarker.setMarker_uri(Uri.parse(cur_mark[pos_uri]));
                            startMarker.setMarker_status(cur_mark[pos_status]);
                            startMarker.setMarker_url(cur_mark[pos_url]);
                            startMarker.setMarker_id(cur_mark[pos_id]);
                            startMarker.setMarker_geoField(cur_mark[pos_geoField]);
                            startMarker.setPosition(point);
                            startMarker.setIcon(getResources().getDrawable(R.drawable.map_marker));
                            startMarker.setTitle("Name: "+ cur_mark[pos_name]);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            startMarker.setSnippet("Status: "+cur_mark[pos_status]);
                            startMarker.setDraggable(true);
                            startMarker.setOnMarkerDragListener(draglistner);
                            startMarker.setInfoWindow(new CustomPopupMaker(mapView, Uri.parse(cur_mark[pos_uri])));
                            mapView.getOverlays().add(startMarker);
                            break;
                        }else{
                            break;
                        }
                    }
                }

            }
            eventType = xpp.next();
        }
    }

    private void drawMarkers() {
        final String selection = InstanceColumns.STATUS + " != ?"; // Find out what this does
        final String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};  //Look like if arguments passed idk.

        //For each instance in the db if there is a point then add it to the overlay/marker list
        final String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC";
        final Cursor instance_cur = getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
        //todo catch when c==null
        instance_cur.moveToFirst();
        while (!instance_cur.isAfterLast()) {
            final String instance_url = instance_cur.getString(instance_cur.getColumnIndex("instanceFilePath"));
            final String instance_form_id = instance_cur.getString(instance_cur.getColumnIndex("jrFormId"));
            final String instance_form_name = instance_cur.getString(instance_cur.getColumnIndex("displayName"));
            final String instance_form_status = instance_cur.getString(instance_cur.getColumnIndex("status"));
            final Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, instance_cur.getLong(instance_cur.getColumnIndex(InstanceColumns._ID)));
            final String instanceUriString = instanceUri.toString();
            String geopoint_field = null;

            try {
                geopoint_field = getGeoField(instance_form_id);
                //Toast.makeText(this,geopoint_field, Toast.LENGTH_SHORT).show();
            } catch (final XmlPullParserException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (final IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            final String[] markerObj = {instance_url,instance_form_id,instance_form_name,instance_form_status,instanceUriString,geopoint_field};
            markerListArray.add(markerObj);

            //startActivity(new Intent(Intent.ACTION_EDIT, instanceUri));

            //Determine the geoPoint Field
            try {
                createMaker(markerObj);
                //addGeoPointMarkerList(instance_cur);
            } catch (final XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            instance_cur.moveToNext();
        }

        instance_cur.close();
    }



    //Make this more eficient so that you dont have to use the cursor all the time only if the form has not be queried
    public String getGeoField(final String form_id) throws XmlPullParserException, IOException{
        String formFilePath ="";
        final String formsortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
        final Cursor form_curser =  getContentResolver().query(FormsColumns.CONTENT_URI, null, null, null, formsortOrder);
        form_curser.moveToFirst();
        //int count = 0;
        while(!form_curser.isAfterLast()){
            final String tempformID = form_curser.getString(form_curser.getColumnIndex("jrFormId"));
            if(tempformID.equals(form_id)){
                //read xml and get geopoint table name
                //Toast.makeText(this,form_id+" == "+tempformID, Toast.LENGTH_SHORT).show();
                formFilePath =form_curser.getString(form_curser.getColumnIndex("formFilePath"));
                break;
                //Read the
                //count++;
            }else{
                //Toast.makeText(this,form_id+" !: "+tempformID, Toast.LENGTH_SHORT).show();
            }
            form_curser.moveToNext();
        }
        form_curser.close();
        String db_field_name= "";
        if (formFilePath != ""){
            //That file exists
            //Read the Xml file of the instance
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new FileReader(new File(formFilePath)));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
            	String name = xpp.getName();
            	String name_space = xpp.getNamespace();
                if (xpp.getName()!=null){
                    if(xpp.getName().equals("bind")){
                    	if (xpp.getAttributeValue(null,"type")!=null){
                    		String bind_type = xpp.getAttributeValue(null,"type");
		                        if (bind_type.equals("geopoint")){
		                        	String[] bind_nodeset = (xpp.getAttributeValue(null, "nodeset")).split("/");
		                        	String bind_db_name = bind_nodeset[bind_nodeset.length -1];
		                            db_field_name= bind_db_name;
		                            break;
		                        }
                        }

                    }
                }
                eventType = xpp.next();

            }


            //Now you loop through the xml form to find the geopoint.
            //Im sure ODK has something that figured this out, but I could not find it so I wrote it


        }else{
            //File file Does not exist
        }
        return db_field_name;

    }
    private String getMBTileFromItem(final int item) {
        // TODO Auto-generated method stub
        final String foldername = OffilineOverlays[item];
        final File dir = new File(Collect.OFFLINE_LAYERS+File.separator+foldername);
        String mbtilePath;
        final File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".mbtiles");
            }
        });
        mbtilePath =Collect.OFFLINE_LAYERS+File.separator+foldername+File.separator+files[0].getName();
        //returnFile = new File(Collect.OFFLINE_LAYERS+File.separator+foldername+files[0]);

        return mbtilePath;
    }
    private String[] getOfflineLayerList() {
        // TODO Auto-generated method stub
        final File files = new File(Collect.OFFLINE_LAYERS);
        final ArrayList<String> results = new ArrayList<String>();
        results.add("None");
        final String[] overlay_folders =  files.list();
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



    //This is going to be the listner for the devices locations

    public void hideInfoWindows(){
        final List<Overlay> overlays = mapView.getOverlays();
        for (final Overlay overlay : overlays) {
            if (overlay.getClass() == CustomMarkerHelper.class){
                ((CustomMarkerHelper)overlay).hideInfoWindow();
            }
        }

    }
    private void setbasemapTiles(final String basemap) {
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
    private void showLayersDialog() {
        // TODO Auto-generated method stub
        //FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
        //View view=fl.inflate(self, R.layout.showlayers_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OSM_Map.this);
        alertDialog.setTitle("Select Offline Layer");
        OffilineOverlays = getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
        //alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
        alertDialog.setSingleChoiceItems(OffilineOverlays,selected_layer,new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {
                //Toast.makeText(OSM_Map.this,item, Toast.LENGTH_LONG).show();
                // The 'which' argument contains the index position
                // of the selected item
                //Toast.makeText(OSM_Map.this,item +" ", Toast.LENGTH_LONG).show();

                switch(item){
                case 0 :
                    mapView.getOverlays().remove(mbTileOverlay);
                    layerStatus =false;
                    break;
                default:
                    mapView.getOverlays().remove(mbTileOverlay);
                    //String mbTileLocation = getMBTileFromItem(item);
                    final String mbFilePath = getMBTileFromItem(item);
                    //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
                    final File mbFile = new File(mbFilePath);
                    mbprovider = new MBTileProvider(OSM_Map.this, mbFile);
                    mbTileOverlay = new TilesOverlay(mbprovider,OSM_Map.this);
                    mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                    mapView.getOverlays().add(mbTileOverlay);
                    drawMarkers();
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


}