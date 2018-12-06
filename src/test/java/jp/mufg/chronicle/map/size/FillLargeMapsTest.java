package jp.mufg.chronicle.map.size;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore("Performance test")
public class FillLargeMapsTest
{
    @Test
    public void testFillLargeMapWithSmallStrings() throws IOException {
        FillLargeMaps.fillLargeMapWithSmallStrings(true);
    }

    @Test
    public void testFillLargeMapWithDoubles() throws IOException {
        FillLargeMaps.fillLargeMapWithDoubles(true);
    }
}