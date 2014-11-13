package com.geoodk.collect.android.spatial;

import java.io.File;
import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.geoodk.collect.android.application.Collect;

public class MapHelper {
	
	public static ITileSource getTileSource(String basemap) {
		// TODO Auto-generated method stub
		
		ITileSource baseTiles;
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
