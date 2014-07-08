package com.geoodk.collect.android.spatial;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

import android.net.Uri;

public class CustomMarkerHelper extends Marker{

	Uri marker_uri;
	String marker_name;
	String marker_id;
	String marker_url;
	String marker_status;
	
	
	public CustomMarkerHelper(MapView mapView) {
		super(mapView);
		
		// TODO Auto-generated constructor stub
	}

	public Uri getMarker_uri() {
		return marker_uri;
	}

	public void setMarker_uri(Uri marker_uri) {
		this.marker_uri = marker_uri;
	}

	public String getMarker_name() {
		return marker_name;
	}

	public void setMarker_name(String marker_name) {
		this.marker_name = marker_name;
	}

	public String getMarker_id() {
		return marker_id;
	}

	public void setMarker_id(String marker_id) {
		this.marker_id = marker_id;
	}

	public String getMarker_url() {
		return marker_url;
	}

	public void setMarker_url(String marker_url) {
		this.marker_url = marker_url;
	}

	public String getMarker_status() {
		return marker_status;
	}

	public void setMarker_status(String marker_status) {
		this.marker_status = marker_status;
	}

}
