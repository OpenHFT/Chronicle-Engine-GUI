package ddp.api.util;

import ddp.api.*;
import org.junit.*;

import java.util.*;

public class DataCacheConfigUtilsTest
{
    @Test(expected = ConfigurationException.class)
    public void testGetNextIntRollConfigMapNull() throws Exception
    {
        DataCacheConfigUtils.getNextConfigRoll(null, null);
    }

    @Test(expected = ConfigurationException.class)
    public void testGetNextIntRollConfigMapEmpty() throws Exception
    {
        DataCacheConfigUtils.getNextConfigRoll(new HashMap<>(), null);
    }

    @Test
    public void testGetNextIntRollCurrentIndexNullOneConfig() throws Exception
    {
        Map<Integer, DataCacheConfiguration> dataCacheConfigurations = new HashMap<>();
        dataCacheConfigurations.put(1, new DataCacheConfiguration(null, null, (byte) 1, null));

        int nextConfigIndex = DataCacheConfigUtils.getNextConfigRoll(dataCacheConfigurations, null);

        Assert.assertEquals(1, nextConfigIndex);
    }

    @Test
    public void testGetNextIntRollCurrentIndexNullMultipleConfigsRoll() throws Exception
    {
        Map<Integer, DataCacheConfiguration> dataCacheConfigurations = new HashMap<>();
        dataCacheConfigurations.put(1, new DataCacheConfiguration(null, null, (byte) 1, null));
        dataCacheConfigurations.put(2, new DataCacheConfiguration(null, null, (byte) 2, null));
        dataCacheConfigurations.put(3, new DataCacheConfiguration(null, null, (byte) 3, null));

        int nextConfigIndex = DataCacheConfigUtils.getNextConfigRoll(dataCacheConfigurations, null);

        Assert.assertEquals(1, nextConfigIndex);
    }

    @Test
    public void testGetNextIntRollCurrentIndexMiddle() throws Exception
    {
        Map<Integer, DataCacheConfiguration> dataCacheConfigurations = new HashMap<>();
        dataCacheConfigurations.put(1, new DataCacheConfiguration(null, null, (byte) 1, null));
        dataCacheConfigurations.put(2, new DataCacheConfiguration(null, null, (byte) 1, null));
        dataCacheConfigurations.put(3, new DataCacheConfiguration(null, null, (byte) 1, null));

        int nextConfigIndex = DataCacheConfigUtils.getNextConfigRoll(dataCacheConfigurations, 2);

        Assert.assertEquals(3, nextConfigIndex);
    }

    @Test
    public void testGetNextIntRollCurrentIndexLastRoll() throws Exception
    {
        Map<Integer, DataCacheConfiguration> dataCacheConfigurations = new HashMap<>();
        dataCacheConfigurations.put(1, new DataCacheConfiguration(null, null, (byte) 1, null));
        dataCacheConfigurations.put(2, new DataCacheConfiguration(null, null, (byte) 1, null));
        dataCacheConfigurations.put(3, new DataCacheConfiguration(null, null, (byte) 1, null));

        int nextConfigIndex = DataCacheConfigUtils.getNextConfigRoll(dataCacheConfigurations, 3);

        Assert.assertEquals(1, nextConfigIndex);
    }
}