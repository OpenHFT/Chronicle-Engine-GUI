package ddp.api.authorisation;

import ddp.api.identity.ClientIdentity;
import ddp.api.identity.IdentityProvider;
import ddp.api.security.AdminAccessLevel;
import ddp.api.security.DataAccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages entitlements.
 */
public class EntitlementsManager
{
    private static final Logger _logger = LoggerFactory.getLogger(EntitlementsManager.class);

    private Map<String, Map<String, Entitlement>> _entitlementsCache;

    /**
     * Create manager with entitlements map.
     * @param entitlementsCache Map of maps with entitlements. Map<UserId, Map<DataCacheName, Entitlement>>
     */
    public EntitlementsManager(Map<String, Map<String, Entitlement>> entitlementsCache)
    {
        _entitlementsCache = entitlementsCache;
    }

    //TODO DS this should be removed
    public EntitlementsManager()
    {
        _entitlementsCache = getEntitlementsDataCache();
    }

    /**
     * Get entitlement for the given user identity on the Data Cache with the given fullName.
     * @param dataCacheName Name of data cache to get user entitlement for.
     * @param clientIdentity Identity for which to get entitlement on given Data Cache.
     * @return The giver users entitlement on the given Data Cache.
     */
    public Entitlement getUserEntitlementsForDataCache(String dataCacheName, ClientIdentity clientIdentity)
    {
        Map<String, Entitlement> userEntitlements = _entitlementsCache.get(clientIdentity.getClientId());

        if(userEntitlements == null)
        {
            String errorMessage = String.format("Client '%s' does not have any Data Cache entitlements!", clientIdentity.getClientId());

            _logger.error(errorMessage);

            throw new SecurityException(errorMessage);
        }

        Entitlement entitlement = userEntitlements.get(dataCacheName);

        if(entitlement == null)
        {
            String errorMessage = String.format("Client with identity '%s' does not have any entitlements on Data Cache '%s'.",
                    clientIdentity.toString(),
                    dataCacheName);

            _logger.error(errorMessage);

            throw new SecurityException(errorMessage);
        }

        return entitlement;
    }

    //TODO DS temporary hack until Chronicle Map supports collections within maps!
    private Map<String, Map<String, Entitlement>> getEntitlementsDataCache()
    {
        String dataCacheName = "ExampleDataCache";
        Map<String, Map<String, Entitlement>> entitlementsByMap = new HashMap<>();

        ClientIdentity clientIdentity = IdentityProvider.getClientIdentity("ExampleUser", "ExamplePwd", "MUSI");

        Set<DataAccessLevel> dataAccessLevels = new HashSet<>();
        dataAccessLevels.add(DataAccessLevel.LOCK);
        dataAccessLevels.add(DataAccessLevel.READ);
        dataAccessLevels.add(DataAccessLevel.SUBSCRIBE);
        dataAccessLevels.add(DataAccessLevel.WRITE);

        Set<AdminAccessLevel> adminAccessLevels = new HashSet<>();
        adminAccessLevels.add(AdminAccessLevel.CREATE);

        Entitlement entitlement = new Entitlement(clientIdentity, dataCacheName, dataAccessLevels, adminAccessLevels);

        Map<String, Entitlement> mapEntitlements = new HashMap();
        mapEntitlements.put(dataCacheName, entitlement);

        entitlementsByMap.put("ExampleUser", mapEntitlements);

        return entitlementsByMap;
    }
}