package ddp.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.map.SubscriptionKeyValueStore;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.MapKVSSubscription;
import net.openhft.chronicle.engine.map.ObjectSubscription;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;

public class ServerMain {
    public static final WireType WIRE_TYPE = WireType.BINARY;
    public static boolean _addValuesAndSubscriber = Boolean.getBoolean("add");
    private VanillaAssetTree _assetTree;
    private VanillaAsset _root;
    private String _mapName = "testMap";
    private ServerEndpoint serverEndpoint;

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerMain serverMain = new ServerMain();
        serverMain.start();

        if (_addValuesAndSubscriber) {
            serverMain.addValuesAndSubscriber();
        }

        System.in.read();
    }

    public void start() throws IOException {
        //TODO DS move to constructor
        int port = 8088;

        _assetTree = new VanillaAssetTree().forServer();

        _root = _assetTree.root();

        serverEndpoint = new ServerEndpoint("*:" + port, _assetTree);

        _root.addWrappingRule(AuthorizedKeyValueStore.class, "authenticated kvs",
                AuthorizedKeyValueStore::new, SubscriptionKeyValueStore.class);

        //TODO DS implement
        _root.addWrappingRule(AuthenticatedKeyValueStore.class, "authenticated kvs",
                DdpAuthenticatedKeyValueStore::new, AuthorizedKeyValueStore.class);


        _root.addWrappingRule(ObjectSubscription.class, "authenticated subscription",
                AuthenticatedKVSubscription::new, MapKVSSubscription.class);

        _root.addLeafRule(MapKVSSubscription.class, "DDP", MapKVSSubscription::new);
    }

    public void addValuesAndSubscriber() throws InterruptedException {
        SessionProvider sessionProvider = _root.findView(SessionProvider.class);
        sessionProvider.set(VanillaSessionDetails.of("java-daniels", "secretPwd",""));

        MapView<String, String> testMap = _assetTree.acquireMap(_mapName, String.class, String.class);

        String key = "TestKey1";
        String value = "TestValue1";

        testMap.put(key, value);

        String getValue = testMap.get(key);

        System.out.println("Getting: " + getValue);

        assert value == getValue;

        testMap.registerSubscriber(x -> System.out.println("EVENT: " + x));

        Jvm.pause(200);

        key = "TestKey2";
        value = "TestValue2";

        testMap.put(key, value);

        getValue = testMap.get(key);

        System.out.println("Getting: " + getValue);

        assert value == getValue;
    }
}