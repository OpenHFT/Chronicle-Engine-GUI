package ddp.api.authentication;

import ddp.api.identity.ClientIdentity;
import ddp.api.identity.IdentityProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CredentialsMapAuthenticatorTest
{
    private static Map<String, String> _userPwdMap;
    private static String _testUsername = "TestUser";
    private static String _testUserPwd = "TestPwd";
    private static CredentialsMapAuthenticator _hashedUserPwdMapAuthManager;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        _userPwdMap = new HashMap<>();
        _userPwdMap.put(_testUsername, _testUserPwd);

        _hashedUserPwdMapAuthManager = new CredentialsMapAuthenticator(_userPwdMap);
    }

    /**
     * Test that a valid username and pwd returns true.
     *
     * @
     */
    @Test
    public void testAuthenticateUserExistingUserAndPwd()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertTrue(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user does not exist.
     *
     * @
     */
    @Test
    public void testAuthenticateUserNonExistingUser()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("NonExistingUser", _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user is @null.
     *
     * @
     */
    @Test
    public void testAuthenticateUserNullUser()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(null, _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user is empty string.
     *
     * @
     */
    @Test
    public void testAuthenticateUserEmptyStringUser()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("", _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is wrong.
     *
     * @
     */
    @Test
    public void testAuthenticateUserWrongPassword()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, "WrongPassword", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is null.
     *
     * @
     */
    @Test
    public void testAuthenticateUserNullPwd()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, null, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is empty string.
     *
     * @
     */
    @Test
    public void testAuthenticateUserEmptyStringPwd()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, "", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user and password are both empty strings.
     *
     * @
     */
    @Test
    public void testAuthenticateUserEmptyStringUserAndPwd()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("", "", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user and password are both null.
     *
     * @
     */
    @Test
    public void testAuthenticateUserNullUserAndPwd()
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(null, null, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }
}