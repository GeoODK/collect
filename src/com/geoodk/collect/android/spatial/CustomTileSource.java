package com.geoodk.collect.android.spatial;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class CustomTileSource extends BitmapTileSourceBase {

	//public CustomTileSource(String aName, string aResourceId,int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels,	String aImageFilenameEnding) {
	public CustomTileSource(String aName, string aResourceId) {	
	//super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,aImageFilenameEnding);
		super(aName, aResourceId, 1, 6, 256,".png");
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public String getTileRelativeFilenameString(MapTile tile) {
        final StringBuilder sb = new StringBuilder();
        sb.append(pathBase());
        sb.append('/');
        sb.append(tile.getZoomLevel());
        sb.append('/');
        sb.append(tile.getX());
        sb.append('_');
        sb.append(tile.getY());
        sb.append(imageFilenameEnding());
        return sb.toString();

    }

}
