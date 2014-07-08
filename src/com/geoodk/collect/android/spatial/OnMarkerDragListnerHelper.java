package com.geoodk.collect.android.spatial;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class OnMarkerDragListnerHelper extends Marker{

	public OnMarkerDragListnerHelper(MapView mapView) {
		super(mapView);
		// TODO Auto-generated constructor stub
	}

	public interface OnMarkerDragListenerHelper{
		abstract void onMarkerDrag(Marker marker);
		abstract void onMarkerDragEnd(Marker marker);
		abstract void onMarkerDragStart(Marker marker);
	}

}
