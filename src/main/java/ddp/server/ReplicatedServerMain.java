package ddp.server;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.map.SubscriptionKeyValueStore;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.cfg.ClustersCfg;
import net.openhft.chronicle.engine.cfg.EngineCfg;
import net.openhft.chronicle.engine.fs.ChronicleMapGroupFS;
import net.openhft.chronicle.engine.fs.FilePerKeyGroupFS;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.WireType;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ReplicatedServerMain {
    public static final WireType WIRE_TYPE = WireType.BINARY;
    public static final String NAME = "/ReplicationServer/test";
    public static boolean _addValuesAndSubscriber = Boolean.getBoolean("add");
    public static ServerEndpoint serverEndpoint1;
    public static ServerEndpoint serverEndpoint2;
    public static ServerEndpoint serverEndpoint3;
    private static AssetTree tree3;
    private static AssetTree tree1;
    private static AssetTree tree2;

    private String _mapName = "testMap";
    private ServerEndpoint serverEndpoint;

    public static void main(String[] args) throws IOException, InterruptedException {
        ReplicatedServerMain replicatedServerMain = new ReplicatedServerMain ();
        replicatedServerMain.start();

        //if (_addValuesAndSubscriber) {
            replicatedServerMain.addValuesAndSubscriber();
        //}

        System.in.read();
    }

    public void start() {
        try
        {
            //TODO DS move to constructor
            int port = 8088;

            ClassAliasPool.CLASS_ALIASES.addAlias(ChronicleMapGroupFS.class);
            ClassAliasPool.CLASS_ALIASES.addAlias(FilePerKeyGroupFS.class);
            //Delete any files from the last run
            TestUtils.deleteRecursive(new File(OS.TARGET, NAME));

            TCPRegistry.createServerSocketChannelFor("host.port1");

            tree1 = create(1, WIRE_TYPE, null);
            tree2 = create(2, WIRE_TYPE, null);
            tree3 = create(3, WIRE_TYPE, null);

            serverEndpoint1 = new ServerEndpoint("host.port1", tree1);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static AssetTree create(final int hostId, WireType writeType, Consumer<AssetTree>
            applyRules) {
        AssetTree tree = new VanillaAssetTree((byte) hostId)
                .forTesting(Throwable::printStackTrace)
                .withConfig(resourcesDir() + "/multrepltest", OS.TARGET + "/" + hostId);

        tree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                VanillaMapView::new,
                KeyValueStore.class);
        tree.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                CMap2EngineReplicator::new);
        tree.root().addLeafRule(KeyValueStore.class, "KVS is Chronicle Map", (context, asset) ->
                new ChronicleMapKeyValueStore(context.wireType(writeType).putReturnsNull(false),
                        asset));

        if (applyRules != null) {
            applyRules.accept(tree);
        }

        return tree;
    }

    public static String resourcesDir() {
        String path = ReplicatedServerMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path == null) {
            return ".";
        }
        return new File(path).getParentFile().getParentFile() + "/src/test/resources";
    }

    public void addValuesAndSubscriber() throws InterruptedException {
        SessionProvider sessionProvider = tree1.root().findView(SessionProvider.class);
        sessionProvider.set(VanillaSessionDetails.of("java-daniels", "secretPwd",""));

        MapView<String, String> testMap = tree1.acquireMap(_mapName, String.class, String.class);

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