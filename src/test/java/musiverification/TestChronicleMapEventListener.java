package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.engine.api.map.MapEventListener;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Map event listener for performance testing. Checks that the key is the one expected and the size of the value is
 * as expected.
 * Increments event specific counters that can be used to check agains the expected number of events.
 */
class TestChronicleMapEventListener implements MapEventListener<String, String> {
    private AtomicInteger _noOfInsertEvents = new AtomicInteger(0);
    private AtomicInteger _noOfUpdateEvents = new AtomicInteger(0);
    private AtomicInteger _noOfRemoveEvents = new AtomicInteger(0);

    private String _mapName;
    private int _stringLength;

    public TestChronicleMapEventListener(String mapName, int stringLength) {
        _mapName = mapName;
        _stringLength = stringLength;
    }

    @Override
    public void update(String assetName, String key, String oldValue, String newValue) {
        testKeyAndValue(key, newValue, _noOfUpdateEvents);
    }

    @Override
    public void insert(String assetName, String key, String value) {
        testKeyAndValue(key, value, _noOfInsertEvents);
    }

    @Override
    public void remove(String assetName, String key, String value) {
        testKeyAndValue(key, value, _noOfRemoveEvents);
    }

    public AtomicInteger getNoOfInsertEvents() {
        return _noOfInsertEvents;
    }

    public AtomicInteger getNoOfUpdateEvents() {
        return _noOfUpdateEvents;
    }

    public AtomicInteger getNoOfRemoveEvents() {
        return _noOfRemoveEvents;
    }

    public void resetCounters() {
        _noOfInsertEvents = new AtomicInteger(0);
        _noOfUpdateEvents = new AtomicInteger(0);
        _noOfRemoveEvents = new AtomicInteger(0);
    }

    private void testKeyAndValue(String key, String value, AtomicInteger counterToIncrement) {
        int counter = counterToIncrement.getAndIncrement();
        Assert.assertEquals(TestUtils.getKey(_mapName, counter), key);
        Assert.assertEquals(_stringLength, value.length());
    }
}
