package jp.mufg.chronicle.map.size;

import org.junit.*;

import static org.junit.Assert.*;

public class FillLargeMapsTest
{
    @Test
    public void testFillLargeMapWithSmallStrings() throws Exception
    {
        Assert.assertTrue(FillLargeMaps.fillLargeMapWithSmallStrings());
    }

    @Test
    public void testFillLargeMapWithDoubles() throws Exception
    {
        Assert.assertTrue(FillLargeMaps.fillLargeMapWithDoubles());
    }
}