package ddp.api;

import ddp.api.authentication.*;
import ddp.api.authorisation.*;
import ddp.api.identity.*;
import ddp.api.security.*;

public class DataCacheBuilder
{
    private static EntitlementsManager _entitlementsManager = new EntitlementsManager();
    private static Authenticator _authenticator = new ChronicleAuthenticator();

    //TODO DS this should not be hard coded - get from config?
    private static String _primaryHostname = "localhost";
    private static int _primaryPort = 3216;
    private static String _primaryIpAddress = "localhost";

    //TODO DS have a builder which creates a Map DataCache for unit testing of Restricted map and unrestricted map functionality with having to integrate with Chronicle. Is there a performance problem with the multiple layers?

    //TODO DS need to provide methods for list of configs or single config, passed in the constructor or from config???

    //TODO DS consider having this on the interface itself as it allows static methods in Java 8. Although that might not be possible in C# and we should aim to keep the APIs as similar as possible.
    public static <K, V> RestrictedDataCache<K,V> createRestrictedDataCache(String dataCacheName,
                                                                            ClientIdentity clientIdentity) throws AuthenticationException, ConfigurationException //TODO DS consider using higher level identity interface
    {
        if(!_authenticator.authenticateClient(clientIdentity))
        {
            throw new AuthenticationException(
                    String.format("Client with id '%s' and entity '%s' is not authenticated to use the system!",
                            clientIdentity.getClientId(), clientIdentity.getEntity()));
        }

        //This throws an exception if the user isn't entitled to use the cache at all
        Entitlement entitlement = _entitlementsManager.getUserEntitlementsForDataCache(dataCacheName, clientIdentity);

        //READ is required to access
        entitlement.checkDataAccess(DataAccessLevel.READ);

        DataCacheConfiguration dataCacheConfiguration = new DataCacheConfiguration(_primaryHostname, _primaryIpAddress, _primaryPort, dataCacheName);

        //TODO DS generate list of failover configs...

        ChronicleDataCache<K, V> chronicleDataCache = new ChronicleDataCache<K, V>(dataCacheConfiguration);

        return new RestrictedDataCache<K, V>(chronicleDataCache, entitlement);
    }
}