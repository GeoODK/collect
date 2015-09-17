package com.geoodk.collect.android.spatial;

/**
 * Created by jnordling on 9/13/15.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.provider.FormsProviderAPI;
import com.geoodk.collect.android.provider.InstanceProviderAPI;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
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
                geoFeature.setGeoFields(getGeoField(geoFeature, fXmlFile));

                this.geoFeatures.add(geoFeature);

                Log.i("mylog","herer");
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
                            String[] nodeset = eElement.getAttribute("nodeset").split("/");
                            geoObject.setNodeset(nodeset[nodeset.length -1]);
                            geoObject.setGeotype(type);
                            //Set The overlay data
                            if (type.equals( geopoint)){
                                geoObject.setPointMarker(createPointOverlay(geoFeature, geoObject));
                            }
                            if (type.equals(geoshape)) {
                                createPathOverlay(geoFeature, geoObject);
                            }

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
        try{
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            try {
                File file = new File(geoFeature.instance_url);
                doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                String root = doc.getDocumentElement().getNodeName();
                NodeList nList = doc.getElementsByTagName(geoObject.getNodeset());
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    nNode.getNodeName();
                    String name = nNode.getNodeName();
                    Short s = nNode.getNodeType();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String value = eElement.getTextContent();
                        if ((value != null) && (!value.equals(""))){
                            String[] location = value.split(" ");
                            String z = "";
                            Double lat = Double.parseDouble(location[0]);
                            Double lng = Double.parseDouble(location[1]);
                            GeoPoint point = new GeoPoint(lat, lng);
                            marker.setMarker_name(geoFeature.getInstance_form_name());
                            marker.setMarker_uri(Uri.parse(geoFeature.getInstanceUriString()));
                            marker.setMarker_status(geoFeature.getInstance_form_status());
                            marker.setMarker_url(geoFeature.getInstance_url());
                            marker.setMarker_id(geoFeature.getInstance_form_id());
                            marker.setMarker_geoField(geoObject.getNodeset());
                            marker.setPosition(point);
                            marker.setIcon(this.context.getResources().getDrawable(R.drawable.map_marker));
                            marker.setTitle("Name: " + geoFeature.getInstance_form_name());
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setSnippet("Status: "+geoFeature.getInstance_form_status());
                            marker.setDraggable(true);
//                            marker.setOnMarkerDragListener(draglistner);
                            marker.setInfoWindow(new CustomPopupMaker(this.mapView, Uri.parse(geoFeature.getInstanceUriString())));
                            this.mapView.getOverlays().add(marker);
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
        return marker;
    }
    private void createPathOverlay(GeoFeature geoFeature,GeoObject geoObject){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        PathOverlay pathOverlay = new PathOverlay(Color.RED, this.context);
        Paint pPaint = pathOverlay.getPaint();
        pPaint.setStrokeWidth(6);
//        CustomMarkerHelper marker = new CustomMarkerHelper(this.mapView);
        try{
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            try {
                File file = new File(geoFeature.instance_url);
                doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                String root = doc.getDocumentElement().getNodeName();
                NodeList nList = doc.getElementsByTagName(geoObject.getNodeset());
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    nNode.getNodeName();
                    String name = nNode.getNodeName();
//                    Short s = nNode.getNodeType();
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String value = eElement.getTextContent();
                        String s = value.replace("; ",";");
                        String[] sa = s.split(";");
                        if ((value != null) && (!value.equals(""))){
                            for (int i=0;i<(sa.length);i++){
                                String[] sp = sa[i].split(" ");
                                double gp[] = new double[4];
                                String lat = sp[0].replace(" ", "");
                                String lng = sp[1].replace(" ", "");
                                gp[0] = Double.parseDouble(lat);
                                gp[1] = Double.parseDouble(lng);
                    //			gp[0] = Double.valueOf(lat).doubleValue();
                    //			gp[1] = Double.valueOf(lng).doubleValue();
                                Marker marker = new Marker(mapView);
                                GeoPoint point = new GeoPoint(gp[0], gp[1]);
                                marker.setPosition(point);
                                marker.setDraggable(true);
                                marker.setIcon(this.context.getResources().getDrawable(R.drawable.map_marker));
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                marker.setOnMarkerClickListener(nullmarkerlistner);
//                                map_markers.add(marker);
                                pathOverlay.addPoint(marker.getPosition());
//                                marker.setDraggable(true);
////                                marker.setOnMarkerDragListener(draglistner);
//                                mapView.getOverlays().add(marker);
                            }
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
        mapView.getOverlays().add(pathOverlay);
//        return marker;

    }


}
