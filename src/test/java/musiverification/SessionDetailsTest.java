package musiverification;

import musiverification.helpers.CheckSessionDetailsSubscription;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.map.MapKVSSubscription;
import net.openhft.chronicle.engine.map.ObjectSubscription;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class SessionDetailsTest
{

    public static final WireType WIRE_TYPE = WireType.BINARY;
    public static final String NAME = "/session/details/test";
    public static ServerEndpoint serverEndpoint;
    private static AssetTree serverAssetTree;
    private static VanillaAssetTree remoteAssetTree;

    @NotNull
    private static AssetTree create(final int hostId, Function<Bytes, Wire> writeType, Consumer<AssetTree> applyRules) {
        AssetTree tree = new VanillaAssetTree((byte) hostId)
                .forTesting(Throwable::printStackTrace);

        //Add session detail check wrapper for
        tree.root().addWrappingRule(ObjectSubscription.class, "Check session details subscription",
                CheckSessionDetailsSubscription::new, MapKVSSubscription.class);

        tree.root().addLeafRule(MapKVSSubscription.class, "Chronicle vanilla subscription", MapKVSSubscription::new);

        return tree;
    }

    @Before
    public void before() throws IOException
    {
        resetTrees(null);
    }

    private void resetTrees(Consumer<AssetTree> applyRulesToAllTrees) throws IOException
    {
//        YamlLogging.clientWrites = true;
//        YamlLogging.clientReads = true;
        //YamlLogging.showServerWrites = true;

        TCPRegistry.createServerSocketChannelFor("host.port1");

        serverAssetTree = create(1, WIRE_TYPE, applyRulesToAllTrees);

        remoteAssetTree = new VanillaAssetTree();
        remoteAssetTree.root().forRemoteAccess(new String[]{"host.port1"}, WIRE_TYPE,
                VanillaSessionDetails.of("java-client", null, "java-domain"), null, Throwable::printStackTrace);

        serverEndpoint = new ServerEndpoint("host.port1", serverAssetTree);
    }

    @After
    public void after()
    {
        if (remoteAssetTree != null)
        {
            remoteAssetTree.close();
        }

        if (serverAssetTree != null)
        {
            serverAssetTree.close();
        }

        if (serverEndpoint != null)
        {
            serverEndpoint.close();
        }

        TCPRegistry.reset();

    }

    @Test
    public void test() throws InterruptedException
    {


        final ConcurrentMap<String, String> map1 = remoteAssetTree.acquireMap(NAME, String.class, String
                .class);
        Assert.assertNotNull(map1);

        map1.size();

        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);

        remoteAssetTree.registerSubscriber(NAME, String.class, queue::add);
        Jvm.pause(200);

        String key = "Hello";
        String putValue = "world";

        map1.put(key, putValue);

        String getValue = map1.get(key);

        Assert.assertEquals(putValue, getValue);
        Assert.assertEquals(key, queue.poll(200, TimeUnit.MILLISECONDS));

        System.out.println(getValue);
    }
}