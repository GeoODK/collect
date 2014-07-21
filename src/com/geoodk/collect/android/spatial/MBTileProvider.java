package com.geoodk.collect.android.spatial;

import java.io.File;
import java.util.Collections;
 
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;


 
/**
 * This class is a simplification of the the MapTileProviderArray: it only
 * allows a single provider.
 */
public class MBTileProvider extends MapTileProviderArray {
 
    public MBTileProvider(IRegisterReceiver receiverRegistrar, File file) {
 
        /**
         * Call the super-constructor.
         *
         * MapTileProviderBase requires a TileSource. As far as I can tell it is
         * only used in its method rescaleCache(...) to get the pixel size of a
         * tile. It seems to me that this is inappropriate, as a MapTileProvider
         * can have multiple sources (like the module array defined below) and
         * therefore multiple tileSources which might return different values!!
         *
         * If the requirement is that the tile size is equal across tile
         * sources, then the parameter should be obtained from a different
         * location, From TileSystem for example.
         */
        super(MBTileSource.createFromFile(file), receiverRegistrar);
 
        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the DB.
        MBTileModuleProvider moduleProvider;
        moduleProvider = new MBTileModuleProvider(receiverRegistrar,
                                                  file,
                                                  (MBTileSource) getTileSource());
 
        MapTileModuleProviderBase[] pTileProviderArray;
        pTileProviderArray = new MapTileModuleProviderBase[] { moduleProvider };
 
        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        Collections.addAll(mTileProviderList, pTileProviderArray);
    }
     
    // TODO: implement public Drawable getMapTile(final MapTile pTile) {}
    //       The current implementation is needlessly complex because it uses
    //       MapTileProviderArray as a basis.
 
}