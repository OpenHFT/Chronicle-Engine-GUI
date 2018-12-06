package queue4;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.BinaryWire;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import queue4.chronicle.FromChronicle;
import queue4.chronicle.ToChronicle;
import queue4.externalizableObjects.*;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static net.openhft.chronicle.wire.WireType.TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by cliveh on 10/05/2016.
 */
public class SingleChronicleQueueExternalizableTest {

    String chronicleQueueBase1 = OS.TARGET + "/Chronicle/data1";
    String chronicleQueueBase2 = OS.TARGET + "/Chronicle/data2";

    static void testFor(Object o) {
        final String cs = TEXT.asString(o);
        System.out.println("# testFor\n" + cs);
        final Object o1 = Marshallable.fromString(cs);

        o.equals(o1);
        assertEquals(o, o1);

        Wire wire = new BinaryWire(Bytes.elasticByteBuffer());
        wire.getValueOut()
                .object(o);
        Object o2 = wire.getValueIn()
                .object();
        final String s2 = TEXT.asString(o2);
        final Object o3 = Marshallable.fromString(s2);
        assertEquals(cs, TEXT.asString(o3));
        assertEquals(o, o3);
    }

    private void deleteChronicle(String path) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (int n = 0; n < fileList.length; n++) {
                if (fileList[n].isFile()) {
                    fileList[n].delete();
                }
            }
        }
        file.delete();
    }


    static void deleteDir(@NotNull File dir) {
        if (dir.isDirectory()) {
            @Nullable File[] files = dir.listFiles();
            if (files != null) {
                for (@NotNull File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(file);
                    } else
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                }
            }
        }

        dir.delete();
    }


    @Test
    public void testExternalizable() throws Exception {
        deleteDir(new File(chronicleQueueBase1));

        try (SingleChronicleQueue queue1 = SingleChronicleQueueBuilder.binary(chronicleQueueBase1).build()) {
            ClassAliasPool.CLASS_ALIASES.addAlias(MarketDataKey.class, MarketDataSource.class,
                    MarketDataSupplier.class, MarketDataType.class, MarketDataField.class,
                    InstrumentId.class, Values.class, MarketDataKeyEnvironment.class,
                    MarketDataKeyEnvironments.class, MarketDataKeyEnvironmentsConfig.class);
            // Create externalizable objects to write
            MarketDataKey marketDataKey = new MarketDataKey();
            marketDataKey.set_field(MarketDataField.MID_PRICE);
            marketDataKey.set_marketDataId("9Y.swap");
            marketDataKey.set_producer("executor");
            marketDataKey.set_source(MarketDataSource.BLOOMBERG);
            marketDataKey.set_supplier(MarketDataSupplier.BLOOMBERG);
            marketDataKey.set_type(MarketDataType.BOND);
//        testFor(marketDataKey);

            InstrumentId instrumentId = new InstrumentId();
            instrumentId.set_id(1);
            instrumentId.set_charId('A');
            instrumentId.set_doubleId(1.0);
            instrumentId.set_floatId(1.0f);
            instrumentId.set_longId(1_0);
            instrumentId.set_shortId((short) 1);
            instrumentId.set_stringId("1");
//        testFor(instrumentId);

            Values values = new Values();
            values.set_value1(2.0);
            List<Double> valueList = new ArrayList<>();
            valueList.add(2.1);
            valueList.add(2.2);
            values.set_values(valueList);
//        testFor(values);

            Map<InstrumentId, Values> instrumentIdValuesMap = new LinkedHashMap<>();
            instrumentIdValuesMap.put(instrumentId, values);
//        testFor(instrumentIdValuesMap);

            Map<MarketDataKey, Map<InstrumentId, Values>> instrumentIdValuesByKey = new LinkedHashMap<>();
            instrumentIdValuesByKey.put(marketDataKey, instrumentIdValuesMap);
            testFor(instrumentIdValuesByKey);

            MarketDataKeyEnvironment marketDataKeyEnvironment = new MarketDataKeyEnvironment();
            marketDataKeyEnvironment.set_instrumentIdValuesByKey(instrumentIdValuesByKey);
            testFor(marketDataKeyEnvironment);

            Set<MarketDataKeyEnvironment> marketDataKeyEnvironmentSet = new LinkedHashSet<>();
            marketDataKeyEnvironmentSet.add(marketDataKeyEnvironment);
            testFor(marketDataKeyEnvironmentSet);

            MarketDataKeyEnvironments marketDataKeyEnvironments = new MarketDataKeyEnvironments();
            marketDataKeyEnvironments.set_environments(marketDataKeyEnvironmentSet);
            testFor(marketDataKeyEnvironment);

            MarketDataKeyEnvironmentsConfig config = new MarketDataKeyEnvironmentsConfig();
            config.setId("SomeId");
            config.setExecutor("executor");
            config.setRetransmit(false);
            config.set_marketDataKeyEnvironments(marketDataKeyEnvironments);
            testFor(config);

            EventManager toChronicle = ToChronicle.of(EventManager.class, queue1);
            toChronicle.onConfigAdd("executor", config);
            ExcerptTailer tailer = queue1.createTailer();
            tailer.toStart();

            TestEventManagerMarketDataKeyEnvironmentConfig testEventManager = new TestEventManagerMarketDataKeyEnvironmentConfig(config);
            FromChronicle<TestEventManagerMarketDataKeyEnvironmentConfig> fromChronicle = FromChronicle.of(testEventManager, tailer);
            fromChronicle.readOne();
            queue1.close();
        }
        deleteDir(new File(chronicleQueueBase1));
    }


    @Test
    public void miscellaneousTypeConfigTest() throws Exception {
        deleteDir(new File(chronicleQueueBase2));
        try (SingleChronicleQueue queue2 = SingleChronicleQueueBuilder.binary(chronicleQueueBase2).build()) {

            // Instantiate a MiscellaneousTypesConfig
            MiscellaneousTypesConfig miscellaneousTypesConfig = new MiscellaneousTypesConfig();
            miscellaneousTypesConfig.setId("SomeIdToo");
            miscellaneousTypesConfig.setExecutor("executor");
            miscellaneousTypesConfig.setRetransmit(false);
            miscellaneousTypesConfig.setSystemDate(ZonedDateTime.of(2016, 6, 9, 0, 0, 0, 0, ZoneId.systemDefault()));
            double[] doublePcaMatrixData = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0};
            miscellaneousTypesConfig.setDoublePcaMatrixData(doublePcaMatrixData);
            int[] integerPcaMatrixData = {1, 2, 3, 4, 5, 6, 7};
            miscellaneousTypesConfig.setIntPcaMatrixData(integerPcaMatrixData);
            long[] longPcaMatrixData = {1_0, 2_0, 3_0, 4_0, 5_0, 6_0, 7_0};
            miscellaneousTypesConfig.setLongPcaMatrixData(longPcaMatrixData);
            float[] floatPcaMatrixData = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f};
            miscellaneousTypesConfig.setFloatPcaMatrixData(floatPcaMatrixData);
            boolean[] booleanPcaMatrixData = {true, false, true, false, true, false, true};
            miscellaneousTypesConfig.setIsPcaMatrixData(booleanPcaMatrixData);

            TreeSet<SwapId> orderedTenors = new TreeSet<>();
            orderedTenors.add(new SwapId(1.0));
            orderedTenors.add(new SwapId(2.0));
            orderedTenors.add(new SwapId(3.0));
            orderedTenors.add(new SwapId(4.0));
            orderedTenors.add(new SwapId(5.0));
            orderedTenors.add(new SwapId(6.0));
            miscellaneousTypesConfig.setOrderedTenors(orderedTenors);

            String valuationEnvironment = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n<!DOCTYPE boost_serialization>\n<boost_serialization signature=\"serialization::archive\" version=\"4\">\netc etc";
            miscellaneousTypesConfig.setValuationEnvironment(valuationEnvironment);

            EventManager toChronicle = ToChronicle.of(EventManager.class, queue2);
            toChronicle.onConfigAdd("executor", miscellaneousTypesConfig);
            ExcerptTailer tailer = queue2.createTailer();
            tailer.toStart();

            TestEventManagerMiscellaneousTypesConfig testEventManager = new TestEventManagerMiscellaneousTypesConfig(miscellaneousTypesConfig);
            FromChronicle<TestEventManagerMiscellaneousTypesConfig> fromChronicle = FromChronicle.of(testEventManager, tailer);
            fromChronicle.readOne();
            queue2.close();
        }
        deleteDir(new File(chronicleQueueBase2));
    }


    /**
     * TestEventManager is where the asserts take place
     */
    public class TestEventManagerMarketDataKeyEnvironmentConfig implements EventManager {
        private MarketDataKeyEnvironmentsConfig _config;

        public TestEventManagerMarketDataKeyEnvironmentConfig(MarketDataKeyEnvironmentsConfig config) {
            _config = config;
        }

        @Override
        public String getExecutor() {
            return null;
        }

        @Override
        public void getConfig(String executor) {

        }

        @Override
        public void setConfig(String executor) {

        }

        @Override
        public void onConfigAdd(String executor, ConfigSetting addedConfigSetting) {
            assertEquals("executor", executor);
            assertEquals(TEXT.asString(_config), TEXT.asString(addedConfigSetting));
            Assert.assertNotNull(addedConfigSetting);
            Assert.assertTrue(addedConfigSetting instanceof MarketDataKeyEnvironmentsConfig);
            MarketDataKeyEnvironmentsConfig marketDataKeyEnvironmentsConfig = (MarketDataKeyEnvironmentsConfig) addedConfigSetting;
            Assert.assertNotNull(marketDataKeyEnvironmentsConfig);
            assertEquals(_config.getId(), marketDataKeyEnvironmentsConfig.getId());
            assertEquals(_config.getExecutor(), marketDataKeyEnvironmentsConfig.getExecutor());
            assertEquals(_config.isRetransmit(), marketDataKeyEnvironmentsConfig.isRetransmit());

            MarketDataKeyEnvironments marketDataKeyEnvironments = marketDataKeyEnvironmentsConfig.get_marketDataKeyEnvironments();
            Assert.assertNotNull(marketDataKeyEnvironmentsConfig);
            Set<MarketDataKeyEnvironment> environments = marketDataKeyEnvironments.get_environments();
            Assert.assertNotNull(environments);
            assertEquals(1, environments.size());
            MarketDataKeyEnvironment marketDataKeyEnvironment = environments.stream().findFirst().get();
            Assert.assertNotNull(marketDataKeyEnvironment);
            Map<MarketDataKey, Map<InstrumentId, Values>> instrumentIdValuesByKey = marketDataKeyEnvironment.get_instrumentIdValuesByKey();
            Assert.assertNotNull(instrumentIdValuesByKey);
            assertEquals(1, instrumentIdValuesByKey.size());
            Map.Entry<MarketDataKey, Map<InstrumentId, Values>> marketDataKeyMapEntry = instrumentIdValuesByKey.entrySet().stream().findFirst().get();
            MarketDataKey key = marketDataKeyMapEntry.getKey();
            Assert.assertNotNull(key);

            Map.Entry<MarketDataKey, Map<InstrumentId, Values>> originalMarketDataKeyMapEntry = _config.get_marketDataKeyEnvironments().get_environments().stream().findFirst().get().get_instrumentIdValuesByKey().entrySet().stream().findFirst().get();

            assertEquals(originalMarketDataKeyMapEntry.getKey().get_field(), key.get_field());
            assertEquals(originalMarketDataKeyMapEntry.getKey().get_marketDataId(), key.get_marketDataId());
            assertEquals(originalMarketDataKeyMapEntry.getKey().get_producer(), key.get_producer());
            assertEquals(originalMarketDataKeyMapEntry.getKey().get_source(), key.get_source());
            assertEquals(originalMarketDataKeyMapEntry.getKey().get_type(), key.get_type());
            assertEquals(originalMarketDataKeyMapEntry.getKey().get_supplier(), key.get_supplier());

            Map<InstrumentId, Values> value = marketDataKeyMapEntry.getValue();
            Assert.assertNotNull(value);
            assertEquals(1, value.size());
            Map.Entry<InstrumentId, Values> instrumentIdValuesEntry = value.entrySet().stream().findFirst().get();
            InstrumentId instrumentId = instrumentIdValuesEntry.getKey();
            Assert.assertNotNull(instrumentId);

            Map.Entry<InstrumentId, Values> orginalInstrumentIdValuesEntry = originalMarketDataKeyMapEntry.getValue().entrySet().stream().findFirst().get();
            InstrumentId originalInstrumentId = orginalInstrumentIdValuesEntry.getKey();
            assertEquals(originalInstrumentId.get_id(), instrumentId.get_id());
            assertEquals(originalInstrumentId.get_charId(), instrumentId.get_charId());
            assertEquals(originalInstrumentId.get_doubleId(), instrumentId.get_doubleId(), 0.0001);
            assertEquals(originalInstrumentId.get_floatId(), instrumentId.get_floatId(), 0.0001);
            assertEquals(originalInstrumentId.get_shortId(), instrumentId.get_shortId());
            assertEquals(originalInstrumentId.get_stringId(), instrumentId.get_stringId());

            Values values = instrumentIdValuesEntry.getValue();
            Assert.assertNotNull(values);
            Values orginalValues = orginalInstrumentIdValuesEntry.getValue();
            assertEquals(orginalValues.get_value1(), values.get_value1(), 0.0001);
            Assert.assertNotNull(values.get_values());
            assertEquals(2, values.get_values().size());
            assertEquals(values.get_values().get(0), orginalValues.get_values().get(0));

        }

        @Override
        public void onConfigUpdate(String executor, ConfigSetting updateConfigSetting) {

        }

        @Override
        public void onConfigRemove(String executor, ConfigSetting removedConfigSetting) {

        }

        @Override
        public void getMarketData(String executor) {

        }

        @Override
        public void setMarketData(String executor) {

        }

        @Override
        public void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception {

        }

        @Override
        public void process(String executor) {

        }

        @Override
        public boolean hasChanged() {
            return false;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }
    }

    /**
     * TestEventManager is where the asserts take place
     */
    public class TestEventManagerMiscellaneousTypesConfig implements EventManager {
        private MiscellaneousTypesConfig _config;

        public TestEventManagerMiscellaneousTypesConfig(MiscellaneousTypesConfig config) {
            _config = config;
        }

        @Override
        public String getExecutor() {
            return null;
        }

        @Override
        public void getConfig(String executor) {

        }

        @Override
        public void setConfig(String executor) {

        }

        @Override
        public void onConfigAdd(String executor, ConfigSetting addedConfigSetting) {
            assertEquals("executor", executor);
            assertEquals(TEXT.asString(_config), TEXT.asString(addedConfigSetting));
            Assert.assertNotNull(addedConfigSetting);
            Assert.assertTrue(addedConfigSetting instanceof MiscellaneousTypesConfig);
            MiscellaneousTypesConfig miscellaneousTypesConfig = (MiscellaneousTypesConfig) addedConfigSetting;
            Assert.assertNotNull(miscellaneousTypesConfig);
            assertEquals(_config.getId(), miscellaneousTypesConfig.getId());
            assertEquals(_config.getExecutor(), miscellaneousTypesConfig.getExecutor());
            assertEquals(_config.isRetransmit(), miscellaneousTypesConfig.isRetransmit());

            assertEquals(_config.getSystemDate(), miscellaneousTypesConfig.getSystemDate());
            assertTrue(Arrays.equals(_config.getDoublePcaMatrixData(), miscellaneousTypesConfig.getDoublePcaMatrixData()));
            assertTrue(Arrays.equals(_config.getIntPcaMatrixData(), miscellaneousTypesConfig.getIntPcaMatrixData()));
            assertTrue(Arrays.equals(_config.getLongPcaMatrixData(), miscellaneousTypesConfig.getLongPcaMatrixData()));
            assertTrue(Arrays.equals(_config.getFloatPcaMatrixData(), miscellaneousTypesConfig.getFloatPcaMatrixData()));
            assertTrue(Arrays.equals(_config.getIsPcaMatrixData(), miscellaneousTypesConfig.getIsPcaMatrixData()));

            assertEquals(_config.getOrderedTenors(), miscellaneousTypesConfig.getOrderedTenors());

            assertEquals(_config.getValuationEnvironment(), miscellaneousTypesConfig.getValuationEnvironment());
        }

        @Override
        public void onConfigUpdate(String executor, ConfigSetting updateConfigSetting) {

        }

        @Override
        public void onConfigRemove(String executor, ConfigSetting removedConfigSetting) {

        }

        @Override
        public void getMarketData(String executor) {

        }

        @Override
        public void setMarketData(String executor) {

        }

        @Override
        public void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception {

        }

        @Override
        public void process(String executor) {

        }

        @Override
        public boolean hasChanged() {
            return false;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }
    }
}
