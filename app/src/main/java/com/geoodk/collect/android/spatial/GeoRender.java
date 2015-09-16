package com.geoodk.collect.android.spatial;

/**
 * Created by jnordling on 9/13/15.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.geoodk.collect.android.provider.FormsProviderAPI;
import com.geoodk.collect.android.provider.InstanceProviderAPI;


import org.osmdroid.views.MapView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class GeoRender {

    public Context context;
    public MapView mapView;
    public ArrayList<GeoFeature> geoFeatures = new ArrayList<GeoFeature>();
    private ArrayList<GeoFeature> geoFeaturesList;
    private XmlPullParserFactory factory;
    private ArrayList geoDataArray;

    private String geoshape = "geoshape";
    private String geopoint = "geopoint";
    private String geotrace = "geotrace";

    public ArrayList getGeoData(){
        return this.geoDataArray;
    }


    public GeoRender(Context pContext,MapView mapView) {
        if((pContext != null) && (mapView !=null)) {
            this.context = pContext;
            this.mapView = mapView;
            Cursor instanceCursor = this.getAllCursor();
            while (instanceCursor.moveToNext()) {
                String instance_url = instanceCursor.getString(instanceCursor.getColumnIndex("instanceFilePath"));
                String instance_form_id = instanceCursor.getString(instanceCursor.getColumnIndex("jrFormId"));
                String instance_form_name = instanceCursor.getString(instanceCursor.getColumnIndex("displayName"));
                String instance_form_status = instanceCursor.getString(instanceCursor.getColumnIndex("status"));
                Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instanceCursor.getLong(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                String instanceUriString = instanceUri.toString();
                String formFilePath = getFormFilePath(instance_form_id);
                File fXmlFile = new File(formFilePath);
                Log.i("mylog", instance_form_name);
                GeoFeature geoFeature = new GeoFeature();
                geoFeature.setInstance_form_id(instance_form_id);
                geoFeature.setInstance_form_name(instance_form_name);
                geoFeature.setInstance_form_status(instance_form_status);
                geoFeature.setInstance_url(instance_url);
                geoFeature.setInstanceUriString(instanceUriString);
                geoFeature.setGeoFields(getGeoField(geoFeature,fXmlFile));

//                createPointOverlay(geoFeature);


                String tesss = "sds";

            }
            instanceCursor.close();

        }
    }

    private Cursor getAllCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or "+ InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "+ InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,InstanceProviderAPI.STATUS_SUBMISSION_FAILED,InstanceProviderAPI.STATUS_SUBMITTED,InstanceProviderAPI.STATUS_INCOMPLETE };
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor c = this.context.getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
        return c;
    }

    private String getFormFilePath(String instance_form_id){
        String formsortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        String selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?";
        String selectionArgs[] = {instance_form_id};
        Cursor form_curser =  this.context.getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, selection, selectionArgs, formsortOrder);
        form_curser.moveToNext();
        String formFilePath = form_curser.getString(form_curser.getColumnIndex("formFilePath"));
        form_curser.close();
        return formFilePath;
    }


    private ArrayList getGeoField(GeoFeature geoFeature,File file){
        //final String[] markerObj = {instance_url,instance_form_id,instance_form_name,instance_form_status,instanceUriString,geopoint_field};
        ArrayList<GeoObject> geoFields = new ArrayList<GeoObject>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try{
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            try {
                doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                String root = doc.getDocumentElement().getNodeName();
                NodeList nList = doc.getElementsByTagName("bind");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    nNode.getNodeName();
                    String name = nNode.getNodeName();
                    Short s = nNode.getNodeType();
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String type = eElement.getAttribute("type");
                        if (type.equals( geopoint) || type.equals(geoshape) || type.equals(geotrace)){
//                            ArrayList<String> singleList = new ArrayList<String>();
                            GeoObject geoObject = new GeoObject();
                            String nodeset = eElement.getAttribute("nodeset");
                            geoObject.setNodeset(nodeset);
                            geoObject.setGeotype(type);

                            //Set The overlay data
                            if (type.equals( geopoint)){
                                geoObject.setPointMarker(createPointOverlay(geoFeature, geoObject));
                            }

//                            if (type.equals(geoshape)) {
//                                createPathOverlay();
//                            }

                            geoFields.add(geoObject);
                        }
                    }
                }
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }catch(ParserConfigurationException e){
            e.printStackTrace();
        }
        return geoFields;
    }

    private CustomMarkerHelper createPointOverlay(GeoFeature geoFeature,GeoObject geoObject){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        CustomMarkerHelper marker = new CustomMarkerHelper(this.mapView);

        return marker;

    }

    private void createPathOverlay(){

    }

//    public void createMaker (final String[] cur_mark) throws XmlPullParserException, IOException {
//        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//        factory.setNamespaceAware(true);
//        XmlPullParser xpp = factory.newPullParser();
//        xpp.setInput(new FileReader(new File(cur_mark[pos_url])));
//        int eventType = xpp.getEventType();
//
//        //For each of the objects in the instance xml <location>
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            if (xpp.getName()!=null){
//                if(xpp.getName().equals(cur_mark[pos_geoField])){
//                    if (eventType == XmlPullParser.START_TAG){
//                        String tagname = xpp.getName();
//                        eventType = xpp.next();
//                        String value = xpp.getText();
//                        if (value != null){
//                            String[] location = xpp.getText().split(" ");
//                            Double lat = Double.parseDouble(location[0]);
//                            Double lng = Double.parseDouble(location[1]);
//                            GeoPoint point = new GeoPoint(lat, lng);
//                            CustomMarkerHelper startMarker = new CustomMarkerHelper(mapView);
//                            startMarker.setMarker_name(cur_mark[pos_name]);
//                            startMarker.setMarker_uri(Uri.parse(cur_mark[pos_uri]));
//                            startMarker.setMarker_status(cur_mark[pos_status]);
//                            startMarker.setMarker_url(cur_mark[pos_url]);
//                            startMarker.setMarker_id(cur_mark[pos_id]);
//                            startMarker.setMarker_geoField(cur_mark[pos_geoField]);
//                            startMarker.setPosition(point);
//                            startMarker.setIcon(this.getResources().getDrawable(R.drawable.map_marker));
//                            startMarker.setTitle("Name: "+ cur_mark[pos_name]);
//                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                            startMarker.setSnippet("Status: "+cur_mark[pos_status]);
//                            startMarker.setDraggable(true);
//                            startMarker.setOnMarkerDragListener(draglistner);
//                            startMarker.setInfoWindow(new CustomPopupMaker(mapView, Uri.parse(cur_mark[pos_uri])));
//                            mapView.getOverlays().add(startMarker);
//                            break;
//                        }else{
//                            break;
//                        }
//                    }
//                }
//
//            }
//            eventType = xpp.next();
//        }
//    }

}
