package ddp.api.authorisation;

import ddp.api.identity.*;
import ddp.api.security.*;
import org.junit.*;

import java.util.*;

public class EntitlementsManagerTest
{
    private static String _testDataCacheName = "TestDataCache";
    private static String _testUserIdAll = "TestUserAll";
    private static String _testUserPwdAll = "TestUserPwd";
    private static String _testEntity = "London";

    private static ClientIdentity _testClientIdentityAll;
    private static EntitlementsManager _entitlementsManager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        _testClientIdentityAll = IdentityProvider.getClientIdentity(_testUserIdAll, _testUserPwdAll, _testEntity);

        Map<String, Map<String, Entitlement>> testEntitlements = getTestEntitlementsDataCache();

        _entitlementsManager = new EntitlementsManager(testEntitlements);
    }

    /**
     * Test that the user has all the set data and admin access.
     *
     * @throws Exception
     */
    @Test
    public void testGetUserEntitlementsForDataCacheUserIsEntitled() throws Exception
    {
        Entitlement userEntitlements = _entitlementsManager.getUserEntitlementsForDataCache(_testDataCacheName, _testClientIdentityAll);

        Assert.assertNotNull(userEntitlements);
        Assert.assertEquals(_testClientIdentityAll, userEntitlements.getClientIdentity());

        //Check that the user has all data access levels
        for (DataAccessLevel dataAccessLevel : DataAccessLevel.values())
        {
            //This will throw an exception if the user does not have the access
            userEntitlements.checkDataAccess(dataAccessLevel);
        }

        for (AdminAccessLevel adminAccessLevel : AdminAccessLevel.values())
        {
            userEntitlements.checkAdminAccess(adminAccessLevel);
        }
    }

    /**
     * Test that a {@link java.lang.SecurityException} is thrown when the user doesn't have any entitlements (user does not exist in
     * entitlements map).
     *
     * @throws Exception
     */
    @Test(expected = SecurityException.class)
    public void testGetUserEntitlementsForDataCacheUserHasNoEntitlements() throws Exception
    {
        ClientIdentity testClientIdentity = IdentityProvider.getClientIdentity("NotEntitledUser", _testUserPwdAll, _testEntity);

        _entitlementsManager.getUserEntitlementsForDataCache(_testDataCacheName, testClientIdentity);
    }

    /**
     * Test that an {@link java.lang.SecurityException} is thrown when the user does not have any entitlements on the
     * given {@link ddp.api.DataCache}.
     *
     * @throws Exception
     */
    @Test(expected = SecurityException.class)
    public void testGetUserEntitlementsForDataCacheUserHasNoEntitlementsForGivenMap() throws Exception
    {
        _entitlementsManager.getUserEntitlementsForDataCache("RandomDataCache", _testClientIdentityAll);
    }

    /**
     * Get a test entitlements map.
     *
     * @return
     */
    private static Map<String, Map<String, Entitlement>> getTestEntitlementsDataCache()
    {
        //TestUserAll will have all data and admin access
        Set<DataAccessLevel> dataAccessLevels = new HashSet<>();
        for (DataAccessLevel dataAccessLevel : DataAccessLevel.values())
        {
            dataAccessLevels.add(dataAccessLevel);
        }

        Set<AdminAccessLevel> adminAccessLevels = new HashSet<>();
        for (AdminAccessLevel adminAccessLevel : AdminAccessLevel.values())
        {
            adminAccessLevels.add(adminAccessLevel);
        }

        Entitlement entitlement = new Entitlement(_testClientIdentityAll, _testDataCacheName, dataAccessLevels, adminAccessLevels);

        Map<String, Entitlement> mapEntitlements = new HashMap();
        mapEntitlements.put(_testDataCacheName, entitlement);

        Map<String, Map<String, Entitlement>> entitlementsByMap = new HashMap<>();
        entitlementsByMap.put(_testClientIdentityAll.getClientId(), mapEntitlements);

        return entitlementsByMap;
    }
}