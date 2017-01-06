package ddp.api.authorisation;

import net.openhft.chronicle.engine.api.tree.AssetTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import java.util.*;
import java.util.function.*;

import net.openhft.chronicle.engine.api.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PermissionAuthorizationManager} using a Chronicle replicated map to store permissions. Updates are
 * replicated to other servers (assuming asset trees are configured correctly).
 * <p>
 * The current implementation uses a ChronicleMap<domain, Map<permissions...> which means all permissions for each
 * domain is serialized as replicated as one event to all servers. This causes an obvious race condition if two servers
 * make changes at the same time one of the changes will not be applied. Therefore, with this implementation the map
 * should only be updated from one server and let that server replicate the changes to all other servers.
 * <p>
 * This is lazily thread-safe, meaning synchronization is performed when changing permissions (grant/revoke), but not
 * on checking permissions. This means there is potential for a race condition where one thread is checking whether the
 * user is authorized on a given data source while another thread is granting the permissions resulting in a false
 * negative of the authorization check. Similarly there could be a false positive if one thread is checking permissions
 * while another thread is revoking it. It is guaranteed though, that there are no race conditions between
 * granting/revoking permissions.
 * <p>
 * All granting/revoking of permissions are synchronized on the same object so there is a potential for contention if
 * there are MANY of these actions, but it is highly unlikely.
 */
public class ChronicleUserPermissionAuthorizationManager
{
    private static final Logger _logger = LoggerFactory.getLogger(ChronicleUserPermissionAuthorizationManager.class);
    private final Object _updateLock = new Object();
    private Map<String, Map<String, Map<String, Set<DataPermission>>>> _permissions;


    /**
     * Creates a new instance using the user permissions provided.
     *
     * @param assetTree                Chronicle Asset Tree to get permissions map from
     * @param dataSourcePermissionsUri Base uri for data source permissions
     * @param permissions              User permissions grouped by domain, user, data source, permissions.
     */
    public ChronicleUserPermissionAuthorizationManager(AssetTree assetTree, String dataSourcePermissionsUri, Map<String, Map<String, Map<String, Set<DataPermission>>>> permissions)
    {
        Map stringMapMapView = assetTree.acquireMap(dataSourcePermissionsUri, String.class, Map.class);
        _permissions = stringMapMapView;

        deepCopyPermissionsMap(permissions);
    }


    /**
     * Performs a deep copy of the given permissions map to the current permissions map. It does this by iterating
     * at all levels and creating new objects.
     * <p>
     * This is necessary to maintain synchronization policy.
     *
     * @param permissions Permissions to deep copy to current permissions.
     */
    private void deepCopyPermissionsMap(Map<String, Map<String, Map<String, Set<DataPermission>>>> permissions)
    {
        String domain = null;
        String userId = null;
        String dataSource = null;

        if (permissions != null && permissions.size() > 0)
        {
            for (Map.Entry<String, Map<String, Map<String, Set<DataPermission>>>> domainPermissions : permissions.entrySet())
            {
                domain = domainPermissions.getKey();

                for (Map.Entry<String, Map<String, Set<DataPermission>>> userPermissions : domainPermissions.getValue().entrySet())
                {
                    userId = userPermissions.getKey();

                    for (Map.Entry<String, Set<DataPermission>> dataSourcePermissions : userPermissions.getValue().entrySet())
                    {
                        dataSource = dataSourcePermissions.getKey();

                        for (DataPermission dataPermission : dataSourcePermissions.getValue())
                        {
                            grantUserPermissions(dataSource, domain, userId, dataPermission, true);
                        }
                    }

                }
            }
        }
    }


    /**
     * Gets the permissions for the given user/domain grouped by data source
     *
     * @param domain Domain for the user.
     * @param userId User id of the user.
     * @return Permissions grouped by data source, null if none exist.
     */
    private Map<String, Set<DataPermission>> getUserPermissions(String domain, String userId)
    {
        Map<String, Map<String, Set<DataPermission>>> domainPermissions = _permissions.get(domain);

        if (domainPermissions == null)
        {
            return null;
        }

        return domainPermissions.get(userId);
    }


    public boolean isUserAuthorized(String dataSourceId, String domain, String userId,
                                    DataPermission permissionRequired, Consumer<String> onUnauthorized)
    {
        Map<String, Set<DataPermission>> userPermissions = getUserPermissions(domain, userId);

        Set<DataPermission> dataSourcePermissions = null;

        if (userPermissions == null || (dataSourcePermissions = userPermissions.get(dataSourceId)) == null
                || !dataSourcePermissions.contains(permissionRequired))
        {
            if (onUnauthorized != null)
            {
                onUnauthorized.accept(String.format("Could not find required user permissions for data source '%s'!" +
                                " Tried to lookup user '%s' and check data access level '%s'.",
                        dataSourceId, domain + "/" + userId, permissionRequired));
            }

            return false;
        }

        return true;
    }



    public Map<String, Map<String, Map<String, Set<DataPermission>>>> getPermissions()
    {
        return _permissions;
    }


    /**
     * Grant the given permissions on the given data source to the given domain/user.
     *
     * @param dataSourceId   Data source to grant permissions on.
     * @param domain         Domain of user.
     * @param userId         User id.
     * @param dataPermission Permission to be granted on data source.
     */
    public void grantUserPermissions(String dataSourceId, String domain, String userId, DataPermission dataPermission)
    {
        grantUserPermissions(dataSourceId, domain, userId, dataPermission, false);
    }



    public void revokeUserPermissions(String dataSourceId, String domain, String userId, DataPermission dataPermission)
    {
        synchronized (_updateLock)
        {
            _logger.info("Revoking permissions '" + dataPermission + "' from user "
                    + domain + "/" + userId + "' on data source '" + dataSourceId + "'.");

            Map<String, Map<String, Set<DataPermission>>> domainPermissions = _permissions.get(domain);

            if (domainPermissions == null)
            {
                return;
            }

            Map<String, Set<DataPermission>> userPermissions = domainPermissions.get(userId);

            if (userPermissions == null)
            {
                return;
            }

            Set<DataPermission> dataSourcePermissions = userPermissions.get(dataSourceId);

            if (dataSourcePermissions == null)
            {
                return;
            }

            dataSourcePermissions.remove(dataPermission);

            //Remove data source permissions if the user no longer have any permissions
            if (dataSourcePermissions.size() == 0)
            {
                userPermissions.remove(dataSourceId);
            }

            _permissions.put(domain, domainPermissions);
        }
    }


    public void refreshUserPermissions(String domain, String userId, Map<String, Set<DataPermission>> userPermissions)
    {
        // Remove old permissions
        String dataSource = null;
        Map<String, Set<DataPermission>> userOldPermissions = getUserPermissions(domain, userId);
        if (userOldPermissions != null)
        {
            for (Map.Entry<String, Set<DataPermission>> dataSourcePermissions : userOldPermissions.entrySet())
            {
                dataSource = dataSourcePermissions.getKey();
                for (DataPermission dataPermission : dataSourcePermissions.getValue())
                {
                    revokeUserPermissions(dataSource, domain, userId, dataPermission);
                }
            }
        }

        // Create new permissions
        for (Map.Entry<String, Set<DataPermission>> dataSourcePermissions : userPermissions.entrySet())
        {
            dataSource = dataSourcePermissions.getKey();

            for (DataPermission dataPermission : dataSourcePermissions.getValue())
            {
                grantUserPermissions(dataSource, domain, userId, dataPermission, true);
            }
        }
    }


    /**
     * Grant the given permissions on the given data source to the given domain/user.
     *
     * @param dataSourceId    Data source to grant permissions on.
     * @param domain          Domain of user.
     * @param userId          User id.
     * @param dataPermissions Permissions to be granted on data source.
     */
    public void grantUserPermissions(String dataSourceId, String domain, String userId, Set<DataPermission> dataPermissions)
    {
        dataPermissions.forEach(permission -> grantUserPermissions(dataSourceId, domain, userId, permission));
    }


    /**
     * Revokes the given data permissions for the given data source for the given domain/user.
     * This does not automatically clean up empty maps/sets when the last permissions have been revoked from a given
     * data source. It would be very rare for empty maps/sets to stay for long as they are cleared on restart.
     *
     * @param dataSourceId    Data source id to revoke permissions from.
     * @param domain          Domain of user.
     * @param userId          Id of user.
     * @param dataPermissions Permissions to revoke.
     */
    public void revokeUserPermissions(String dataSourceId, String domain, String userId, Set<DataPermission> dataPermissions)
    {
        dataPermissions.forEach(permission -> revokeUserPermissions(dataSourceId, domain, userId, permission));
    }


    private void grantUserPermissions(String dataSourceId, String domain, String userId, DataPermission dataPermission,
                                      boolean isInitialization)
    {
        synchronized (_updateLock)
        {
            if (!isInitialization)
            {
                _logger.info("Granting user " + domain + "/" + userId + " permissions '"
                        + dataPermission + "' on data source '" + dataSourceId + "'.");
            }

            Map<String, Map<String, Set<DataPermission>>> domainPermissions = _permissions.get(domain);

            if (domainPermissions == null)
            {
                domainPermissions = new HashMap<>();
                _permissions.put(domain, domainPermissions);
            }

            Map<String, Set<DataPermission>> userPermissions = domainPermissions.get(userId);

            if (userPermissions == null)
            {
                userPermissions = new HashMap<>();
                domainPermissions.put(userId, userPermissions);
            }

            Set<DataPermission> dataSourcePermissions = userPermissions.get(dataSourceId);

            if (dataSourcePermissions == null)
            {
                dataSourcePermissions = new HashSet<>();
                userPermissions.put(dataSourceId, dataSourcePermissions);
            }

            dataSourcePermissions.add(dataPermission);

            //This is required to trigger an update in Chronicle as the entire domainPermissions map is the value!
            _permissions.put(domain, domainPermissions);
        }
    }
}