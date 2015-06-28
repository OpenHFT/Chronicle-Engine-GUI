package ddp.api.identity;

import ddp.api.security.PasswordHasher;
import ddp.api.util.MachineInfoUtils;
import org.junit.Assert;
import org.junit.Test;

public class IdentityProviderTest
{
    /**
     * Test that all fields are set correctly.
     * @
     */
    @Test
    public void testGetUserIdentityAllFieldsSetCorrectly()
    {
        String username = "TestUser";
        String password = "TestPassword";
        String entity = "TestEntity";

        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(username, password, entity);

        Assert.assertNotNull(clientIdentity);
        Assert.assertEquals(username, clientIdentity.getClientId());
        //Password should be hashed before being set on the identity object
        Assert.assertTrue(clientIdentity.checkPassword(PasswordHasher.getPasswordHash(password)));
        Assert.assertEquals(entity, clientIdentity.getEntity());
        Assert.assertEquals(MachineInfoUtils.getHostname(), clientIdentity.getHostname());
    }
}