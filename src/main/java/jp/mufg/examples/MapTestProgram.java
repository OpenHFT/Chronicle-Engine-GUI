package jp.mufg.examples;

import com.sun.tools.javac.util.*;
import jp.mufg.api.util.*;
import net.openhft.chronicle.*;
import net.openhft.chronicle.tools.*;
import net.openhft.lang.model.*;

import java.io.*;
import java.util.*;

/**
 * Created by daniels on 20/02/2015.
 */
public class MapTestProgram
{
    public static void main(String[] args)
    {
        //This fails because the DataValueClasses.newInstance cannot create a class which has map or collection. Is this by design?
//        runGeneratedClassWithCollection();


        //This fails due to StreamCorruptedException. I am guessing this is because it cannot parse values with different types (type of Object)
        //Exception in thread "main" java.lang.IllegalStateException: java.lang.IllegalStateException: java.io.StreamCorruptedException: UTF length invalid 84 remaining: 22
//        runMapWriteReadWithObject();


        //This seems to work, presumably because values are of the same type?
//        runMapWriteReadWithString();


        //This seems to work, presumably because values are of the same type?
//        runMapWriteReadWithDouble();


        //This writes the event to the queue twice
//        runMapAsMethodParamStringDouble();


        //This writes the event to the queue twice
        runMapAsMethodParamStringObject();

        //This works just fine
//        runMapAsMethodParamEnumObject();
    }

    //This fails because the DataValueClasses.newInstance cannot create a class which has map or collection. Is this by design?
    private static void runGeneratedClassWithCollection()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ChronicleMapTesterImpl chronicleMapTesterImpl = new ChronicleMapTesterImpl();

            ChronicleMapTester writer = ToChronicle.of(ChronicleMapTester.class, chronicle);
            FromChronicle<ChronicleMapTesterImpl> reader = FromChronicle.of(chronicleMapTesterImpl, chronicle.createTailer());

            MapMarketDataUpdate mapMarketDataUpdate = DataValueClasses.newInstance(MapMarketDataUpdate.class);
            mapMarketDataUpdate.setSource("S");
            mapMarketDataUpdate.setExchange("E");
            mapMarketDataUpdate.setInstrument("I");

            //Put a MapMarketDataUpdate on the queue
            writer.onMapMarketDataUpdate(mapMarketDataUpdate);

            //This should read the MapMarketDataUpdate from the queue and call the implementation
            reader.readOne();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //This fails due to StreamCorruptedException. I am guessing this is because it cannot parse values with different types (type of Object)
    //Exception in thread "main" java.lang.IllegalStateException: java.lang.IllegalStateException: java.io.StreamCorruptedException: UTF length invalid 84 remaining: 22
    private static void runMapWriteReadWithObject()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ExcerptAppender appender = chronicle.createAppender();
            appender.startExcerpt();

            Map<String, Object> putMap = new HashMap<String, Object>();
            putMap.put("TestKey", "TestValue");
            putMap.put("TestKey2", "sdsd");

            appender.writeMap(putMap);

            appender.finish();

            ExcerptTailer tailer = chronicle.createTailer();

            Map<String, Object> newMap = new HashMap<String, Object>();

            if (tailer.nextIndex())
            {

                tailer.readMap(newMap, String.class, Object.class);

                System.out.println("Map: " + newMap);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //This seems to work, presumably because values are of the same type?
    private static void runMapWriteReadWithString()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ExcerptAppender appender = chronicle.createAppender();
            appender.startExcerpt();

            Map<String, String> putMap = new HashMap<String, String>();
            putMap.put("TestKey", "TestValue");
            putMap.put("TestKey2", "sdsd");

            appender.writeMap(putMap);

            appender.finish();

            ExcerptTailer tailer = chronicle.createTailer();

            Map<String, String> newMap = new HashMap<String, String>();

            if (tailer.nextIndex())
            {

                tailer.readMap(newMap, String.class, String.class);

                System.out.println("Map: " + newMap);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //This seems to work, presumably because values are of the same type?
    private static void runMapWriteReadWithDouble()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ExcerptAppender appender = chronicle.createAppender();
            appender.startExcerpt();

            Map<String, Double> putMap = new HashMap<String, Double>();
            putMap.put("TestKey", 1.0);
            putMap.put("TestKey2", 2.0);

            appender.writeMap(putMap);

            appender.finish();

            ExcerptTailer tailer = chronicle.createTailer();

            Map<String, Double> newMap = new HashMap<String, Double>();

            if (tailer.nextIndex())
            {

                tailer.readMap(newMap, String.class, Double.class);

                System.out.println("Map: " + newMap);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //This works just fine
    private static void runMapAsMethodParamStringDouble()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ChronicleMapTesterImpl chronicleMapTesterImpl = new ChronicleMapTesterImpl();

            ChronicleMapTester writer = ToChronicle.of(ChronicleMapTester.class, chronicle);
            FromChronicle<ChronicleMapTesterImpl> reader = FromChronicle.of(chronicleMapTesterImpl, chronicle.createTailer());

            //Call method that has a map as param
            Map<String, Double> mapStringDouble = new HashMap<String, Double>();
            mapStringDouble.put("Key1", 1.0);
            mapStringDouble.put("Key2", 2.0);

            writer.onMarketDataUpdateMapStringDouble("TestString - Map<String, Double>", mapStringDouble);

            //This should read the onMarketDataUpdateMapStringDouble with string and map
            Assert.check(reader.readOne());
            Assert.check(!reader.readOne());
            Assert.check(!reader.readOne());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //This works just fine
    private static void runMapAsMethodParamStringObject()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ChronicleMapTesterImpl chronicleMapTesterImpl = new ChronicleMapTesterImpl();

            ChronicleMapTester writer = ToChronicle.of(ChronicleMapTester.class, chronicle);
            FromChronicle<ChronicleMapTesterImpl> reader = FromChronicle.of(chronicleMapTesterImpl, chronicle.createTailer());

            //Call method that has a map as param
            Map<String, Object> mapStringObject = new HashMap<String, Object>();
            mapStringObject.put("KeyDouble", 1.0);
            mapStringObject.put("KeyValue", "valueString");

            writer.onMarketDataUpdateMapStringObject("TestString - Map<String, Double>", mapStringObject);

            //This should read the onMarketDataUpdateMapStringDouble with string and map
            //Read from queue
            Assert.check(reader.readOne());
            Assert.check(!reader.readOne());
            Assert.check(!reader.readOne());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void runMapAsMethodParamEnumObject()
    {
        try
        {
            String chronicleQueueBase = "C:\\LocalFolder\\Chronicle\\data";
            Chronicle chronicle = ChronicleQueueBuilder.vanilla(chronicleQueueBase).build();
            ChronicleTools.deleteDirOnExit(chronicleQueueBase);

            ChronicleMapTesterImpl chronicleMapTesterImpl = new ChronicleMapTesterImpl();

            ChronicleMapTester writer = ToChronicle.of(ChronicleMapTester.class, chronicle);
            FromChronicle<ChronicleMapTesterImpl> reader = FromChronicle.of(chronicleMapTesterImpl, chronicle.createTailer());

            //Call method that has a map as param
            Map<Enum, Object> mapEnumObject = new HashMap<Enum, Object>();
            mapEnumObject.put(TestEnum.SELECT1, 1.0);
            mapEnumObject.put(TestEnum.RANDOMSELECTION, "valueString");

            writer.onMarketDataUpdateMapEnumObject("TestString - Map<Enum, Double>", mapEnumObject);

            //This should read the onMarketDataUpdateMapStringDouble with string and map
            reader.readOne();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private enum TestEnum
    {
        SELECT1,
        SELECT2,
        RANDOMSELECTION
    }
}