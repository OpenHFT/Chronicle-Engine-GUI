package jp.mufg.chronicle.map.size;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.Jvm;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Created by daniels on 09/04/2015.
 */
public class FillLargeMaps {
    static final int noOfEntriesToPut = 260000000;
    static final int noOfEntriesExpected = noOfEntriesToPut;

    public static void fillLargeMapWithSmallStrings(boolean deleteMapOnExit) throws IOException {
        System.out.println("Starting fillLargeMapWithSmallStrings");

        String mapName = "LargeMapWithSmallStrings";

        String value = "Random String";

        putValueInMapNoOfTimes(String.class, value, noOfEntriesExpected, noOfEntriesToPut, mapName, value.getBytes().length, deleteMapOnExit);
    }

    public static void fillLargeMapWithDoubles(boolean deleteMapOnExit) throws IOException {
        System.out.println("Starting fillLargeMapWithDoubles");

        String mapName = "LargeMapWithDoubles";

        double value = 2.5;

        putValueInMapNoOfTimes(Double.class, value, noOfEntriesExpected, noOfEntriesToPut, mapName, 8, deleteMapOnExit);
    }

    private static <T> void putValueInMapNoOfTimes(Class<T> clazz, T value, int noOfEntriesExpected, int noOfEntriesToPut, String mapName, long valueSizeInBytes, boolean deleteMapOnExit) throws IOException {
        System.out.println("Approximate total value size of map (no overhead): "
                + getApproximateTotalSize(valueSizeInBytes, noOfEntriesToPut) + " bytes");

        File mapFile = getFile(mapName);

        ChronicleMap<String, T> map = null;
        long startTime = System.currentTimeMillis();
        try {
            map = ChronicleMapBuilder
                    .of(String.class, clazz)
                    .entries(noOfEntriesExpected)
                    .putReturnsNull(true)
                    .removeReturnsNull(true)
                    .averageKeySize(10)
                    .constantValueSizeBySample(value)
                    .createPersistedTo(mapFile);
            try {
                System.out.println("No of entries expected " + noOfEntriesExpected);
                System.out.println("Putting " + noOfEntriesToPut + " entries...");

                ChronicleMap<String, T> map2 = map;
                IntStream.range(0, noOfEntriesToPut).parallel()
                        .forEach(i -> map2.put(Integer.toString(i), value));

                System.out.println("Keys put!, # of keys: " + map.size());
            } catch (Exception e) {
                int size = map.size();
                System.out.println("Exception caught. size = " + size);

                System.out.printf("Approximate total size of values inserted in bytes (without overhead): %,d%n",
                        getApproximateTotalSize(valueSizeInBytes, size));
                throw e;
            }
        } finally {
            System.out.println("Closing map...");

            if (map != null) {
                map.close();
            }
        }

        System.out.println("File size in bytes: " + mapFile.length());

        if (deleteMapOnExit) {
            mapFile.delete();
        }
        long rate = noOfEntriesToPut * 1000L / (System.currentTimeMillis() - startTime);
        System.out.printf("%nput rate was %.3f million/sec%n%n", rate / 1e6);
    }

    private static long getApproximateTotalSize(long singleObjectSizeInBytes, int noOfEntriesPut) {
        return singleObjectSizeInBytes * noOfEntriesPut;
    }

    private static File getFile(String fileName) {
        File file = new File(Jvm.TMP + "/" + fileName);
        file.delete();
        return file;
    }
}