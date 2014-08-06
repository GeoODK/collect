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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;
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
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

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
    MyLocationNewOverlay mMyLocationOverlay;
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
    public int zoom_level = 3;

    //This is used to store temp latitude values
    private Double lat_temp;
    private Double lng_temp;

    //Keep Track if GPS button is on or off
    //MyLocationOverlay myLocationOverlay = null;

    public Boolean gpsStatus = false;
    public Boolean layerStatus = false;
    private int selected_layer= -1;

    private MBTileProvider mbprovider;
    private TilesOverlay mbTileOverlay;

    private String[] OffilineOverlays;
    private ITileSource baseTiles;

    XmlPullParserFactory factory;


    private final LocationListener myLocationListener = new LocationListener(){

        @Override
        public void onLocationChanged(final Location location) {
            // TODO Auto-generated method stub
            //updateLoc(location);
            OSM_Map.this.mapView.getOverlays().remove(OSM_Map.this.loc_marker);
            //Toast.makeText(OSM_Map.this,"Location Update", Toast.LENGTH_LONG).show();
            final GeoPoint current_loc = new GeoPoint(location);
            OSM_Map.this.loc_marker.setPosition(current_loc);
            OSM_Map.this.mapView.getOverlays().add(OSM_Map.this.loc_marker);
            OSM_Map.this.mapView.invalidate();

            //loc_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            //loc_marker.setIcon(getResources().getDrawable(R.drawable.loc_logo_small));
            //mapView.getOverlays().add(loc_marker);
            //Toast.makeText(this,(location.getLatitude())+" "+location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(final String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(final String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            // TODO Auto-generated method stub

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
            OSM_Map.this.loc_marker = new Marker(OSM_Map.this.mapView);
            final String lat = Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
            final String lng = Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
            //Toast.makeText(OSM_Map.this,lat+" "+lng, Toast.LENGTH_LONG).show();
            OSM_Map.this.askToChangePoint(m);
            // TODO Auto-generated method stub
            //Toast.makeText(OSM_Map.this,((CustomMarkerHelper)m).getMarker_url(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onMarkerDragStart(final Marker m) {
            // TODO Auto-generated method stub
            //lat_temp =  Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
            //lng_temp  =  Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
            OSM_Map.this.lat_temp =  ((CustomMarkerHelper)m).getPosition().getLatitude();
            OSM_Map.this.lng_temp  =  ((CustomMarkerHelper)m).getPosition().getLongitude();
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
                        OSM_Map.this.changeInstanceLocation(mk);
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
                    ((CustomMarkerHelper)mk).setPosition(new GeoPoint(OSM_Map.this.lat_temp, OSM_Map.this.lng_temp));
                    OSM_Map.this.mapView.invalidate();
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
        final String old_loc = Double.toString(this.lat_temp) +" " +Double.toString(this.lng_temp);
        file_value.setNodeValue(temp);
        //String old_loc = Double.toString(lat_temp) +" " +Double.toString(lng_temp);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
        final StreamResult results = new StreamResult(xmlFile);
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        transformer.transform(source, results);
        this.mapView.invalidate();

    }



    public void createMaker (final String[] cur_mark) throws XmlPullParserException, IOException {

        //Read the Xml file of the instance
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
                            final CustomMarkerHelper startMarker = new CustomMarkerHelper(this.mapView);
                            startMarker.setMarker_name(cur_mark[pos_name]);
                            startMarker.setMarker_uri(Uri.parse(cur_mark[pos_uri]));
                            startMarker.setMarker_status(cur_mark[pos_status]);
                            startMarker.setMarker_url(cur_mark[pos_url]);
                            startMarker.setMarker_id(cur_mark[pos_id]);
                            startMarker.setMarker_geoField(cur_mark[pos_geoField]);
                            startMarker.setPosition(point);
                            startMarker.setIcon(this.getResources().getDrawable(R.drawable.map_marker));
                            startMarker.setTitle("Name: "+ cur_mark[pos_name]);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            startMarker.setSnippet("Status: "+cur_mark[pos_status]);
                            startMarker.setDraggable(true);
                            startMarker.setOnMarkerDragListener(this.draglistner);
                            startMarker.setInfoWindow(new CustomPopupMaker(this.mapView, Uri.parse(cur_mark[pos_uri])));
                            //popup_button.setOnClickListener(new on);
                            //startMarker.setSubDescription("Desc");
                            //startMarker.setIcon(getResources().getDrawable(R.drawable.pin_marker));
                            this.mapView.getOverlays().add(startMarker);

                            //OverlayItem overlayitem = new OverlayItem("Title", "SampleDescription", point);
                            //Drawable marker = this.getResources().getDrawable(R.drawable.d);
                            //overlayitem.setMarker(marker);
                            //marker_list.add(overlayitem);
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
        final Cursor instance_cur = this.getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
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
                geopoint_field = this.getGeoField(instance_form_id);
                //Toast.makeText(this,geopoint_field, Toast.LENGTH_SHORT).show();
            } catch (final XmlPullParserException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (final IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            final String[] markerObj = {instance_url,instance_form_id,instance_form_name,instance_form_status,instanceUriString,geopoint_field};
            this.markerListArray.add(markerObj);

            //startActivity(new Intent(Intent.ACTION_EDIT, instanceUri));

            //Determine the geoPoint Field
            try {
                this.createMaker(markerObj);
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
        final Cursor form_curser =  this.getContentResolver().query(FormsColumns.CONTENT_URI, null, null, null, formsortOrder);
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
            this.factory = XmlPullParserFactory.newInstance();
            this.factory.setNamespaceAware(true);
            final XmlPullParser xpp = this.factory.newPullParser();
            xpp.setInput(new FileReader(new File(formFilePath)));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.getName()!=null){
                    if(xpp.getName().equals("bind")){
                        final String bind_type = xpp.getAttributeValue(null, "type");
                        final String[] bind_nodeset = (xpp.getAttributeValue(null, "nodeset")).split("/");
                        final String bind_db_name = bind_nodeset[bind_nodeset.length -1];
                        //Toast.makeText(this,bind_type+" "+bind_db_name, Toast.LENGTH_SHORT).show();
                        if (bind_type.equals("geopoint")){
                            db_field_name= bind_db_name;
                            //Toast.makeText(this,bind_type+" "+db_field_name, Toast.LENGTH_SHORT).show();
                            break;
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
        final String foldername = this.OffilineOverlays[item];
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
        final List<Overlay> overlays = this.mapView.getOverlays();
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

        this.setContentView(R.layout.osmmap_layout); //Setting Content to layout xml
        this.setTitle(this.getString(R.string.app_name) + " > Mapping"); // Setting title of the action
        //Map Settings
        //PreferenceManager.setDefaultValues(this, R.xml.map_preferences, false);
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //PreferenceManager.setDefaultValues(this, R.xml.map_preferences, false);
        //PreferenceActivity.class.addPreferencesFromResource(R.xml.map_preferences);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //sharedPreferences = getSharedPreferences(MapSettings.MAP_PREFERENCES,);
        //online = sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        //basemap = sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPNIK");
        //setbasemapTiles(basemap);
        //Toast.makeText(OSM_Map.this, "Online: "+online+" ", Toast.LENGTH_LONG).show();


        this.resource_proxy = new DefaultResourceProxyImpl(this.getApplicationContext());
        //Layout Code MapView Connection and options
        this.mapView = (MapView)this.findViewById(R.id.MapViewId);
        //mapView.setTileSource(baseTiles);
        //final CustomTileSource tileSource = new CustomTileSource(Environment.getExternalStorageDirectory().getPath()+ "/osmdroid/tiles/MyMap", null);
        //mapView.setTileSource(tileSource);
        //String h = Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles";
        //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
        this.mapView.setMultiTouchControls(true);
        this.mapView.setBuiltInZoomControls(true);
        //mapView.setUseDataConnection(online);
        this.mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(final ScrollEvent arg0) {
                return false;
            }
            @Override
            public boolean onZoom(final ZoomEvent zoomLev) {
                OSM_Map.this.zoom_level = zoomLev.getZoomLevel();
                return false;
            }
        });

        //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
        //MBTileProvider mbprovider = new MBTileProvider(this, mbFile);
        //TilesOverlay mbTileOverlay = new TilesOverlay(mbprovider,this);
        //mapView.getOverlays().add(mbTileOverlay);
        this.mapView.invalidate();
        //mapView = new MapView(this, mbprovider.getTileSource().getTileSizePixels(), resource_proxy, mbprovider);
        //Figure this out!!!!! I want to call this a a class and return the some value!!!!!!1
        //String name = geoheler.getGeopointDBField(temp);

        //Sets the  Resource Proxy

        final ImageButton map_setting_button = (ImageButton) this.findViewById(R.id.map_setting_button);
        map_setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                final Intent i = new Intent(OSM_Map.this.self, MapSettings.class);
                OSM_Map.this.startActivity(i);

            }
        });
        final ImageButton gps_button = (ImageButton)this.findViewById(R.id.gps_button);
        //This is the gps button and its functionality
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(OSM_Map.this.gpsStatus ==false){
                    gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
                    OSM_Map.this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, OSM_Map.this.myLocationListener);
                    OSM_Map.this.gpsStatus = true;
                }else{
                    gps_button.setImageResource(R.drawable.ic_menu_mylocation);
                    OSM_Map.this.locationManager.removeUpdates(OSM_Map.this.myLocationListener);
                    OSM_Map.this.gpsStatus = false;
                }
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationListener);
            }
        });
        final ImageButton layers_button = (ImageButton)this.findViewById(R.id.layers_button);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                OSM_Map.this.showLayersDialog();

            }
        });


        this.locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        this.lastLocation 	= this.locationManager.getLastKnownLocation(	LocationManager.GPS_PROVIDER);
        this.mMyLocationOverlay = new MyLocationNewOverlay(this, this.mapView);
        //mMyLocationOverlay.disableMyLocation(); // not on by default
        //mMyLocationOverlay.disableCompass();
        //mMyLocationOverlay.disableFollowLocation();
        //mMyLocationOverlay.setDrawAccuracyEnabled(true);
        this.mMyLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                OSM_Map.this.mapView.getController().animateTo(OSM_Map.this.mMyLocationOverlay
                        .getMyLocation());
            }
        });
        this.mapView.getOverlays().add(this.mMyLocationOverlay);
        this.loc_marker = new Marker(this.mapView);
        this.updateMyLocation();
    }

    @Override
    protected void onResume() {
        //Initializing all the
        this.online = this.sharedPreferences.getBoolean(MapSettings.KEY_online_offlinePrefernce, true);
        this.basemap = this.sharedPreferences.getString(MapSettings.KEY_map_basemap, "MAPNIK");
        super.onResume(); // Find out what this does? bar
        this.hideInfoWindows();
        this.updateMyLocation();
        this.setbasemapTiles(this.basemap);
        this.mapView.setTileSource(this.baseTiles);
        this.mapView.setUseDataConnection(this.online);
        this.drawMarkers();
        this.mapView.invalidate();
        //This is used to wait a second to wait the center the map on the points
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                GeoPoint point;
                if(OSM_Map.this.lastLocation != null){
                    point = new GeoPoint(OSM_Map.this.lastLocation.getLatitude(), OSM_Map.this.lastLocation.getLongitude());
                }else{
                    point = new GeoPoint(34.08145, -39.85007);
                }
                OSM_Map.this.mapView.getController().setZoom(OSM_Map.this.zoom_level);
                OSM_Map.this.mapView.getController().setCenter(point);
            }
        }, 100);
        this.mapView.invalidate();
    }

    //This function comes after the onCreate function
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //myMapController.setZoom(4);
    }

    private void setbasemapTiles(final String basemap) {
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
            this.baseTiles = TileSourceFactory.MAPNIK;
        }
    }
    private void showLayersDialog() {
        // TODO Auto-generated method stub
        //FrameLayout fl = (ScrollView) findViewById(R.id.layer_scroll);
        //View view=fl.inflate(self, R.layout.showlayers_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OSM_Map.this);
        alertDialog.setTitle("Select Offline Layer");
        this.OffilineOverlays = this.getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
        //alertDialog.setItems(list, new  DialogInterface.OnClickListener() {
        alertDialog.setSingleChoiceItems(this.OffilineOverlays,this.selected_layer,new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {
                //Toast.makeText(OSM_Map.this,item, Toast.LENGTH_LONG).show();
                // The 'which' argument contains the index position
                // of the selected item
                //Toast.makeText(OSM_Map.this,item +" ", Toast.LENGTH_LONG).show();

                switch(item){
                case 0 :
                    OSM_Map.this.mapView.getOverlays().remove(OSM_Map.this.mbTileOverlay);
                    OSM_Map.this.layerStatus =false;
                    break;
                default:
                    OSM_Map.this.mapView.getOverlays().remove(OSM_Map.this.mbTileOverlay);
                    //String mbTileLocation = getMBTileFromItem(item);
                    final String mbFilePath = OSM_Map.this.getMBTileFromItem(item);
                    //File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
                    final File mbFile = new File(mbFilePath);
                    OSM_Map.this.mbprovider = new MBTileProvider(OSM_Map.this, mbFile);
                    OSM_Map.this.mbTileOverlay = new TilesOverlay(OSM_Map.this.mbprovider,OSM_Map.this);
                    OSM_Map.this.mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                    OSM_Map.this.mapView.getOverlays().add(OSM_Map.this.mbTileOverlay);
                    OSM_Map.this.drawMarkers();
                    OSM_Map.this.mapView.invalidate();
                }
                //This resets the map and sets the selected Layer
                OSM_Map.this.selected_layer =item;
                dialog.dismiss();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        OSM_Map.this.mapView.invalidate();
                    }
                }, 400);

            }
        });
        //alertDialog.setView(view);
        alertDialog.show();

    }

    private void updateMyLocation() {
        // TODO Auto-generated method stub

        if(this.lastLocation != null){
            //Set the location of marker on the map
            //Toast.makeText(this,lastLocation.getLatitude()+" "+lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            final GeoPoint loc = new GeoPoint(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
            this.loc_marker.setPosition(loc);
            this.loc_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            this.loc_marker.setIcon(this.getResources().getDrawable(R.drawable.loc_logo_small));
            this.mapView.getOverlays().add(this.loc_marker);
        }

    }

}