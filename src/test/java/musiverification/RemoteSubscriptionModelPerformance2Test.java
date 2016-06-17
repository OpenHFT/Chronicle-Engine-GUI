/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.KVSSubscription;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.IntStream;

public class RemoteSubscriptionModelPerformance2Test {

    //TODO DS test having the server side on another machine
    private static final int _noOfPuts = 50;
    private static final int _noOfRunsToAverage = Boolean.getBoolean("quick") ? 2 : 10;
    // TODO Fix so that it is 1 second. CHENT-49
    private static final long _secondInNanos = 6_500_000_000L;
    private static final AtomicInteger counter = new AtomicInteger();
    private static final String _testStringFilePath = "Vols" + File.separator + "USDVolValEnvOIS-BO.xml";
    private static String _twoMbTestString;
    private static int _twoMbTestStringLength;
    private static Map<String, String> _testMap;
    private static VanillaAssetTree serverAssetTree, clientAssetTree;
    private static ServerEndpoint serverEndpoint;

    private final String _mapName = "PerfTestMap" + System.nanoTime();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException, URISyntaxException {
        YamlLogging.setAll(false);
        _twoMbTestString = TestUtils.loadSystemResourceFileToString(_testStringFilePath); //.substring(1, 1 << 10);
        _twoMbTestStringLength = _twoMbTestString.length();

        serverAssetTree = new VanillaAssetTree(1).forServer(true);
        //The following line doesn't add anything and breaks subscriptions
        serverAssetTree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore", VanillaMapView::new, KeyValueStore.class);
        serverAssetTree.root().addLeafRule(KeyValueStore.class, "use Chronicle Map", (context, asset) ->
                new ChronicleMapKeyValueStore(context.basePath(OS.TARGET + "/RemoteSubscriptionModelPerformance2Test").entries(_noOfPuts).putReturnsNull(false).averageValueSize(_twoMbTestStringLength), asset));
        TCPRegistry.createServerSocketChannelFor("RemoteSubscriptionModelPerformanceTest.port");
        serverEndpoint = new ServerEndpoint("RemoteSubscriptionModelPerformanceTest.port", serverAssetTree);

        clientAssetTree = new VanillaAssetTree(13).forRemoteAccess
                ("RemoteSubscriptionModelPerformanceTest.port", WireType.BINARY, Throwable::printStackTrace);
    }

    @AfterClass
    public static void tearDownAfterClass() throws IOException {
        clientAssetTree.close();
        serverEndpoint.close();
        serverAssetTree.close();
        TCPRegistry.reset();
    }

    @Before
    public void setUp() throws IOException {
        TestUtils.deleteRecursive(new File(OS.TARGET, _mapName));

        _testMap = clientAssetTree.acquireMap(_mapName, String.class, String.class);

        _testMap.clear();
    }

    @After
    public void tearDown() throws IOException {
    }

    /**
     * Tests the performance of an event listener on the map for Update events of 2 MB strings.
     * Expect it to handle at least 50 2 MB updates per second.
     */
    @Test
    @Ignore("TODO FIX")
    public void testSubscriptionMapEventListenerUpdatePerformance() {
        //Put values before testing as we want to ignore the insert events
        Function<Integer, Object> putFunction = a -> _testMap.put(TestUtils.getKey(_mapName, a), _twoMbTestString);

        IntStream.range(0, _noOfPuts).forEach(i ->
        {
            putFunction.apply(i);
        });

        Jvm.pause(100);
        //Create subscriber and register
        TestChronicleMapEventListener mapEventListener = new TestChronicleMapEventListener(_mapName, _twoMbTestStringLength);

        Subscriber<MapEvent> mapEventSubscriber = e -> e.apply(mapEventListener);
        clientAssetTree.registerSubscriber(_mapName + "?bootstrap=false", MapEvent.class, mapEventSubscriber);

        KVSSubscription subscription = (KVSSubscription) serverAssetTree.getAsset(_mapName).subscription(false);

        waitFor(() -> subscription.entrySubscriberCount() == 1);
        Assert.assertEquals(1, subscription.entrySubscriberCount());

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(i -> {
                    if (i > 0) {
                        waitFor(() -> mapEventListener.getNoOfUpdateEvents().get() >= _noOfPuts);

                        //Test that the correct number of events were triggered on event listener
                        Assert.assertEquals(_noOfPuts, mapEventListener.getNoOfUpdateEvents().get());
                    }
                    Assert.assertEquals(0, mapEventListener.getNoOfInsertEvents().get());
                    Assert.assertEquals(0, mapEventListener.getNoOfRemoveEvents().get());

                    mapEventListener.resetCounters();

                }, () -> {
                    IntStream.range(0, _noOfPuts).forEach(i ->
                    {
                        putFunction.apply(i);
                    });
                }, _noOfRunsToAverage, _secondInNanos
        );
        clientAssetTree.unregisterSubscriber(_mapName, mapEventSubscriber);

        waitFor(() -> subscription.entrySubscriberCount() == 0);
        Assert.assertEquals(0, subscription.entrySubscriberCount());
    }

    private void waitFor(BooleanSupplier b) {
        for (int i = 1; i <= 40; i++)
            if (!b.getAsBoolean())
                Jvm.pause(i * i);
    }
}