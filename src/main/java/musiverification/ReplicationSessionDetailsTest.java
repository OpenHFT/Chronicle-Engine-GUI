package musiverification;

import musiverification.helpers.*;
import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.core.*;
import net.openhft.chronicle.core.pool.*;
import net.openhft.chronicle.engine.api.*;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.fs.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.server.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.*;
import net.openhft.chronicle.network.api.session.*;
import net.openhft.chronicle.wire.*;
import net.openhft.lang.thread.*;
import org.easymock.*;
import org.jetbrains.annotations.*;
import org.junit.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Created by daniels on 15/09/2015.
 */
public class ReplicationSessionDetailsTest
{
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

    @BeforeClass
    public static void before() throws IOException
    {
//        YamlLogging.clientWrites = true;
//        YamlLogging.clientReads = true;

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
    public static void after() {

        if (tree1 != null)
            tree1.close();
        if (tree2 != null)
            tree2.close();
        if (tree3 != null)
            tree3.close();

        if (serverEndpoint1 != null)
            serverEndpoint1.close();
        if (serverEndpoint2 != null)
            serverEndpoint2.close();
        if (serverEndpoint3 != null)
            serverEndpoint3.close();

        TCPRegistry.reset();
        // TODO TCPRegistery.assertAllServersStopped();
        YamlLogging.clientWrites = false;
        YamlLogging.clientReads = false;
    }

    @NotNull
    private static AssetTree create(final int hostId, Function<Bytes, Wire> writeType) {
        AssetTree tree = new VanillaAssetTree((byte) hostId)
                .forTesting()
                .withConfig(resourcesDir() + "/cmkvst", OS.TARGET + "/" + hostId);


        SessionProvider sessionProvider = tree.root().acquireView(SessionProvider.class);
        VanillaSessionDetails vanillaSessionDetails = VanillaSessionDetails.of("testUser", null);
        sessionProvider.set(vanillaSessionDetails);



        tree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                VanillaMapView::new,
                KeyValueStore.class);
        tree.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                CMap2EngineReplicator::new);
        tree.root().addLeafRule(KeyValueStore.class, "KVS is Chronicle Map", (context, asset) ->
                new ChronicleMapKeyValueStore(context.wireType(writeType).putReturnsNull(false),
                        asset));

//        tree.root().addWrappingRule(AuthenticatedKeyValueStore.class, "Check session details store",
//                CheckSessionDetailsKeyValueStore::new, VanillaKeyValueStore.class);



        tree.root().addWrappingRule(ObjectKVSSubscription.class, "Check session details subscription",
                CheckSessionDetailsSubscription::new, VanillaKVSSubscription.class);

        tree.root().addLeafRule(VanillaKVSSubscription.class, "Chronicle vanilla subscription", VanillaKVSSubscription::new);

        return tree;
    }

    public static String resourcesDir() {
        String path = ReplicationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path == null)
            return ".";
        return new File(path).getParentFile().getParentFile() + "/src/test/resources";
    }

    @Test
    public void test() throws InterruptedException, InvalidSubscriberException
    {

        final ConcurrentMap<String, String> map1 = tree1.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        Subscriber<String> subscriberMock = EasyMock.createMock(Subscriber.class);

        tree1.registerSubscriber(NAME, String.class, subscriberMock);

        final ConcurrentMap<String, String> map2 = tree2.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map2);

        subscriberMock.onMessage("hello1");
        subscriberMock.onMessage("hello2");
        subscriberMock.onMessage("hello2");

        EasyMock.replay(subscriberMock);

        map1.put("hello1", "world1");
        map2.put("hello2", "world2");

        for (int i = 1; i <= 50; i++) {
            if (map1.size() == 2 && map2.size() == 2)
                break;
            Jvm.pause(200);
        }

        EasyMock.verify(subscriberMock);
        EasyMock.reset(subscriberMock); //HACK for endOfSubscription event

        for (Map m : new Map[]{map1, map2}) {
            Assert.assertEquals("world1", m.get("hello1"));
            Assert.assertEquals("world2", m.get("hello2"));
            Assert.assertEquals(2, m.size());
        }
    }
}