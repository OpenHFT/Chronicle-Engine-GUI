package examples;

import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine.map.VanillaKeyValueStore;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import org.junit.Assert;

import java.util.Map;

public class Example1
{
    public static void main(String[] args)
    {
        AssetTree assetTree = new VanillaAssetTree(1);

        //View is the interface you use for the underlying store (wrapper for underlying store)
        //Leaf rule is not expected to be layered on top of anything, min 1 leaf rule.
        //LEaf is the base for whatever implementation you want to use
        assetTree.root().addLeafRule(AuthenticatedKeyValueStore.class, "My auth store", VanillaKeyValueStore::new);
        Asset testAsset = assetTree.acquireAsset("/test");
        testAsset.addLeafRule(AuthenticatedKeyValueStore.class, "My auth store", (rc, asset) -> new FilePerKeyValueStore(rc.basePath("C:\\LocalFolder\\"), asset));

        //Defines a view on an already existing service - a leaf rule has to exist for any wrapping rule
        assetTree.root().addWrappingRule(MapView.class, "My map view", VanillaMapView::new, AuthenticatedKeyValueStore.class);

        Map<String, String> map1 = assetTree.acquireMap("map1", String.class, String.class);

        map1.put("Key1", "Value1");
        System.out.println(map1.get("Key1"));
        Assert.assertEquals(VanillaKeyValueStore.class, ((MapView) map1).underlying().getClass());

        Map<String, String> test_map = assetTree.acquireMap("/test2/map1", String.class, String.class);
        Assert.assertEquals(FilePerKeyValueStore.class, ((MapView) test_map).underlying().getClass());

    }
}