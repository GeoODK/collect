package com.geoodk.collect.android.spatial;

/**
 * Created by jnordling on 10/14/15.
 */
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public class GoogleEarthMarkTileSource extends OnlineTileSourceBase {
//    private static final String[] BASE_URL = {
//            "http://mt0.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
//            "http://mt1.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
//            "http://mt2.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d",
//            "http://mt3.google.com/vt/lyrs=m@135&hl=zh-CN&x=%d&y=%d&z=%d" };
//    private static final String[] BASE_URL = {
//                "http://khms0.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
//                "http://khms1.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
//            "http://khms2.googleapis.com/kh?v=186&x=%d&y=%d&z=%d",
//            "http://khms3.googleapis.com/kh?v=186&x=%d&y=%d&z=%d" };
//    private static final String NAME = "Google Earth Mark";
//    private static final String NAME = "Google Street";
    public GoogleEarthMarkTileSource(String name, String[] baseurl) {
        super(name, string.unknown, 0, 20, 256, ".png", baseurl);
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        return String.format(getBaseUrl(), aTile.getX(), aTile.getY(),
                aTile.getZoomLevel());
    }

}
