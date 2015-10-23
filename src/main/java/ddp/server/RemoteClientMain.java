package ddp.server;

import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.wire.WireType;

/**
 * Created by daniels on 21/08/2015.
 */
public class RemoteClientMain {
    private static String _serverMapName = "testMap";
    private static String _remoteMapName = "testRemoteMap";

    private static boolean _useNewMap = Boolean.getBoolean("useNewMap");

    public static void main(String[] args) throws InterruptedException {
        String mapName = _useNewMap ? _remoteMapName : _serverMapName;

        VanillaAssetTree assetTree = new VanillaAssetTree().forRemoteAccess("localhost:8088", WireType.BINARY);

        VanillaAsset root = assetTree.root();

        //TODO DS don't know why this doesn't overwrite what is set in the for RemoteAccess method - server receives "daniels" and no pwd.
        SessionProvider sessionProvider = root.findView(SessionProvider.class);
        sessionProvider.set(VanillaSessionDetails.of("remote-java-daniels", "secretPwd", ""));

        MapView<String, String> testMap = assetTree.acquireMap(mapName, String.class, String.class);

        String key = "TestRemoteKey1";
        String value = "TestRemoteValue1";

        testMap.put(key, value);

        String getValue = testMap.get(key);

        System.out.println("Getting Remote: " + getValue);

        testMap.registerSubscriber(x -> System.out.println("REMOTE RECEIVED EVENT: " + x));

        Thread.sleep(200);

        key = "TestKey2";
        value = "TestValue2";

        testMap.put(key, value);

        getValue = testMap.get(key);

        System.out.println("REMOTE Getting: " + getValue);

        assert value == getValue;
    }
}