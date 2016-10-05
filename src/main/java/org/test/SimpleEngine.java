package org.test;

import net.openhft.chronicle.engine.SimpleEngineMain;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;

import java.io.IOException;

/**
 * @author Rob Austin.
 */
public class SimpleEngine {

    public static VanillaAssetTree createEngine() throws IOException {
        VanillaAssetTree assetTree = SimpleEngineMain.tree();

        MapView<String, String> mapView = assetTree.acquireMap("/my/demo/map", String.class, String.class);

        mapView.put("hello", "world");
        return assetTree;
    }

}
