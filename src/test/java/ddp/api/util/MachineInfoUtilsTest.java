package ddp.api.util;

import org.junit.Assert;
import org.junit.Test;

public class MachineInfoUtilsTest
{
    /**
     * Simply checks that something is returned.
     *
     * @
     */
    @Test
    public void testGetHostname()
    {
        String hostname = MachineInfoUtils.getHostname();

        Assert.assertNotNull(hostname);
    }
}