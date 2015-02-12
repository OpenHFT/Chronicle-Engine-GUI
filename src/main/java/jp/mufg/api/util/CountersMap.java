package jp.mufg.api.util;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.values.LongValue;

import java.io.File;
import java.io.IOException;

public class CountersMap {
    final ChronicleMap<String, LongValue> counters;

    public CountersMap(String fileName) throws IOException {
        counters = ChronicleMapBuilder
                .of(String.class, LongValue.class)
                .actualSegments(1)
                .averageKeySize(16)
                .entries(100)
                .createPersistedTo(new File(fileName));
    }

    public LongValue acquireCounter(String name) {
        return counters.acquireUsing(name, null);
    }

    public void close() {
        counters.close();
    }
}
