package jp.mufg.chronicle.map.size;

import net.openhft.chronicle.map.*;

import java.io.*;

/**
 * Created by daniels on 09/04/2015.
 */
public class FillLargeMaps
{
    public static boolean fillLargeMapWithSmallStrings(){
        System.out.println("Starting fillLargeMapWithSmallStrings");

        String mapName = "LargeMapWithSmallStrings";

        int noOfEntriesExpected = 100000000;
        int noOfEntriesToPut = 150000000;

        String value = "Random String";

        return putValueInMapNoOfTimes(String.class, value, noOfEntriesExpected, noOfEntriesToPut, mapName, value.getBytes().length);
    }

    public static boolean fillLargeMapWithDoubles(){
        System.out.println("Starting fillLargeMapWithDoubles");

        String mapName = "LargeMapWithDoubles";

        int noOfEntriesExpected = 120000000;
        int noOfEntriesToPut = 150000000;

        double value = 2.5;

        return putValueInMapNoOfTimes(Double.class, value, noOfEntriesExpected, noOfEntriesToPut, mapName, 8);
    }

    private static <T> boolean putValueInMapNoOfTimes(Class<T> clazz, T value, int noOfEntriesExpected, int noOfEntriesToPut, String mapName, long valueSizeInBytes)
    {
        System.out.println("Approximate total value size of map (no overhead): "
                + getApproximateTotalSize(valueSizeInBytes, noOfEntriesToPut) + " bytes");

        File mapFile = getFile(mapName);

        int i = 0;
        boolean wasSuccessful = true;
        ChronicleMap<String, T> map = null;

        try
        {
            map = ChronicleMapBuilder
                    .of(String.class, clazz)
                    .entries(noOfEntriesExpected)
                    .averageKeySize(10)
                    .constantValueSizeBySample(value)
                    .createPersistedTo(mapFile);

            System.out.println("No of entries expected " + noOfEntriesExpected);
            System.out.println("Putting " + noOfEntriesToPut + " entries...");

            for (; i < noOfEntriesToPut; i++)
            {
                map.put("" + i, value);
            }

            System.out.println("Keys put!");
            System.out.println("# of keys: " + map.keySet().size());
        }
        catch (Exception e)
        {
            System.out.println("Exception caught. i = " + i);

            System.out.println("Approximate total size of values inserted in bytes (without overhead): " + getApproximateTotalSize(valueSizeInBytes, i));

            e.printStackTrace();

            wasSuccessful = false;
        }
        finally
        {
            System.out.println("Closing map...");

            if(map != null)
            {
                map.close();
            }
        }

        System.out.println("File size in bytes: " + mapFile.length());

        deleteFile(mapFile);

        System.out.println("Finished fillLargeMapWithSmallStrings!");

        return wasSuccessful;
    }

    private static long getApproximateTotalSize(long singleObjectSizeInBytes, int noOfEntriesPut)
    {
        return singleObjectSizeInBytes * noOfEntriesPut;
    }

    private static File getFile(String fileName)
    {
        File file = new File(fileName);
        deleteFile(file);

        return file;
    }

    private static void deleteFile(File file)
    {
        try
        {
            file.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}