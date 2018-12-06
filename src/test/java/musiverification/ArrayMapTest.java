package musiverification;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapEventListener;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.map.CMap2EngineReplicator;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.InsertedEvent;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.network.connection.TcpChannelHub;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * test using the listener both remotely or locally via the engine
 *
 * @author Rob Austin.
 */
@RunWith(value = Parameterized.class)
public class ArrayMapTest {
    private static final String NAME = "test";
    private static MapView<String, byte[]> map;
    private static AtomicReference<Throwable> t = new AtomicReference();
    private final Boolean isRemote;
    private final WireType wireType;
    public String connection = "ArrayMapTest.host.port";
    @NotNull
    @Rule
    public TestName name = new TestName();
    private AssetTree assetTree = new VanillaAssetTree(1).forServer(true);
    private VanillaAssetTree serverAssetTree;
    private ServerEndpoint serverEndpoint;

    public ArrayMapTest(boolean isRemote, WireType wireType) {
        this.isRemote = isRemote;
        this.wireType = wireType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(
                new Object[]{false, null}
                , new Object[]{true, WireType.TEXT}
                , new Object[]{true, WireType.BINARY}
        );
    }

    @After
    public void afterMethod() {
        final Throwable th = t.getAndSet(null);
        if (th != null) throw Jvm.rethrow(th);
    }

    @Before
    public void before() throws IOException {
        serverAssetTree = new VanillaAssetTree(2).forServer(true);

        if (isRemote) {

            serverAssetTree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                    VanillaMapView::new,
                    KeyValueStore.class);
            serverAssetTree.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                    CMap2EngineReplicator::new);
            serverAssetTree.root().addLeafRule(KeyValueStore.class, "KVS is Chronicle Map", (context, asset) ->
                    new ChronicleMapKeyValueStore(context.wireType(wireType).putReturnsNull(false),
                            asset));

            connection = "ArrayMapTest." + name.getMethodName() + ".host.port";
            TCPRegistry.createServerSocketChannelFor(connection);
            serverEndpoint = new ServerEndpoint(connection, serverAssetTree, "cluster");
            assetTree = new VanillaAssetTree().forRemoteAccess(connection, wireType, x -> t.set(x));
        } else {
            assetTree = serverAssetTree;
        }

        map = assetTree.acquireMap(NAME, String.class, byte[].class);

    }

    @After
    public void after() throws IOException {

        serverAssetTree.close();
        if (serverEndpoint != null)
            serverEndpoint.close();

        assetTree.close();

        if (map instanceof Closeable)
            ((Closeable) map).close();
        TcpChannelHub.closeAllHubs();
        TCPRegistry.reset();
    }

    @Test
    public void testByteArrayValue() {

        final MapView<String, byte[]> map = assetTree.acquireMap("name", String.class, byte[]
                .class);
        map.put("1", "hello world".getBytes(ISO_8859_1));

        final byte[] bytes = map.get("1");
        Assert.assertArrayEquals("hello world".getBytes(ISO_8859_1), bytes);

    }

    @Test
    public void testByteArrayValueWithRealBytes() {

        final MapView<String, byte[]> map = assetTree.acquireMap("name", String.class, byte[]
                .class);

        final byte[] expected = {1, 2, 3, 4, 5, 6, 7};
        map.put("1", expected);

        final byte[] actual = map.get("1");
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testByteArrayValueWithRealBytesNegative() {

        final MapView<String, byte[]> map = assetTree.acquireMap("name", String.class, byte[]
                .class);

        final byte[] expected = {-1, -2, -3, -4, -5, -6, -7};
        map.put("1", expected);

        final byte[] actual = map.get("1");
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testByteArrayMapEventSubscription() throws InterruptedException {

        BlockingQueue<MapEvent> mapEventQueue = new ArrayBlockingQueue<>(1);

        final MapView<String, byte[]> map = assetTree.acquireMap("name", String.class, byte[]
                .class);

        assetTree.registerSubscriber("name?bootstrap=false", MapEvent.class, mapEventQueue::add);

        String key = "k1";
        final byte[] expected = {1, 2, 3, 4, 5, 6, 7};
        map.put(key, expected);

        final byte[] actual = map.get(key);
        Assert.assertArrayEquals(expected, actual);

        MapEvent newValueMapEvent = mapEventQueue.poll(200, TimeUnit.MILLISECONDS);

        System.out.println("#########: " + newValueMapEvent.getValue().getClass());

        Assert.assertTrue(newValueMapEvent instanceof InsertedEvent);
        Assert.assertEquals(key, newValueMapEvent.getKey());
        Assert.assertArrayEquals(expected, (byte[]) newValueMapEvent.getValue());
    }

    @Test
    public void testByteArrayMapEventSubscriptionTyped() throws InterruptedException {

        BlockingQueue<byte[]> mapEventQueue = new ArrayBlockingQueue<>(1);

        final MapView<String, byte[]> map = assetTree.acquireMap("name", String.class, byte[]
                .class);

        MapEventListener<String, byte[]> mapEventListenerTyped = (assetName, key1, oldValue, newValue) -> mapEventQueue.add(newValue);

        assetTree.registerSubscriber("name?bootstrap=false", MapEvent.class, e -> e.apply(mapEventListenerTyped));

        String key = "k1";
        final byte[] expected = {1, 2, 3, 4, 5, 6, 7};
        map.put(key, expected);

        final byte[] actual = map.get(key);
        Assert.assertArrayEquals(expected, actual);

        byte[] newValueEvent = mapEventQueue.poll(200, TimeUnit.MILLISECONDS);

        Assert.assertArrayEquals(expected, newValueEvent);
    }
}

