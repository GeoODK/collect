package com.geoodk.collect.android.spatial;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

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

}
