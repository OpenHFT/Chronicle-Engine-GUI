package ddp.api.util;

import org.junit.*;

import static org.junit.Assert.*;

public class MachineInfoUtilsTest
{
    /**
     * Simply checks that something is returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetHostname() throws Exception
    {
        String hostname = MachineInfoUtils.getHostname();

        Assert.assertNotNull(hostname);
    }
}