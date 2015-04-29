package ddp.api.identity;

import org.junit.*;

import static org.junit.Assert.*;

public class ClientIdentityTest
{
    /**
     * Test whether check against null returns false.
     * Test whether check against incorrect password returns false.
     * Test whether check against actual password returns true.
     * Test whether a client identity which has password set to null always returns false.
     *
     * @throws Exception
     */
    @Test
    public void testCheckPassword() throws Exception
    {
        String clientId = "TestClient";
        String password = "s3cretPwd";
        String entity = "London";
        String hostname = "localhost";

        ClientIdentity clientIdentity = new ClientIdentity(clientId, password, entity, hostname);

        //Test whether check against null returns false
        Assert.assertFalse(clientIdentity.checkPassword(null));

        //Test whether check against incorrect password returns false
        Assert.assertFalse(clientIdentity.checkPassword("NotSoSecretPwd"));

        //Test whether check against actual password returns true
        Assert.assertTrue(clientIdentity.checkPassword(password));


        //Create identity with password set to null.
        ClientIdentity clientIdentityNullPwd = new ClientIdentity(clientId, null, entity, hostname);

        //Test whether a client identity which has password set to null always returns false
        Assert.assertFalse(clientIdentityNullPwd.checkPassword(null));

        Assert.assertFalse(clientIdentityNullPwd.checkPassword("NotSoSecretPwd"));

        Assert.assertFalse(clientIdentityNullPwd.checkPassword(password));
    }
}