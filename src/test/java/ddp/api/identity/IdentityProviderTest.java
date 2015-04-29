package ddp.api.identity;

import ddp.api.security.*;
import ddp.api.util.*;
import org.junit.*;

public class IdentityProviderTest
{
    /**
     * Test that all fields are set correctly.
     * @throws Exception
     */
    @Test
    public void testGetUserIdentityAllFieldsSetCorrectly() throws Exception
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