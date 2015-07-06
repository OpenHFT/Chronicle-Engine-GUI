package musiverification;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.fs.ChronicleMapGroupFS;
import net.openhft.chronicle.engine.fs.FilePerKeyGroupFS;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import net.openhft.lang.thread.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Rob Austin
 */

public class ReplicationTest {

    public static final WireType WIRE_TYPE = WireType.TEXT;
    public static final String NAME = "/ChMaps/test";
    public static ServerEndpoint serverEndpoint1;
    public static ServerEndpoint serverEndpoint2;
    public static ServerEndpoint serverEndpoint3;
    private static AssetTree tree3;
    private static AssetTree tree1;
    private static AssetTree tree2;

    @BeforeClass
    public static void before() throws IOException {
        YamlLogging.clientWrites = true;
        YamlLogging.clientReads = true;

        //YamlLogging.showServerWrites = true;

        ClassAliasPool.CLASS_ALIASES.addAlias(ChronicleMapGroupFS.class);
        ClassAliasPool.CLASS_ALIASES.addAlias(FilePerKeyGroupFS.class);
        //Delete any files from the last run
        Files.deleteIfExists(Paths.get(OS.TARGET, NAME));

        TCPRegistry.createServerSocketChannelFor("host.port1", "host.port2", "host.port3");

        WireType writeType = WireType.TEXT;
        tree1 = create(1, writeType);
        tree2 = create(2, writeType);
        tree3 = create(3, writeType);

        serverEndpoint1 = new ServerEndpoint("host.port1", tree1, writeType);
        serverEndpoint2 = new ServerEndpoint("host.port2", tree2, writeType);
        serverEndpoint3 = new ServerEndpoint("host.port3", tree3, writeType);

    }

    @AfterClass
    public static void after() throws IOException {
        if (serverEndpoint1 != null)
            serverEndpoint1.close();
        if (serverEndpoint2 != null)
            serverEndpoint2.close();
          serverEndpoint3.close();
        if (tree1 != null)
            tree1.close();
        if (tree2 != null)
            tree2.close();
        tree3.close();
        TCPRegistry.reset();
        // TODO TCPRegistery.assertAllServersStopped();
    }

    @NotNull
    private static AssetTree create(final int hostId, Function<Bytes, Wire> writeType) {
        AssetTree tree = new VanillaAssetTree((byte) hostId)
                .forTesting()
                .withConfig(resourcesDir() + "/cmkvst", OS.TARGET + "/" + hostId);

        tree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                VanillaMapView::new,
                KeyValueStore.class);
        tree.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                CMap2EngineReplicator::new);
        tree.root().addLeafRule(KeyValueStore.class, "KVS is Chronicle Map", (context, asset) ->
                new ChronicleMapKeyValueStore(context.wireType(writeType),
                        asset));

        registerTextViewofTree("host " + hostId, tree);

        return tree;
    }


    @Test
    public void test() throws InterruptedException {

        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        assertNotNull(map1);

        final ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        assertNotNull(map2);


        final ConcurrentMap<String, String> map3 = tree3.acquireMap(NAME, String.class, String
                .class);
        assertNotNull(map3);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");
        map3.put("hello3", "world3");

        for (int i = 1; i <= 30; i++) {
            if (map1.size() == 3 && map2.size() == 3 && map3.size() == 3)
                break;
            Jvm.pause(200);
        }


        for (Map m : new Map[]{map1, map2, map3}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals("world3", m.get("hello3"));
            Assert.assertEquals(3, m.size());
        }

    }

    public static String resourcesDir() {
        String path = ReplicationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path == null)
            return ".";
        return new File(path).getParentFile().getParentFile() + "/src/test/resources";
    }

    static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("all-trees-watcher", true));

    public static void registerTextViewofTree(String desc, AssetTree tree) {
        tree.registerSubscriber("", TopologicalEvent.class, e ->
                        // give the collection time to be setup.
                        ses.schedule(() -> handleTreeUpdate(desc, tree, e), 50, TimeUnit.MILLISECONDS)
        );
    }

    static void handleTreeUpdate(String desc, AssetTree tree, TopologicalEvent e) {
        try {
            System.out.println(desc + " handle " + e);
            if (e.added()) {
                System.out.println(desc + " Added a " + e.name() + " under " + e.assetName());
                String assetFullName = e.fullName();
                Asset asset = tree.getAsset(assetFullName);
                if (asset == null) {
                    System.out.println("\tbut it's not visible.");
                    return;
                }
                ObjectKeyValueStore view = asset.getView(ObjectKeyValueStore.class);
                if (view == null) {
                    System.out.println("\t[node]");
                } else {
                    long elements = view.longSize();
                    Class keyType = view.keyType();
                    Class valueType = view.valueType();
                    ObjectKVSSubscription objectKVSSubscription = asset.getView(ObjectKVSSubscription.class);
                    int keySubscriberCount = objectKVSSubscription.keySubscriberCount();
                    int entrySubscriberCount = objectKVSSubscription.entrySubscriberCount();
                    int topicSubscriberCount = objectKVSSubscription.topicSubscriberCount();
                    System.out.println("\t[map]");
                    System.out.printf("\t%-20s %s%n", "keyType", keyType.getName());
                    System.out.printf("\t%-20s %s%n", "valueType", valueType.getName());
                    System.out.printf("\t%-20s %s%n", "size", elements);
                    System.out.printf("\t%-20s %s%n", "keySubscriberCount", keySubscriberCount);
                    System.out.printf("\t%-20s %s%n", "entrySubscriberCount", entrySubscriberCount);
                    System.out.printf("\t%-20s %s%n", "topicSubscriberCount", topicSubscriberCount);
                }
            } else {
                System.out.println(desc + " Removed a " + e.name() + " under " + e.assetName());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}

