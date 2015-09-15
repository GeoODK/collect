package com.geoodk.collect.android.spatial;

import com.geoodk.collect.android.application.Collect;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.File;
import java.util.ArrayList;

public class MapHelper {

	public static ITileSource getTileSource(String basemap) {
		// TODO Auto-generated method stub

        //Having Custom Tile Sources, this allows for custom zoom level

		
		ITileSource baseTiles;
        String[] baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.0d981b0d/"};
        if (basemap.equals("Default")){
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.0d981b0d/"};
        }else if(basemap.equals("Steets Classic")){
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.n141ednk/"};
        }else if(basemap.equals("Outdoors")){
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.n1417e4k/"};
        }else if(basemap.equals("Dark")){
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.n1425ld2/"};
        }else if(basemap.equals("Activities")){
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.0d981b0d/"};

        }else{
            //Else nothing
            baseURL = new String[]{"http://api.mapbox.com/v4/jonnordling.0d981b0d/"};
        	
        }

        final String accessToken = "pk.eyJ1Ijoiam9ubm9yZGxpbmciLCJhIjoiZTcwNDcxN2ZiMWU0YTZhZjM2ZWFlNTMxZWI4Y2QwNWMifQ.mMQKvbPR2IYIv7DsV2HU4A#4";
        baseTiles = new XYTileSource(basemap, null, 1, 22, 256, ".png", baseURL){
            @Override
            public String getTileURLString(MapTile aTile) {
                String str = super.getTileURLString(aTile) + "?access_token=" + accessToken;
                return str;
            }
        };

        return baseTiles;
	}
    public static String[] getOfflineLayerList() {
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
    /*private String getMBTileFromItem(final int item) {
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
    }*/
}
