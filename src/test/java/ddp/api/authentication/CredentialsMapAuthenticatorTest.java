package ddp.api.authentication;

import ddp.api.identity.*;
import org.junit.*;

import java.util.*;

public class CredentialsMapAuthenticatorTest
{
    private static Map<String, String> _userPwdMap;
    private static String _testUsername = "TestUser";
    private static String _testUserPwd = "TestPwd";
    private static CredentialsMapAuthenticator _hashedUserPwdMapAuthManager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        _userPwdMap = new HashMap<>();
        _userPwdMap.put(_testUsername, _testUserPwd);

        _hashedUserPwdMapAuthManager = new CredentialsMapAuthenticator(_userPwdMap);
    }

    /**
     * Test that a valid username and pwd returns true.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserExistingUserAndPwd() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertTrue(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user does not exist.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserNonExistingUser() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("NonExistingUser", _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user is @null.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserNullUser() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(null, _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user is empty string.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserEmptyStringUser() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("", _testUserPwd, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is wrong.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserWrongPassword() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, "WrongPassword", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is null.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserNullPwd() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, null, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user exist, but the password is empty string.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserEmptyStringPwd() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(_testUsername, "", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user and password are both empty strings.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserEmptyStringUserAndPwd() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("", "", null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }

    /**
     * Test that false is returned when the user and password are both null.
     *
     * @throws Exception
     */
    @Test
    public void testAuthenticateUserNullUserAndPwd() throws Exception
    {
        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity(null, null, null);

        boolean isUserAuthenticated = _hashedUserPwdMapAuthManager.authenticateClient(clientIdentity);

        Assert.assertFalse(isUserAuthenticated);
    }
}