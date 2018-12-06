package ddp.api.authorisation;

import ddp.api.identity.*;
import ddp.api.security.*;
import org.slf4j.*;

import java.util.*;

//TODO DS bytes marshallable
//TODO DS package private???
public class Entitlement
{
    private static final Logger _logger = LoggerFactory.getLogger(Entitlement.class);

    private ClientIdentity _clientIdentity; //TODO DS consider using higher level identity?
    private String _dataCacheName; //TODO DS is this needed in the entitlement?
    private Set<DataAccessLevel> _dataAccessLevels;
    private Set<AdminAccessLevel> _adminAccessLevels; //TODO DS is it even possible to do any admin tasks dynamically? Do we want to allow it?

    Entitlement(ClientIdentity clientIdentity, String dataCacheName, Set<DataAccessLevel> dataAccessLevels, Set<AdminAccessLevel> adminAccessLevels)
    {
        _clientIdentity = clientIdentity;
        _dataCacheName = dataCacheName;
        _dataAccessLevels = dataAccessLevels;
        _adminAccessLevels = adminAccessLevels;
    }

    public ClientIdentity getClientIdentity()
    {
        return _clientIdentity;
    }

    public String getDataCacheName()
    {
        return _dataCacheName;
    }

    public Set<DataAccessLevel> getDataAccessLevels()
    {
        return _dataAccessLevels;
    }

    public Set<AdminAccessLevel> getAdminAccessLevels()
    {
        return _adminAccessLevels;
    }

    //TODO DS this possibly needs to be async or running in a separate thread - needs to be thread-safe
    //TODO DS this is subject to change as and when we know what the subscription model for Chronicle looks like
    public void onEntitlementUpdate(Entitlement updatedEntitlement)
    {
        if(_clientIdentity.equals(updatedEntitlement.getClientIdentity())
                && _dataCacheName.equals(updatedEntitlement.getDataCacheName()))
        {
            _logger.info("Entitlement has been updated for user '{}' on Data Cache '{}'. Old DataAccessLevels: '{}', " +
                            "New DataAccessLevels: '{}', " +
                            "Old AdminAccessLevels: '{}', " +
                            "New AdminAccessLevels: '{}'.",
                    _clientIdentity, _dataCacheName,_dataAccessLevels, updatedEntitlement.getDataAccessLevels(),
                    _adminAccessLevels, updatedEntitlement.getAdminAccessLevels());

            //If user previously had subscribe access that is now revoked
            if(_dataAccessLevels.contains(DataAccessLevel.SUBSCRIBE)
                    && !updatedEntitlement.getDataAccessLevels().contains(DataAccessLevel.SUBSCRIBE))
            {
                //TODO DS need to ensure that any subscribers are removed from the chronicle map.
            }

            _dataAccessLevels = updatedEntitlement.getDataAccessLevels();
            _adminAccessLevels = updatedEntitlement.getAdminAccessLevels();
        }
    }

    /**
     * Checks whether the entitlement has the required access and throws {@link SecurityException} otherwise.
     * @param requiredAccess Access required to be checked against entitlement.
     */
    public void checkDataAccess(DataAccessLevel requiredAccess)
    {
        if(!_dataAccessLevels.contains(requiredAccess))
        {
            String errorMessage = String.format("User '%s' does not have the required '%s' data access level.", _clientIdentity, requiredAccess);

            _logger.error(errorMessage);

            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Checks whether the entitlement has the required access and throws {@link SecurityException} otherwise.
     * @param requiredAccess Access required to be checked against entitlement.
     */
    public void checkAdminAccess(AdminAccessLevel requiredAccess)
    {
        if(!_adminAccessLevels.contains(requiredAccess))
        {
            String errorMessage = String.format("User '%s' does not have the required '%s' admin access level.", _clientIdentity, requiredAccess);

            _logger.error(errorMessage);

            throw new SecurityException(errorMessage);
        }
    }
}