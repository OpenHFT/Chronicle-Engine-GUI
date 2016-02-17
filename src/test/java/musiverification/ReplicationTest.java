package musiverification;

import ddp.api.TestUtils;
import musiverification.helpers.CheckSessionDetailsSubscription;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.fs.ChronicleMapGroupFS;
import net.openhft.chronicle.engine.fs.FilePerKeyGroupFS;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import net.openhft.lang.thread.NamedThreadFactory;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * Created by Rob Austin
 */

public class ReplicationTest {

    public static final WireType WIRE_TYPE = WireType.TEXT;
    public static final String NAME = "/ChMaps/test";
    static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("all-trees-watcher", true));
    public static ServerEndpoint serverEndpoint1;
    public static ServerEndpoint serverEndpoint2;
    public static ServerEndpoint serverEndpoint3;
    private static AssetTree tree3;
    private static AssetTree tree1;
    private static AssetTree tree2;

    @Before
    public void before() throws IOException {
        resetTrees(null);
    }

    private void resetTrees(Consumer<AssetTree> applyRulesToAllTrees) throws IOException {
//        YamlLogging.clientWrites = true;
//        YamlLogging.clientReads = true;

        //YamlLogging.showServerWrites = true;

        ClassAliasPool.CLASS_ALIASES.addAlias(ChronicleMapGroupFS.class);
        ClassAliasPool.CLASS_ALIASES.addAlias(FilePerKeyGroupFS.class);
        //Delete any files from the last run
        TestUtils.deleteRecursive(new File(OS.TARGET, NAME));

        TCPRegistry.createServerSocketChannelFor("host.port1", "host.port2", "host.port3");

        tree1 = create(1, WIRE_TYPE, applyRulesToAllTrees);
        tree2 = create(2, WIRE_TYPE, applyRulesToAllTrees);
        tree3 = create(3, WIRE_TYPE, applyRulesToAllTrees);

        serverEndpoint1 = new ServerEndpoint("host.port1", tree1);
        serverEndpoint2 = new ServerEndpoint("host.port2", tree2);
        serverEndpoint3 = new ServerEndpoint("host.port3", tree3);
    }

    @After
    public void after() {
        if (tree1 != null) {
            tree1.close();
        }
        if (tree2 != null) {
            tree2.close();
        }
        if (tree3 != null) {
            tree3.close();
        }

        if (serverEndpoint1 != null) {
            serverEndpoint1.close();
        }
        if (serverEndpoint2 != null) {
            serverEndpoint2.close();
        }
        if (serverEndpoint3 != null) {
            serverEndpoint3.close();
        }

        TCPRegistry.reset();
        // TODO TCPRegistery.assertAllServersStopped();
        YamlLogging.clientWrites = false;
        YamlLogging.clientReads = false;
    }

    @NotNull
    private static AssetTree create(final int hostId, WireType writeType, Consumer<AssetTree>
            applyRules) {
        AssetTree tree = new VanillaAssetTree((byte) hostId)
                .forTesting(Throwable::printStackTrace)
                .withConfig(resourcesDir() + "/cmkvst", OS.TARGET + "/" + hostId);

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
        String path = ReplicationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path == null) {
            return ".";
        }
        return new File(path).getParentFile().getParentFile() + "/src/test/resources";
    }

//    public static void registerTextViewofTree(String desc, AssetTree tree) {
//        tree.registerSubscriber("", TopologicalEvent.class, e ->
//                        // give the collection time to be setup.
//                        ses.schedule(() -> handleTreeUpdate(desc, tree, e), 50, TimeUnit.MILLISECONDS)
//        );
//    }
//
//    static void handleTreeUpdate(String desc, AssetTree tree, TopologicalEvent e) {
//        try {
//            System.out.println(desc + " handle " + e);
//            if (e.added()) {
//                System.out.println(desc + " Added a " + e.name() + " under " + e.assetName());
//                String assetFullName = e.fullName();
//                Asset asset = tree.getAsset(assetFullName);
//                if (asset == null) {
//                    System.out.println("\tbut it's not visible.");
//                    return;
//                }
//                ObjectKeyValueStore view = asset.getView(ObjectKeyValueStore.class);
//                if (view == null) {
//                    System.out.println("\t[node]");
//                } else {
//                    long elements = view.longSize();
//                    Class keyType = view.keyType();
//                    Class valueType = view.valueType();
//                    ObjectKVSSubscription objectKVSSubscription = asset.getView(ObjectKVSSubscription.class);
//                    int keySubscriberCount = objectKVSSubscription.keySubscriberCount();
//                    int entrySubscriberCount = objectKVSSubscription.entrySubscriberCount();
//                    int topicSubscriberCount = objectKVSSubscription.topicSubscriberCount();
//                    System.out.println("\t[map]");
//                    System.out.printf("\t%-20s %s%n", "keyType", keyType.getName());
//                    System.out.printf("\t%-20s %s%n", "valueType", valueType.getName());
//                    System.out.printf("\t%-20s %s%n", "size", elements);
//                    System.out.printf("\t%-20s %s%n", "keySubscriberCount", keySubscriberCount);
//                    System.out.printf("\t%-20s %s%n", "entrySubscriberCount", entrySubscriberCount);
//                    System.out.printf("\t%-20s %s%n", "topicSubscriberCount", topicSubscriberCount);
//                }
//            } else {
//                System.out.println(desc + " Removed a " + e.name() + " under " + e.assetName());
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }

    @Test
    public void test() throws InterruptedException {

        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        final ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        final ConcurrentMap<String, String> map3 = tree3.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map3);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");
        map3.put("hello3", "world3");

        for (int i = 1; i <= 50; i++) {
            if (map1.size() == 3 && map2.size() == 3 && map3.size() == 3) {
                break;
            }
            Jvm.pause(200);
        }


        for (Map m : new Map[]{map1, map2, map3}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals("world3", m.get("hello3"));
            Assert.assertEquals(3, m.size());
        }

    }

    /**
     * Test that events are only received once and in order
     *
     * @throws InterruptedException
     * @throws InvalidSubscriberException
     */
    @Test
    public void testSubscriptionNoOfEvents() throws InterruptedException, InvalidSubscriberException {
        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        Subscriber<String> subscriberMock = EasyMock.createStrictMock(Subscriber.class);

        tree1.registerSubscriber(NAME + "?bootstrap=false", String.class, subscriberMock);

        Jvm.pause(100);
        final ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        subscriberMock.onMessage("hello1");
        subscriberMock.onMessage("hello2");
        EasyMock.expectLastCall().atLeastOnce();

        EasyMock.replay(subscriberMock);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");

        for (int i = 1; i <= 50; i++) {
            if (map1.size() == 2 && map2.size() == 2) {
                break;
            }
            Jvm.pause(200);
        }

        EasyMock.verify(subscriberMock);

        for (Map m : new Map[]{map1, map2}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals(2, m.size());
        }

        EasyMock.reset(subscriberMock);
    }

    /**
     * Test that session details are set on all replication method calls.
     *
     * @throws InterruptedException
     * @throws InvalidSubscriberException
     */
    @Test
    public void testSessionDetailsSet() throws InterruptedException, InvalidSubscriberException, IOException {
        after();
        resetTrees(this::setSessionDetailsAndTestWrapperOnTree);

        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        Subscriber<String> subscriberMock = EasyMock.createMock(Subscriber.class);

        tree1.registerSubscriber(NAME + "?bootstrap=false", String.class, subscriberMock);

        Jvm.pause(100);
        final ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        Map[] maps = {map1, map2};

        subscriberMock.onMessage("hello1");
        subscriberMock.onMessage("hello2");
//        subscriberMock.onMessage("hello2"); //TODO hack due to multiple events bug

        EasyMock.replay(subscriberMock);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");

        waitForReplication(maps, 2);

        EasyMock.verify(subscriberMock);
        EasyMock.reset(subscriberMock); //HACK for endOfSubscription event

        for (Map m : new Map[]{map1, map2}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals(2, m.size());
        }
    }

    /**
     * Test that maps are re-replicated when the connection is lost and regained.
     *
     * @throws InterruptedException
     * @throws InvalidSubscriberException
     * @throws IOException
     */
    @Test
    public void testReplicationOnReconnect() throws InterruptedException, InvalidSubscriberException, IOException {
        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");

        waitForReplication(new Map[]{map1, map2}, 2);

        for (Map m : new Map[]{map1, map2}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals(2, m.size());
        }

        tree2.close();
        serverEndpoint2.close();
        tree2 = create(2, WIRE_TYPE, null);

        serverEndpoint2 = new ServerEndpoint("host.port2", tree2);

        map1.put("Map1NonRep", "NonRepValue");

        map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        waitForReplication(new Map[]{map2}, map1.size());

        Assert.assertEquals("NonRepValue", map2.get("Map1NonRep"));
        Assert.assertEquals("world1", map2.get("hello1"));
        Assert.assertEquals("world2", map2.get("hello2"));
        Assert.assertEquals(3, map2.size());
    }

    private void waitForReplication(Map[] maps, int mapSize) {
        for (int i = 1; i <= 50; i++) {
            boolean allMapsReplicated = true;

            for (Map m : maps) {
                if (m.size() != mapSize) {
                    allMapsReplicated = false;
                }
            }

            if (allMapsReplicated) {
                break;
            }

            Jvm.pause(200);
        }
    }

    private void setSessionDetailsAndTestWrapperOnTree(AssetTree assetTree) {
        SessionProvider sessionProvider = assetTree.root().acquireView(SessionProvider.class);
        VanillaSessionDetails vanillaSessionDetails = VanillaSessionDetails.of("testUser", null, "");
        sessionProvider.set(vanillaSessionDetails);

        assetTree.root().addWrappingRule(ObjectSubscription.class, "Check session details subscription",
                CheckSessionDetailsSubscription::new, MapKVSSubscription.class);

        assetTree.root().addLeafRule(MapKVSSubscription.class, "Chronicle vanilla subscription", MapKVSSubscription::new);
    }
}